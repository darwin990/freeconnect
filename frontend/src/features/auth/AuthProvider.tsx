import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from "react";
import type { ReactNode } from "react";
import { refreshAccessToken } from "@/shared/api/client";
import { session } from "@/shared/api/session";
import type { AuthResponse, LoginPayload, RegisterPayload, Role, UserSummary } from "@/types/models";
import { authApi } from "./api";

type AuthStatus = "loading" | "authenticated" | "anonymous";

interface AuthContextValue {
  user: UserSummary | null;
  status: AuthStatus;
  login: (payload: LoginPayload) => Promise<UserSummary>;
  register: (payload: RegisterPayload) => Promise<UserSummary>;
  logout: () => Promise<void>;
  hasRole: (...roles: Role[]) => boolean;
}

const AuthContext = createContext<AuthContextValue | null>(null);

function summaryFromAuth(auth: AuthResponse): UserSummary {
  return auth.user;
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserSummary | null>(null);
  const [status, setStatus] = useState<AuthStatus>("loading");
  const bootstrapStarted = useRef(false);

  useEffect(() => {
    if (bootstrapStarted.current) return;
    bootstrapStarted.current = true;

    if (!session.hasStoredSession()) {
      setStatus("anonymous");
      return;
    }

    refreshAccessToken()
      .then((auth) => {
        if (auth) {
          setUser(summaryFromAuth(auth));
          setStatus("authenticated");
        } else {
          setUser(null);
          setStatus("anonymous");
        }
      })
      .catch(() => {
        session.clear();
        setUser(null);
        setStatus("anonymous");
      });
  }, []);

  useEffect(() => {
    const onExpired = () => {
      setUser(null);
      setStatus("anonymous");
    };
    window.addEventListener("freeconnect:session-expired", onExpired);
    return () => window.removeEventListener("freeconnect:session-expired", onExpired);
  }, []);

  const login = useCallback(async (payload: LoginPayload) => {
    const auth = await authApi.login(payload);
    session.setAuth(auth);
    setUser(auth.user);
    setStatus("authenticated");
    return auth.user;
  }, []);

  const register = useCallback(async (payload: RegisterPayload) => {
    const auth = await authApi.register(payload);
    session.setAuth(auth);
    setUser(auth.user);
    setStatus("authenticated");
    return auth.user;
  }, []);

  const logout = useCallback(async () => {
    const refreshToken = session.getRefreshToken();
    try {
      if (refreshToken) await authApi.logout(refreshToken);
    } catch {
      // El token local se elimina aunque la revocación remota falle.
    } finally {
      session.clear();
      setUser(null);
      setStatus("anonymous");
    }
  }, []);

  const hasRole = useCallback((...roles: Role[]) => Boolean(user && roles.includes(user.role)), [user]);

  const value = useMemo<AuthContextValue>(
    () => ({ user, status, login, register, logout, hasRole }),
    [user, status, login, register, logout, hasRole],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const value = useContext(AuthContext);
  if (!value) throw new Error("useAuth debe usarse dentro de AuthProvider");
  return value;
}
