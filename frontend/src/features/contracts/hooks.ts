import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { queryKeys } from "@/shared/api/queryKeys";
import type { AddDeliverablePayload, Contract } from "@/types/models";
import { contractsApi } from "./api";

function useInvalidateContract(contractId: string) {
  const queryClient = useQueryClient();
  return () => {
    queryClient.invalidateQueries({ queryKey: queryKeys.contract(contractId) });
    queryClient.invalidateQueries({ queryKey: queryKeys.contracts });
  };
}

export function useContracts(enabled = true) {
  return useQuery({
    queryKey: queryKeys.contracts,
    queryFn: contractsApi.list,
    enabled,
  });
}

export function useContract(id: string | undefined) {
  return useQuery({
    queryKey: queryKeys.contract(id ?? ""),
    queryFn: () => contractsApi.get(id as string),
    enabled: Boolean(id),
  });
}

export function useContractMutation<TVariables>(
  contractId: string,
  mutationFn: (variables: TVariables) => Promise<Contract>,
) {
  const invalidate = useInvalidateContract(contractId);
  return useMutation({ mutationFn, onSuccess: invalidate });
}

export function useAddDeliverable(contractId: string) {
  return useContractMutation<{ milestoneId: string; payload: AddDeliverablePayload }>(
    contractId,
    ({ milestoneId, payload }) => contractsApi.addDeliverable(contractId, milestoneId, payload),
  );
}

export function useSubmitMilestone(contractId: string) {
  return useContractMutation<string>(contractId, (milestoneId) =>
    contractsApi.submitMilestone(contractId, milestoneId),
  );
}

export function useApproveMilestone(contractId: string) {
  return useContractMutation<string>(contractId, (milestoneId) =>
    contractsApi.approveMilestone(contractId, milestoneId),
  );
}

export function useRejectMilestone(contractId: string) {
  return useContractMutation<{ milestoneId: string; reason: string }>(contractId, ({ milestoneId, reason }) =>
    contractsApi.rejectMilestone(contractId, milestoneId, reason),
  );
}

export function useCancelContract(contractId: string) {
  return useContractMutation<string>(contractId, (reason) => contractsApi.cancel(contractId, reason));
}

export function useAddTask(contractId: string) {
  return useContractMutation<{
    milestoneId: string;
    deliverableId: string;
    title: string;
  }>(contractId, ({ milestoneId, deliverableId, title }) =>
    contractsApi.addTask(contractId, milestoneId, deliverableId, title),
  );
}

export function useToggleTask(contractId: string) {
  return useContractMutation<{
    milestoneId: string;
    deliverableId: string;
    taskId: string;
    done: boolean;
  }>(contractId, ({ milestoneId, deliverableId, taskId, done }) =>
    contractsApi.toggleTask(contractId, milestoneId, deliverableId, taskId, done),
  );
}

export function useAddComment(contractId: string) {
  return useContractMutation<{
    milestoneId: string;
    deliverableId: string;
    body: string;
  }>(contractId, ({ milestoneId, deliverableId, body }) =>
    contractsApi.addComment(contractId, milestoneId, deliverableId, body),
  );
}
