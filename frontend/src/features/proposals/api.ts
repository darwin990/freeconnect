import { api } from "@/shared/api/client";
import type { AcceptProposalPayload, Contract, Proposal, SubmitProposalPayload } from "@/types/models";

export const proposalsApi = {
  list: (projectId: string) => api.get<Proposal[]>(`/projects/${projectId}/proposals`),
  submit: (projectId: string, payload: SubmitProposalPayload) =>
    api.post<Proposal>(`/projects/${projectId}/proposals`, payload),
  withdraw: (proposalId: string) => api.delete<Proposal>(`/proposals/${proposalId}`),
  evaluate: (proposalId: string, score: number, note?: string) =>
    api.post<Proposal>(`/proposals/${proposalId}/evaluate`, { score, note }),
  reject: (proposalId: string) => api.post<Proposal>(`/proposals/${proposalId}/reject`),
  accept: (proposalId: string, payload: AcceptProposalPayload) =>
    api.post<Contract>(`/proposals/${proposalId}/accept`, payload),
};
