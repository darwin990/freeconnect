import type { ProjectSearchFilters } from "./api";

export const DEFAULT_PAGE_SIZE = 12;

export function readProjectFilters(searchParams: URLSearchParams): ProjectSearchFilters {
  const number = (value: string | null, fallback: number) => {
    if (value === null || value === "") return fallback;
    const parsed = Number(value);
    return Number.isFinite(parsed) && parsed >= 0 ? parsed : fallback;
  };
  return {
    q: searchParams.get("q") ?? "",
    category: searchParams.get("category") ?? "",
    modality: searchParams.get("modality") ?? "",
    status: searchParams.has("status") ? (searchParams.get("status") ?? "") : "PUBLISHED",
    minBudget: searchParams.get("minBudget") ?? "",
    maxBudget: searchParams.get("maxBudget") ?? "",
    skill: searchParams.get("skill") ?? "",
    filter: searchParams.get("filter") ?? "",
    page: number(searchParams.get("page"), 0),
    size: number(searchParams.get("size"), DEFAULT_PAGE_SIZE),
  };
}

export function writeProjectFilters(filters: ProjectSearchFilters): URLSearchParams {
  const params = new URLSearchParams();
  const entries: [string, string | number | undefined][] = [
    ["q", filters.q],
    ["category", filters.category],
    ["modality", filters.modality],
    ["status", filters.status],
    ["minBudget", filters.minBudget],
    ["maxBudget", filters.maxBudget],
    ["skill", filters.skill],
    ["filter", filters.filter],
  ];
  for (const [key, value] of entries) {
    if (value !== undefined) params.set(key, String(value));
  }
  if (filters.page && filters.page > 0) params.set("page", String(filters.page));
  if (filters.size && filters.size !== DEFAULT_PAGE_SIZE) params.set("size", String(filters.size));
  return params;
}

export function normalizeProjectFilters(filters: ProjectSearchFilters): ProjectSearchFilters {
  return {
    ...filters,
    q: filters.q?.trim() || undefined,
    category: filters.category || undefined,
    modality: filters.modality || undefined,
    status: filters.status === "" ? undefined : filters.status,
    minBudget: filters.minBudget || undefined,
    maxBudget: filters.maxBudget || undefined,
    skill: filters.skill?.trim() || undefined,
    filter: filters.filter?.trim() || undefined,
    page: filters.page ?? 0,
    size: filters.size ?? DEFAULT_PAGE_SIZE,
  };
}
