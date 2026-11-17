import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { queryKeys } from "@/shared/api/queryKeys";
import type { CreatePortfolioPayload, UpdateProfilePayload } from "@/types/models";
import { profilesApi } from "./api";

export function useMyProfile() {
  return useQuery({
    queryKey: queryKeys.profile,
    queryFn: profilesApi.me,
    retry: false,
  });
}

export function useUpdateProfile() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: UpdateProfilePayload) => profilesApi.update(payload),
    onSuccess: (profile) => queryClient.setQueryData(queryKeys.profile, profile),
  });
}

export function useAddPortfolioItem() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: CreatePortfolioPayload) => profilesApi.addPortfolio(payload),
    onSuccess: (profile) => queryClient.setQueryData(queryKeys.profile, profile),
  });
}

export function useUploadPortfolioAttachment() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ itemId, file }: { itemId: string; file: File }) =>
      profilesApi.uploadAttachment(itemId, file),
    onSuccess: (profile) => queryClient.setQueryData(queryKeys.profile, profile),
  });
}
