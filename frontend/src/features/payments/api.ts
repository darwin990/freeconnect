import { api } from "@/shared/api/client";
import type { Payment } from "@/types/models";

export const paymentsApi = {
  mine: () => api.get<Payment[]>("/payments"),
  byContract: (contractId: string) => api.get<Payment[]>(`/contracts/${contractId}/payments`),
  release: (contractId: string, amount?: number) =>
    api.post<Payment>(`/contracts/${contractId}/payments`, { amount }),
};
