import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { queryKeys } from "@/shared/api/queryKeys";
import { ApiError } from "@/shared/api/errors";
import type { AcceptProposalPayload, SubmitProposalPayload } from "@/types/models";
import { proposalsApi } from "./api";

export function useProjectProposals(projectId: string | undefined, enabled = true) {
  return useQuery({
    queryKey: queryKeys.proposals(projectId ?? ""),
    queryFn: () => proposalsApi.list(projectId as string),
    enabled: Boolean(projectId) && enabled,
    retry: (failureCount, error) => {
      if (error instanceof ApiError && error.status === 403) return false;
      return failureCount < 2;
    },
  });
}

export function useSubmitProposal(projectId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: SubmitProposalPayload) => proposalsApi.submit(projectId, payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.proposals(projectId) }),
  });
}

export function useWithdrawProposal(projectId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (proposalId: string) => proposalsApi.withdraw(proposalId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.proposals(projectId) }),
  });
}

export function useEvaluateProposal(projectId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ proposalId, score, note }: { proposalId: string; score: number; note?: string }) =>
      proposalsApi.evaluate(proposalId, score, note),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.proposals(projectId) }),
  });
}

export function useRejectProposal(projectId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (proposalId: string) => proposalsApi.reject(proposalId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.proposals(projectId) }),
  });
}

export function useAcceptProposal() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ proposalId, payload }: { proposalId: string; payload: AcceptProposalPayload }) =>
      proposalsApi.accept(proposalId, payload),
    onSuccess: (contract) => {
      queryClient.invalidateQueries({ queryKey: ["proposals"] });
      queryClient.invalidateQueries({ queryKey: queryKeys.contracts });
      queryClient.invalidateQueries({ queryKey: queryKeys.project(contract.projectId) });
      queryClient.invalidateQueries({ queryKey: ["projects"] });
      queryClient.setQueryData(queryKeys.contract(contract.id), contract);
    },
  });
}
