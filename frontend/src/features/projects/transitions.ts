import type { ProjectStatus } from "@/types/models";

export const projectTransitions: Record<ProjectStatus, ProjectStatus[]> = {
  DRAFT: ["PUBLISHED", "CANCELLED"],
  PUBLISHED: ["PAUSED", "IN_PROGRESS", "CANCELLED"],
  PAUSED: ["PUBLISHED", "CANCELLED"],
  IN_PROGRESS: ["FINISHED", "CANCELLED"],
  FINISHED: [],
  CANCELLED: [],
};
