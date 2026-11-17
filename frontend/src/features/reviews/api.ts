import { api } from "@/shared/api/client";
import type { Review } from "@/types/models";

export const reviewsApi = {
  byUser: (userId: string) => api.get<Review[]>(`/users/${userId}/reviews`),
  byContract: (contractId: string) => api.get<Review[]>(`/contracts/${contractId}/reviews`),
  create: (contractId: string, rating: number, comment?: string) =>
    api.post<Review>(`/contracts/${contractId}/reviews`, { rating, comment }),
};
