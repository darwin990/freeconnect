import { api } from "@/shared/api/client";
import type { AdminReport, AuditLog, Project, ProjectStatus, SuspendResult } from "@/types/models";

export const adminApi = {
  reports: () => api.get<AdminReport[]>("/admin/reports"),
  audit: () => api.get<AuditLog[]>("/admin/audit"),
  suspendUser: (id: string) => api.post<SuspendResult>(`/admin/users/${id}/suspend`),
  moderateProject: (id: string, status: ProjectStatus) =>
    api.post<Project>(`/admin/projects/${id}/moderate`, { status }),
};
