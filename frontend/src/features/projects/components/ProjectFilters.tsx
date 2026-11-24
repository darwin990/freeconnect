import { useEffect, useState } from "react";
import { Search, SlidersHorizontal, X } from "lucide-react";
import type { ProjectSearchFilters } from "@/features/projects/api";
import { Button } from "@/shared/components/Button";
import { Field, Input, Select } from "@/shared/components/FormControls";
import { categoryOptions, modalityOptions, projectStatusOptions } from "@/shared/utils/labels";

interface FilterForm {
  q: string;
  category: string;
  modality: string;
  status: string;
  minBudget: string;
  maxBudget: string;
  skill: string;
  filter: string;
}

function fromFilters(filters: ProjectSearchFilters): FilterForm {
  return {
    q: filters.q ?? "",
    category: filters.category ?? "",
    modality: filters.modality ?? "",
    status: filters.status ?? "",
    minBudget: filters.minBudget ?? "",
    maxBudget: filters.maxBudget ?? "",
    skill: filters.skill ?? "",
    filter: filters.filter ?? "",
  };
}

export function ProjectFilters({
  filters,
  onApply,
  onReset,
  showStatus = true,
}: {
  filters: ProjectSearchFilters;
  onApply: (filters: ProjectSearchFilters) => void;
  onReset: () => void;
  showStatus?: boolean;
}) {
  const [form, setForm] = useState<FilterForm>(() => fromFilters(filters));
  const [advanced, setAdvanced] = useState(Boolean(filters.filter));

  useEffect(() => {
    setForm(fromFilters(filters));
  }, [filters]);

  const update = (key: keyof FilterForm, value: string) =>
    setForm((current) => ({ ...current, [key]: value }));

  return (
    <form
      className="rounded-2xl border border-ink-200 bg-white p-4 shadow-soft sm:p-5"
      onSubmit={(event) => {
        event.preventDefault();
        onApply({ ...filters, ...form, page: 0 });
      }}
      role="search"
      aria-label="Filtros de proyectos"
    >
      <div className="grid gap-3 md:grid-cols-[2fr_1fr_1fr_auto]">
        <Field label="Buscar" htmlFor="filter-q">
          <div className="relative">
            <Search className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-ink-400" aria-hidden="true" />
            <Input
              id="filter-q"
              value={form.q}
              onChange={(event) => update("q", event.target.value)}
              placeholder="Palabra clave del título o descripción"
              className="pl-9"
            />
          </div>
        </Field>
        <Field label="Categoría" htmlFor="filter-category">
          <Select id="filter-category" value={form.category} onChange={(event) => update("category", event.target.value)}>
            <option value="">Todas</option>
            {categoryOptions.map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </Select>
        </Field>
        <Field label="Modalidad" htmlFor="filter-modality">
          <Select id="filter-modality" value={form.modality} onChange={(event) => update("modality", event.target.value)}>
            <option value="">Todas</option>
            {modalityOptions.map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </Select>
        </Field>
        <div className="flex items-end gap-2">
          <Button type="submit" className="w-full">Buscar</Button>
          <Button
            variant="outline"
            onClick={() => setAdvanced((value) => !value)}
            aria-expanded={advanced}
            aria-controls="advanced-filters"
            icon={<SlidersHorizontal className="size-4" />}
          >
            <span className="hidden sm:inline">Más filtros</span>
          </Button>
        </div>
      </div>

      {advanced && (
        <div id="advanced-filters" className="mt-4 grid gap-3 border-t border-ink-200 pt-4 md:grid-cols-2 lg:grid-cols-4">
          {showStatus && (
            <Field label="Estado" htmlFor="filter-status">
              <Select id="filter-status" value={form.status} onChange={(event) => update("status", event.target.value)}>
                <option value="">Todos</option>
                {projectStatusOptions.map(([value, label]) => (
                  <option key={value} value={value}>
                    {label}
                  </option>
                ))}
              </Select>
            </Field>
          )}
          <Field label="Presupuesto mínimo" htmlFor="filter-min">
            <Input
              id="filter-min"
              type="number"
              min="0"
              step="0.01"
              value={form.minBudget}
              onChange={(event) => update("minBudget", event.target.value)}
              placeholder="0"
            />
          </Field>
          <Field label="Presupuesto máximo" htmlFor="filter-max">
            <Input
              id="filter-max"
              type="number"
              min="0"
              step="0.01"
              value={form.maxBudget}
              onChange={(event) => update("maxBudget", event.target.value)}
              placeholder="10000"
            />
          </Field>
          <Field label="Habilidad" htmlFor="filter-skill" hint="Puedes separar varias habilidades con comas.">
            <Input
              id="filter-skill"
              value={form.skill}
              onChange={(event) => update("skill", event.target.value)}
              placeholder="Java, React"
            />
          </Field>
          <Field
            label="Filtro DSL"
            htmlFor="filter-dsl"
            hint='Ej.: budget >= 1000 AND modality = REMOTE'
            className="md:col-span-2 lg:col-span-4"
          >
            <Input
              id="filter-dsl"
              value={form.filter}
              onChange={(event) => update("filter", event.target.value)}
              placeholder="budget >= 1000 AND modality = REMOTE"
              spellCheck={false}
              className="font-mono text-xs"
            />
          </Field>
        </div>
      )}

      <div className="mt-4 flex flex-wrap justify-end gap-2">
        <Button
          variant="ghost"
          size="sm"
          icon={<X className="size-4" />}
          onClick={() => {
            setForm(fromFilters({}));
            onReset();
          }}
        >
          Limpiar filtros
        </Button>
      </div>
    </form>
  );
}
