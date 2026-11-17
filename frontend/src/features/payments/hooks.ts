import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { queryKeys } from "@/shared/api/queryKeys";
import { paymentsApi } from "./api";

export function useMyPayments(enabled = true) {
  return useQuery({ queryKey: queryKeys.payments, queryFn: paymentsApi.mine, enabled });
}

export function useContractPayments(contractId: string | undefined) {
  return useQuery({
    queryKey: queryKeys.contractPayments(contractId ?? ""),
    queryFn: () => paymentsApi.byContract(contractId as string),
    enabled: Boolean(contractId),
  });
}

export function useReleasePayment(contractId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (amount?: number) => paymentsApi.release(contractId, amount),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.contractPayments(contractId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.payments });
      queryClient.invalidateQueries({ queryKey: queryKeys.notifications });
    },
  });
}
