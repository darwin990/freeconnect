import type { AuthResponse } from "@/types/models";
import { ApiError, parseApiError } from "./errors";
import { notifySessionExpired, session } from "./session";

export const API_BASE_URL = (
  import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api/v1"
).replace(/\/+$/, "");

type QueryValue = string | number | boolean | null | undefined | string[];

export function toQueryString(params: Record<string, QueryValue>): string {
  const search = new URLSearchParams();
  for (const [key, value] of Object.entries(params)) {
    if (value === undefined || value === null || value === "") continue;
    if (Array.isArray(value)) {
      value.filter(Boolean).forEach((entry) => search.append(key, entry));
    } else {
      search.set(key, String(value));
    }
  }
  const result = search.toString();
  return result ? `?${result}` : "";
}

interface RequestOptions extends Omit<RequestInit, "body"> {
  body?: unknown;
  auth?: boolean;
  retry?: boolean;
  responseType?: "json" | "text" | "blob";
  timeoutMs?: number;
}

let refreshInFlight: Promise<AuthResponse | null> | null = null;

async function parseBody<T>(response: Response, responseType: RequestOptions["responseType"]): Promise<T> {
  if (response.status === 204) return undefined as T;
  if (responseType === "blob") return (await response.blob()) as T;
  if (responseType === "text") return (await response.text()) as T;
  const text = await response.text();
  if (!text) return undefined as T;
  try {
    return JSON.parse(text) as T;
  } catch {
    return text as T;
  }
}

async function perform<T>(path: string, options: RequestOptions, isRetry: boolean): Promise<T> {
  const { body, auth = true, retry = true, responseType = "json", headers, timeoutMs, ...rest } = options;
  const finalHeaders = new Headers(headers);
  const token = session.getAccessToken();
  if (auth && token) finalHeaders.set("Authorization", `Bearer ${token}`);

  let payload: BodyInit | undefined;
  if (body instanceof FormData) {
    payload = body;
  } else if (body !== undefined) {
    finalHeaders.set("Content-Type", "application/json");
    payload = JSON.stringify(body);
  }

  const controller = new AbortController();
  const timer = timeoutMs ? window.setTimeout(() => controller.abort(), timeoutMs) : undefined;

  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...rest,
      headers: finalHeaders,
      body: payload,
      signal: rest.signal ?? controller.signal,
    });
  } catch (error) {
    if (error instanceof DOMException && error.name === "AbortError") {
      throw new ApiError(0, "TIMEOUT", "La solicitud tardó demasiado. Inténtalo de nuevo.");
    }
    throw new ApiError(
      0,
      "NETWORK_ERROR",
      "No se pudo contactar con el servidor. Comprueba que la API esté disponible.",
    );
  } finally {
    if (timer) window.clearTimeout(timer);
  }

  if (response.status === 401 && auth && retry && !isRetry && session.getRefreshToken()) {
    const refreshed = await refreshAccessToken();
    if (refreshed) return perform<T>(path, options, true);
    notifySessionExpired();
  }

  if (!response.ok) throw await parseApiError(response);
  return parseBody<T>(response, responseType);
}

export async function refreshAccessToken(): Promise<AuthResponse | null> {
  const refreshToken = session.getRefreshToken();
  if (!refreshToken) return null;
  if (refreshInFlight) return refreshInFlight;

  refreshInFlight = (async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/refresh`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ refreshToken }),
      });
      if (!response.ok) {
        session.clear();
        return null;
      }
      const auth = (await response.json()) as AuthResponse;
      session.setAuth(auth);
      return auth;
    } catch {
      return null;
    } finally {
      refreshInFlight = null;
    }
  })();

  return refreshInFlight;
}

async function download(path: string, isRetry = false): Promise<{ blob: Blob; filename: string }> {
  const token = session.getAccessToken();
  const headers = new Headers();
  if (token) headers.set("Authorization", `Bearer ${token}`);

  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, { headers });
  } catch {
    throw new ApiError(0, "NETWORK_ERROR", "No se pudo descargar el archivo.");
  }

  if (response.status === 401 && !isRetry && session.getRefreshToken()) {
    const refreshed = await refreshAccessToken();
    if (refreshed) return download(path, true);
    notifySessionExpired();
  }
  if (!response.ok) throw await parseApiError(response);

  const disposition = response.headers.get("Content-Disposition") ?? "";
  const match = /filename="?([^";]+)"?/i.exec(disposition);
  return {
    blob: await response.blob(),
    filename: match?.[1] ?? "descarga",
  };
}

export const api = {
  get: <T>(path: string, options?: RequestOptions) => perform<T>(path, { ...options, method: "GET" }, false),
  post: <T>(path: string, body?: unknown, options?: RequestOptions) =>
    perform<T>(path, { ...options, method: "POST", body }, false),
  put: <T>(path: string, body?: unknown, options?: RequestOptions) =>
    perform<T>(path, { ...options, method: "PUT", body }, false),
  patch: <T>(path: string, body?: unknown, options?: RequestOptions) =>
    perform<T>(path, { ...options, method: "PATCH", body }, false),
  delete: <T>(path: string, options?: RequestOptions) =>
    perform<T>(path, { ...options, method: "DELETE" }, false),
  upload: <T>(path: string, formData: FormData) =>
    perform<T>(path, { method: "POST", body: formData }, false),
  download,
};

export function triggerDownload(blob: Blob, filename: string) {
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement("a");
  anchor.href = url;
  anchor.download = filename;
  document.body.appendChild(anchor);
  anchor.click();
  anchor.remove();
  URL.revokeObjectURL(url);
}
