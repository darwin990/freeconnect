import { describe, expect, it } from "vitest";
import {
  DEFAULT_PAGE_SIZE,
  normalizeProjectFilters,
  readProjectFilters,
  writeProjectFilters,
} from "@/features/projects/filters";

describe("filtros de proyectos sincronizados con la URL", () => {
  it("usa PUBLISHED por defecto y 0/12 para paginación", () => {
    const filters = readProjectFilters(new URLSearchParams());

    expect(filters.status).toBe("PUBLISHED");
    expect(filters.page).toBe(0);
    expect(filters.size).toBe(DEFAULT_PAGE_SIZE);
  });

  it("distingue status ausente de status vacío para ver todos los estados", () => {
    const filters = readProjectFilters(new URLSearchParams("status="));

    expect(filters.status).toBe("");
  });

  it("hace round-trip de todos los filtros", () => {
    const original = {
      q: "react",
      category: "SOFTWARE",
      modality: "REMOTE",
      status: "PUBLISHED",
      minBudget: "100",
      maxBudget: "5000",
      skill: "TypeScript",
      filter: "budget>=100",
      page: 2,
      size: 24,
    };

    const roundTrip = readProjectFilters(writeProjectFilters(original));

    expect(roundTrip).toEqual(original);
  });

  it("normaliza valores vacíos al payload que espera la API", () => {
    const normalized = normalizeProjectFilters({
      q: "  api  ",
      category: "",
      modality: "",
      status: "",
      minBudget: "",
      maxBudget: "",
      skill: "  java ",
      filter: "",
      page: 0,
      size: 12,
    });

    expect(normalized.q).toBe("api");
    expect(normalized.category).toBeUndefined();
    expect(normalized.modality).toBeUndefined();
    expect(normalized.status).toBeUndefined();
    expect(normalized.skill).toBe("java");
    expect(normalized.filter).toBeUndefined();
  });
});
