import { api } from "@/shared/api/client";
import type { Notification } from "@/types/models";

export const notificationsApi = {
  list: () => api.get<Notification[]>("/notifications"),
  retry: (id: string) => api.post<Notification>(`/notifications/${id}/retry`),
};
