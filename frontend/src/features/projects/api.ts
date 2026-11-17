import { api, toQueryString } from "@/shared/api/client";
import type {
  Project,
  ProjectPage,
  ProjectPayload,
  ProjectStatus,
  ProjectTemplate,
} from "@/types/models";

export interface ProjectSearchFilters {
  q?: string;
  category?: string;
  modality?: string;
  status?: string;
  minBudget?: string;
  maxBudget?: string;
  skill?: string;
  filter?: string;
  page?: number;
  size?: number;
}

export const projectsApi = {
  search: (filters: ProjectSearchFilters) => {
    const query = toQueryString({
      page: filters.page ?? 0,
      size: filters.size ?? 12,
      q: filters.q,
      category: filters.category,
      modality: filters.modality,
      status: filters.status,
      minBudget: filters.minBudget,
      maxBudget: filters.maxBudget,
      skill: filters.skill,
      filter: filters.filter,
    });
    return api.get<ProjectPage>(`/projects${query}`);
  },
  get: (id: string) => api.get<Project>(`/projects/${id}`),
  create: (payload: ProjectPayload) => api.post<Project>("/projects", payload),
  update: (id: string, payload: ProjectPayload) => api.put<Project>(`/projects/${id}`, payload),
  changeStatus: (id: string, status: ProjectStatus) =>
    api.patch<Project>(`/projects/${id}/status`, { status }),
  templates: () => api.get<ProjectTemplate[]>("/projects/templates"),
  fromTemplate: (templateId: string) =>
    api.post<Project>(`/projects/from-template/${templateId}`),
  exportMine: () => api.download("/projects/mine/export"),
  async mine(clientId: string): Promise<Project[]> {
    const projects: Project[] = [];
    for (let page = 0; page < 10; page += 1) {
      const result = await projectsApi.search({ page, size: 100 });
      projects.push(...result.content.filter((project) => project.clientId === clientId));
      if (!result.hasNext) break;
    }
    return projects.sort((a, b) => (b.createdAt ?? "").localeCompare(a.createdAt ?? ""));
  },
};
