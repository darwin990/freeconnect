import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { api } from "@/shared/api/client";
import { session } from "@/shared/api/session";
import type { AuthResponse } from "@/types/models";

const initialAuth = {
  accessToken: "old-access",
  refreshToken: "refresh-1",
  tokenType: "Bearer",
  expiresIn: 3600,
  user: {
    id: "11111111-1111-1111-1111-111111111111",
    email: "cliente@example.com",
    displayName: "Cliente",
    role: "CLIENT",
  },
} as AuthResponse;

const refreshedAuth = {
  ...initialAuth,
  accessToken: "new-access",
  refreshToken: "refresh-2",
} as AuthResponse;

describe("cliente HTTP", () => {
  beforeEach(() => {
    session.clear();
    session.setAuth(initialAuth);
  });

  afterEach(() => {
    session.clear();
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
  });

  it("refresca una sola vez ante 401 y reintenta con el token nuevo", async () => {
    let projectCalls = 0;
    const fetchMock = vi.fn(async (input: RequestInfo | URL, _init?: RequestInit) => {
      const url = String(input);
      if (url.endsWith("/auth/refresh")) {
        return new Response(JSON.stringify(refreshedAuth), {
          status: 200,
          headers: { "Content-Type": "application/json" },
        });
      }
      projectCalls += 1;
      if (projectCalls === 1) {
        return new Response(JSON.stringify({ code: "UNAUTHORIZED", message: "Token expirado" }), {
          status: 401,
          headers: { "Content-Type": "application/json" },
        });
      }
      return new Response(
        JSON.stringify({
          content: [],
          page: 0,
          size: 12,
          totalElements: 0,
          totalPages: 0,
          hasNext: false,
        }),
        {
          status: 200,
          headers: { "Content-Type": "application/json" },
        },
      );
    });
    vi.stubGlobal("fetch", fetchMock);

    const result = await api.get<{ totalElements: number }>("/projects?page=0");

    expect(result.totalElements).toBe(0);
    expect(projectCalls).toBe(2);
    expect(fetchMock).toHaveBeenCalledTimes(3);
    expect(session.getAccessToken()).toBe("new-access");
    expect(session.getRefreshToken()).toBe("refresh-2");

    const retriedCall = fetchMock.mock.calls[2];
    const headers = new Headers((retriedCall[1] as RequestInit).headers);
    expect(headers.get("Authorization")).toBe("Bearer new-access");
  });

  it("no intenta refrescar si el error no es 401", async () => {
    const fetchMock = vi.fn(
      async () =>
        new Response(JSON.stringify({ code: "FORBIDDEN", message: "Sin permiso" }), {
          status: 403,
          headers: { "Content-Type": "application/json" },
        }),
    );
    vi.stubGlobal("fetch", fetchMock);

    await expect(api.get("/contracts")).rejects.toMatchObject({
      status: 403,
      code: "FORBIDDEN",
    });
    expect(fetchMock).toHaveBeenCalledTimes(1);
  });

  it("traduce fallos de red a un ApiError de estado 0", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn(async () => {
        throw new TypeError("fetch failed");
      }),
    );

    await expect(api.get("/projects")).rejects.toMatchObject({
      status: 0,
      code: "NETWORK_ERROR",
    });
  });
});
