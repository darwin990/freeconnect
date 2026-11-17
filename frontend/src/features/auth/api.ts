import { api } from "@/shared/api/client";
import { session } from "@/shared/api/session";
import type { AuthResponse, LoginPayload, RegisterPayload, UserView } from "@/types/models";

export const authApi = {
  login: (payload: LoginPayload) =>
    api.post<AuthResponse>("/auth/login", payload, { auth: false, retry: false }),
  register: (payload: RegisterPayload) =>
    api.post<AuthResponse>("/auth/register", payload, { auth: false, retry: false }),
  logout: (refreshToken: string) => api.post<void>("/auth/logout", { refreshToken }),
  me: () => api.get<UserView>("/auth/me"),
  storedRefreshToken: () => session.getRefreshToken(),
};
