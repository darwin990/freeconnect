import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { queryKeys } from "@/shared/api/queryKeys";
import type { ProjectStatus } from "@/types/models";
import { adminApi } from "./api";

export function useAdminReports(enabled = true) {
  return useQuery({
    queryKey: queryKeys.adminReports,
    queryFn: adminApi.reports,
    staleTime: 30_000,
    enabled,
  });
}

export function useAdminAudit(enabled = true) {
  return useQuery({ queryKey: queryKeys.adminAudit, queryFn: adminApi.audit, enabled });
}

export function useSuspendUser() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => adminApi.suspendUser(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.adminReports });
      queryClient.invalidateQueries({ queryKey: queryKeys.adminAudit });
    },
  });
}

export function useModerateProject() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, status }: { id: string; status: ProjectStatus }) =>
      adminApi.moderateProject(id, status),
    onSuccess: (project) => {
      queryClient.setQueryData(queryKeys.project(project.id), project);
      queryClient.invalidateQueries({ queryKey: ["projects"] });
      queryClient.invalidateQueries({ queryKey: queryKeys.adminAudit });
    },
  });
}
