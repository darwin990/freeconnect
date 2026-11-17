import { api } from "@/shared/api/client";
import type { Conversation } from "@/types/models";

export const messagingApi = {
  list: () => api.get<Conversation[]>("/conversations"),
  get: (id: string) => api.get<Conversation>(`/conversations/${id}`),
  send: (id: string, body: string) =>
    api.post<Conversation>(`/conversations/${id}/messages`, { body }),
  markRead: (id: string) => api.post<Conversation>(`/conversations/${id}/read`),
  archive: (id: string) => api.post<Conversation>(`/conversations/${id}/archive`),
};
