import { api } from "@/shared/api/client";
import type { AddDeliverablePayload, Contract, Conversation } from "@/types/models";

export const contractsApi = {
  list: () => api.get<Contract[]>("/contracts"),
  get: (id: string) => api.get<Contract>(`/contracts/${id}`),
  addDeliverable: (
    contractId: string,
    milestoneId: string,
    payload: AddDeliverablePayload,
  ) => api.post<Contract>(`/contracts/${contractId}/milestones/${milestoneId}/deliverables`, payload),
  submitMilestone: (contractId: string, milestoneId: string) =>
    api.post<Contract>(`/contracts/${contractId}/milestones/${milestoneId}/submit`),
  approveMilestone: (contractId: string, milestoneId: string) =>
    api.post<Contract>(`/contracts/${contractId}/milestones/${milestoneId}/approve`),
  rejectMilestone: (contractId: string, milestoneId: string, reason: string) =>
    api.post<Contract>(`/contracts/${contractId}/milestones/${milestoneId}/reject`, { reason }),
  cancel: (contractId: string, reason: string) =>
    api.post<Contract>(`/contracts/${contractId}/cancel`, { reason }),
  addTask: (contractId: string, milestoneId: string, deliverableId: string, title: string) =>
    api.post<Contract>(
      `/contracts/${contractId}/milestones/${milestoneId}/deliverables/${deliverableId}/tasks`,
      { title },
    ),
  toggleTask: (
    contractId: string,
    milestoneId: string,
    deliverableId: string,
    taskId: string,
    done: boolean,
  ) =>
    api.patch<Contract>(
      `/contracts/${contractId}/milestones/${milestoneId}/deliverables/${deliverableId}/tasks/${taskId}`,
      { done },
    ),
  addComment: (
    contractId: string,
    milestoneId: string,
    deliverableId: string,
    body: string,
  ) =>
    api.post<Contract>(
      `/contracts/${contractId}/milestones/${milestoneId}/deliverables/${deliverableId}/comments`,
      { body },
    ),
  openConversation: (contractId: string) =>
    api.post<Conversation>(`/contracts/${contractId}/conversations`),
};
