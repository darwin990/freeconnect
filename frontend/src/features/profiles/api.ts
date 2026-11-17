import { api } from "@/shared/api/client";
import type { CreatePortfolioPayload, Profile, UpdateProfilePayload } from "@/types/models";

export const profilesApi = {
  me: () => api.get<Profile>("/profiles/me"),
  byId: (id: string) => api.get<Profile>(`/profiles/${id}`),
  update: (payload: UpdateProfilePayload) => api.put<Profile>("/profiles/me", payload),
  addPortfolio: (payload: CreatePortfolioPayload) => api.post<Profile>("/profiles/me/portfolio", payload),
  uploadAttachment: (itemId: string, file: File) => {
    const formData = new FormData();
    formData.append("file", file);
    return api.upload<Profile>(`/profiles/me/portfolio/${itemId}/attachment`, formData);
  },
  downloadAttachment: (itemId: string) =>
    api.download(`/profiles/me/portfolio/${itemId}/attachment`),
};
