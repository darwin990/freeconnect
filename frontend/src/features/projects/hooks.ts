import { keepPreviousData, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { queryKeys } from "@/shared/api/queryKeys";
import type { ProjectPayload, ProjectStatus } from "@/types/models";
import { projectsApi, type ProjectSearchFilters } from "./api";

export function useProjects(filters: ProjectSearchFilters, enabled = true) {
  return useQuery({
    queryKey: queryKeys.projects(filters as Record<string, unknown>),
    queryFn: () => projectsApi.search(filters),
    placeholderData: keepPreviousData,
    enabled,
  });
}

export function useProject(id: string | undefined) {
  return useQuery({
    queryKey: queryKeys.project(id ?? ""),
    queryFn: () => projectsApi.get(id as string),
    enabled: Boolean(id),
  });
}

export function useMyProjects(clientId: string | undefined) {
  return useQuery({
    queryKey: ["projects", "mine", clientId],
    queryFn: () => projectsApi.mine(clientId as string),
    enabled: Boolean(clientId),
    staleTime: 30_000,
  });
}

export function useProjectTemplates() {
  return useQuery({
    queryKey: queryKeys.templates,
    queryFn: projectsApi.templates,
    staleTime: 5 * 60_000,
  });
}

export function useCreateProject() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: ProjectPayload) => projectsApi.create(payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["projects"] }),
  });
}

export function useUpdateProject(id: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: ProjectPayload) => projectsApi.update(id, payload),
    onSuccess: (project) => {
      queryClient.setQueryData(queryKeys.project(id), project);
      queryClient.invalidateQueries({ queryKey: ["projects"] });
    },
  });
}

export function useChangeProjectStatus(id: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (status: ProjectStatus) => projectsApi.changeStatus(id, status),
    onSuccess: (project) => {
      queryClient.setQueryData(queryKeys.project(id), project);
      queryClient.invalidateQueries({ queryKey: ["projects"] });
    },
  });
}

export function useCloneTemplate() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (templateId: string) => projectsApi.fromTemplate(templateId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["projects"] }),
  });
}
