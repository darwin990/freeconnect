import type { AuthResponse } from "@/types/models";

const REFRESH_KEY = "freeconnect.refreshToken";

let accessToken: string | null = null;
let refreshToken: string | null = readStoredRefresh();
const listeners = new Set<() => void>();

function readStoredRefresh(): string | null {
  try {
    return window.sessionStorage.getItem(REFRESH_KEY);
  } catch {
    return null;
  }
}

function persistRefresh(token: string | null) {
  try {
    if (token) window.sessionStorage.setItem(REFRESH_KEY, token);
    else window.sessionStorage.removeItem(REFRESH_KEY);
  } catch {
    // sessionStorage puede estar bloqueado (modo privado). La sesión sigue en memoria.
  }
}

function notify() {
  listeners.forEach((listener) => listener());
}

export const session = {
  getAccessToken: () => accessToken,
  getRefreshToken: () => refreshToken,
  hasStoredSession: () => Boolean(refreshToken),
  setAuth(auth: AuthResponse) {
    accessToken = auth.accessToken;
    refreshToken = auth.refreshToken;
    persistRefresh(refreshToken);
    notify();
  },
  setAccessToken(token: string) {
    accessToken = token;
    notify();
  },
  clear() {
    accessToken = null;
    refreshToken = null;
    persistRefresh(null);
    notify();
  },
  subscribe(listener: () => void) {
    listeners.add(listener);
    return () => listeners.delete(listener);
  },
};

export function notifySessionExpired() {
  window.dispatchEvent(new CustomEvent("freeconnect:session-expired"));
}
