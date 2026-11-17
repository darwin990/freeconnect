import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { queryKeys } from "@/shared/api/queryKeys";
import { reviewsApi } from "./api";

export function useUserReviews(userId: string | undefined) {
  return useQuery({
    queryKey: queryKeys.reviews(userId ?? ""),
    queryFn: () => reviewsApi.byUser(userId as string),
    enabled: Boolean(userId),
  });
}

export function useContractReviews(contractId: string | undefined) {
  return useQuery({
    queryKey: queryKeys.contractReviews(contractId ?? ""),
    queryFn: () => reviewsApi.byContract(contractId as string),
    enabled: Boolean(contractId),
  });
}

export function useCreateReview(contractId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ rating, comment }: { rating: number; comment?: string }) =>
      reviewsApi.create(contractId, rating, comment),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.contractReviews(contractId) });
      queryClient.invalidateQueries({ queryKey: ["reviews"] });
      queryClient.invalidateQueries({ queryKey: queryKeys.notifications });
    },
  });
}
