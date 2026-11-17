import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { session } from "@/shared/api/session";
import type { AuthResponse } from "@/types/models";

const auth = {
  accessToken: "access-1",
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

describe("session", () => {
  beforeEach(() => {
    session.clear();
  });

  afterEach(() => {
    session.clear();
  });

  it("guarda el access token en memoria y el refresh en sessionStorage", () => {
    session.setAuth(auth);

    expect(session.getAccessToken()).toBe("access-1");
    expect(session.getRefreshToken()).toBe("refresh-1");
    expect(session.hasStoredSession()).toBe(true);
    expect(window.sessionStorage.getItem("freeconnect.refreshToken")).toBe("refresh-1");
  });

  it("limpia ambos tokens", () => {
    session.setAuth(auth);
    session.clear();

    expect(session.getAccessToken()).toBeNull();
    expect(session.getRefreshToken()).toBeNull();
    expect(session.hasStoredSession()).toBe(false);
    expect(window.sessionStorage.getItem("freeconnect.refreshToken")).toBeNull();
  });

  it("notifica a los suscriptores en cada cambio", () => {
    const listener = vi.fn();
    const unsubscribe = session.subscribe(listener);

    session.setAuth(auth);
    session.setAccessToken("access-2");
    session.clear();
    unsubscribe();
    session.setAuth(auth);

    expect(listener).toHaveBeenCalledTimes(3);
  });
});
