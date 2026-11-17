import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Link, useNavigate, useParams } from "react-router-dom";
import { ArrowLeft } from "lucide-react";
import { useAuth } from "@/features/auth/AuthProvider";
import { projectSchema, type ProjectFormValues } from "@/features/projects/schemas";
import { useCreateProject, useProject, useUpdateProject } from "@/features/projects/hooks";
import { ErrorAlert } from "@/shared/components/Alert";
import { Button } from "@/shared/components/Button";
import { Card } from "@/shared/components/Card";
import { Field, Input, Select, Textarea } from "@/shared/components/FormControls";
import { PageHeader } from "@/shared/components/PageHeader";
import { PageLoader } from "@/shared/components/Spinner";
import { useDocumentTitle } from "@/shared/hooks/useDocumentTitle";
import { categoryOptions, modalityOptions } from "@/shared/utils/labels";

const defaults: ProjectFormValues = {
  title: "",
  description: "",
  category: "SOFTWARE",
  budget: "",
  currency: "USD",
  modality: "REMOTE",
  deadline: "",
  skills: "",
};

export function ProjectFormPage() {
  const { id } = useParams<{ id: string }>();
  const editing = Boolean(id);
  useDocumentTitle(editing ? "Editar proyecto" : "Nuevo proyecto");
  const { user } = useAuth();
  const navigate = useNavigate();
  const projectQuery = useProject(id);
  const createProject = useCreateProject();
  const updateProject = useUpdateProject(id ?? "");
  const [error, setError] = useState<unknown>(null);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<ProjectFormValues>({
    resolver: zodResolver(projectSchema),
    defaultValues: defaults,
  });

  const project = projectQuery.data;

  useEffect(() => {
    if (!project) return;
    reset({
      title: project.title,
      description: project.description ?? "",
      category: project.category,
      budget: String(project.budgetAmount),
      currency: project.currency,
      modality: project.modality,
      deadline: project.deadline ?? "",
      skills: (project.skills ?? []).join(", "),
    });
  }, [project, reset]);

  if (editing && projectQuery.isLoading) return <PageLoader label="Cargando proyecto" />;
  if (editing && projectQuery.isError) {
    return <ErrorAlert error={projectQuery.error} title="No se pudo cargar el proyecto" />;
  }
  if (editing && project && user && project.clientId !== user.id) {
    return (
      <Card>
        <p className="text-sm text-ink-700">Solo el cliente propietario puede editar este proyecto.</p>
      </Card>
    );
  }

  const notEditable =
    project && ["IN_PROGRESS", "FINISHED", "CANCELLED"].includes(project.status);

  const onSubmit = async (values: ProjectFormValues) => {
    setError(null);
    const payload = {
      title: values.title.trim(),
      description: values.description?.trim() || undefined,
      category: values.category,
      budget: Number(values.budget),
      currency: values.currency.toUpperCase(),
      modality: values.modality,
      deadline: values.deadline || undefined,
      skills: (values.skills ?? "")
        .split(",")
        .map((skill) => skill.trim())
        .filter(Boolean),
    };
    try {
      const saved = editing
        ? await updateProject.mutateAsync(payload)
        : await createProject.mutateAsync(payload);
      navigate(`/proyectos/${saved.id}`, { state: { [editing ? "updated" : "created"]: true } });
    } catch (cause) {
      setError(cause);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <Link to={editing && id ? `/proyectos/${id}` : "/app/mis-proyectos"} className="inline-flex items-center gap-1 text-sm font-medium text-ink-500 hover:text-ink-800">
          <ArrowLeft className="size-4" aria-hidden="true" />
          Volver
        </Link>
      </div>
      <PageHeader
        title={editing ? "Editar proyecto" : "Publicar un proyecto"}
        description={
          editing
            ? "Los cambios se guardan en el proyecto. Algunos estados no permiten edición."
            : "El proyecto se crea como borrador. Publícalo cuando esté listo para recibir propuestas."
        }
      />

      {notEditable && (
        <ErrorAlert
          error={new Error("El proyecto está en un estado que no admite edición.")}
          title="Proyecto no editable"
        />
      )}
      {!editing && (
        <div className="rounded-xl border border-sky-200 bg-sky-50 p-4 text-sm text-sky-900" role="status">
          Al guardar se creará en estado <strong>DRAFT</strong>. Después podrás publicarlo desde el detalle.
        </div>
      )}

      <Card>
        <form className="space-y-5" onSubmit={handleSubmit(onSubmit)} noValidate>
          {error ? <ErrorAlert error={error} title="No se pudo guardar el proyecto" /> : null}

          <Field label="Título" htmlFor="title" error={errors.title?.message} required>
            <Input id="title" placeholder="API de reservas para plataforma de turismo" invalid={Boolean(errors.title)} {...register("title")} />
          </Field>

          <Field label="Descripción" htmlFor="description" error={errors.description?.message}>
            <Textarea
              id="description"
              rows={6}
              placeholder="Objetivo, alcance, entregables esperados y criterios de aceptación."
              {...register("description")}
            />
          </Field>

          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <Field label="Categoría" htmlFor="category" error={errors.category?.message} required>
              <Select id="category" {...register("category")}>
                {categoryOptions.map(([value, label]) => (
                  <option key={value} value={value}>
                    {label}
                  </option>
                ))}
              </Select>
            </Field>
            <Field label="Modalidad" htmlFor="modality" error={errors.modality?.message}>
              <Select id="modality" {...register("modality")}>
                {modalityOptions.map(([value, label]) => (
                  <option key={value} value={value}>
                    {label}
                  </option>
                ))}
              </Select>
            </Field>
            <Field label="Presupuesto" htmlFor="budget" error={errors.budget?.message} required>
              <Input id="budget" type="number" min="0.01" step="0.01" placeholder="1500" invalid={Boolean(errors.budget)} {...register("budget")} />
            </Field>
            <Field label="Moneda" htmlFor="currency" error={errors.currency?.message} required>
              <Input id="currency" maxLength={3} placeholder="USD" {...register("currency")} />
            </Field>
          </div>

          <div className="grid gap-4 sm:grid-cols-2">
            <Field label="Fecha límite" htmlFor="deadline" error={errors.deadline?.message}>
              <Input id="deadline" type="date" {...register("deadline")} />
            </Field>
            <Field label="Habilidades" htmlFor="skills" hint="Sepáralas con comas." error={errors.skills?.message}>
              <Input id="skills" placeholder="Java, Spring Boot, PostgreSQL" {...register("skills")} />
            </Field>
          </div>

          <div className="flex flex-wrap justify-end gap-2 border-t border-ink-200 pt-5">
            <Link to={editing && id ? `/proyectos/${id}` : "/app/mis-proyectos"}>
              <Button variant="outline">Cancelar</Button>
            </Link>
            <Button type="submit" loading={isSubmitting || createProject.isPending || updateProject.isPending} disabled={Boolean(notEditable)}>
              {editing ? "Guardar cambios" : "Crear borrador"}
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
}
