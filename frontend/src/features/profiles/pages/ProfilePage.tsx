import { useEffect, useRef, useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Download, Plus, Star, Upload } from "lucide-react";
import { useAuth } from "@/features/auth/AuthProvider";
import { profilesApi } from "@/features/profiles/api";
import {
  useAddPortfolioItem,
  useMyProfile,
  useUpdateProfile,
  useUploadPortfolioAttachment,
} from "@/features/profiles/hooks";
import { createProfileSchema, type ProfileFormValues } from "@/features/profiles/schemas";
import { ErrorAlert } from "@/shared/components/Alert";
import { Badge } from "@/shared/components/Badge";
import { Button } from "@/shared/components/Button";
import { Card, CardHeader } from "@/shared/components/Card";
import { EmptyState } from "@/shared/components/EmptyState";
import { Field, Input, Select, Textarea } from "@/shared/components/FormControls";
import { Modal } from "@/shared/components/Modal";
import { PageHeader } from "@/shared/components/PageHeader";
import { PageLoader } from "@/shared/components/Spinner";
import { triggerDownload } from "@/shared/api/client";
import { isApiError } from "@/shared/api/errors";
import { useDocumentTitle } from "@/shared/hooks/useDocumentTitle";
import { availabilityOptions, availabilityLabels } from "@/shared/utils/labels";
import { formatMoney } from "@/shared/utils/format";

export function ProfilePage() {
  useDocumentTitle("Mi perfil");
  const { user } = useAuth();
  const profileQuery = useMyProfile();
  const updateProfile = useUpdateProfile();
  const addPortfolio = useAddPortfolioItem();
  const uploadAttachment = useUploadPortfolioAttachment();
  const [portfolioOpen, setPortfolioOpen] = useState(false);
  const [portfolioError, setPortfolioError] = useState<unknown>(null);
  const [attachmentError, setAttachmentError] = useState<unknown>(null);
  const [downloadError, setDownloadError] = useState<unknown>(null);
  const [newItem, setNewItem] = useState({ title: "", url: "", description: "" });
  const [uploadingId, setUploadingId] = useState<string | null>(null);
  const fileInputRefs = useRef<Record<string, HTMLInputElement | null>>({});

  const role = user?.role ?? "CLIENT";
  const schema = createProfileSchema(role);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<ProfileFormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      headline: "",
      bio: "",
      hourlyRate: "",
      currency: "USD",
      availability: "AVAILABLE",
      skills: "",
      companyName: "",
      description: "",
    },
  });

  const profile = profileQuery.data;
  const missingProfile = profileQuery.isError && isApiError(profileQuery.error) && profileQuery.error.status === 404;

  useEffect(() => {
    if (!profile) return;
    reset({
      headline: profile.headline ?? "",
      bio: profile.bio ?? "",
      hourlyRate: profile.hourlyRate !== null && profile.hourlyRate !== undefined ? String(profile.hourlyRate) : "",
      currency: profile.currency ?? "USD",
      availability: profile.availability ?? "AVAILABLE",
      skills: (profile.skills ?? []).map((skill) => skill.name).join(", "),
      companyName: profile.companyName ?? "",
      description: profile.description ?? "",
    });
  }, [profile, reset]);

  const onSubmit = async (values: ProfileFormValues) => {
    const skills = (values.skills ?? "")
      .split(",")
      .map((skill) => skill.trim())
      .filter(Boolean);

    if (role === "FREELANCER") {
      await updateProfile.mutateAsync({
        headline: values.headline?.trim(),
        bio: values.bio?.trim(),
        hourlyRate: values.hourlyRate ? Number(values.hourlyRate) : undefined,
        currency: values.currency || "USD",
        availability: values.availability,
        skills,
      });
    } else {
      await updateProfile.mutateAsync({
        companyName: values.companyName?.trim(),
        description: values.description?.trim(),
      });
    }
  };

  const handleAddPortfolio = async () => {
    setPortfolioError(null);
    try {
      await addPortfolio.mutateAsync({
        title: newItem.title.trim(),
        url: newItem.url.trim() || undefined,
        description: newItem.description.trim() || undefined,
      });
      setPortfolioOpen(false);
      setNewItem({ title: "", url: "", description: "" });
    } catch (error) {
      setPortfolioError(error);
    }
  };

  const handleFile = async (itemId: string, file: File | undefined) => {
    if (!file) return;
    setAttachmentError(null);
    setUploadingId(itemId);
    try {
      await uploadAttachment.mutateAsync({ itemId, file });
    } catch (error) {
      setAttachmentError(error);
    } finally {
      setUploadingId(null);
      const input = fileInputRefs.current[itemId];
      if (input) input.value = "";
    }
  };

  const handleDownload = async (itemId: string) => {
    setDownloadError(null);
    try {
      const { blob, filename } = await profilesApi.downloadAttachment(itemId);
      triggerDownload(blob, filename);
    } catch (error) {
      setDownloadError(error);
    }
  };

  if (user?.role === "ADMIN") {
    return (
      <div className="space-y-6">
        <PageHeader title="Mi perfil" description="La cuenta administrativa no gestiona perfil público." />
        <Card>
          <p className="text-sm text-ink-600">
            Las cuentas de administración no tienen perfil público. Usa la sección de administración
            para reportes, moderación y auditoría.
          </p>
        </Card>
      </div>
    );
  }

  if (profileQuery.isLoading) return <PageLoader label="Cargando perfil" />;
  if (profileQuery.isError && !missingProfile) {
    return <ErrorAlert error={profileQuery.error} title="No se pudo cargar el perfil" />;
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title="Mi perfil"
        description={
          missingProfile
            ? "Aún no tienes perfil. Completa los datos para que otros usuarios puedan conocerte."
            : "Estos datos se muestran a la contraparte en tus contratos y propuestas."
        }
        actions={
          profile && (
            <div className="flex items-center gap-2 rounded-xl bg-white px-3 py-2 text-sm shadow-sm ring-1 ring-ink-200">
              <Star className="size-4 fill-amber-400 text-amber-500" aria-hidden="true" />
              <span className="font-semibold">{profile.ratingAverage?.toFixed(1) ?? "0.0"}</span>
              <span className="text-ink-500">({profile.ratingCount} reseñas)</span>
            </div>
          )
        }
      />

      {missingProfile && (
        <ErrorAlert
          error={new Error("No existe un perfil todavía; al guardar se creará automáticamente.")}
          title="Perfil pendiente"
        />
      )}

      <div className="grid gap-6 lg:grid-cols-[1fr_340px]">
        <Card>
          <CardHeader
            title={role === "FREELANCER" ? "Perfil profesional" : "Perfil de empresa"}
            description="Los cambios se guardan y se reflejan de inmediato."
          />
          <form className="mt-5 space-y-4" onSubmit={handleSubmit(onSubmit)} noValidate>
            {updateProfile.isError ? <ErrorAlert error={updateProfile.error} /> : null}
            {updateProfile.isSuccess ? (
              <div className="rounded-xl border border-emerald-200 bg-emerald-50 p-3 text-sm text-emerald-900" role="status">
                Perfil guardado correctamente.
              </div>
            ) : null}

            {role === "FREELANCER" ? (
              <>
                <Field label="Titular profesional" htmlFor="headline" error={errors.headline?.message} required>
                  <Input
                    id="headline"
                    placeholder="Desarrolladora full-stack · React + Spring Boot"
                    invalid={Boolean(errors.headline)}
                    {...register("headline")}
                  />
                </Field>
                <Field label="Biografía" htmlFor="bio" error={errors.bio?.message}>
                  <Textarea id="bio" placeholder="Cuéntanos tu experiencia, especialidades y forma de trabajo." {...register("bio")} />
                </Field>
                <div className="grid gap-4 sm:grid-cols-3">
                  <Field label="Tarifa por hora" htmlFor="hourlyRate" error={errors.hourlyRate?.message}>
                    <Input id="hourlyRate" type="number" min="0" step="0.01" placeholder="45" {...register("hourlyRate")} />
                  </Field>
                  <Field label="Moneda" htmlFor="currency" error={errors.currency?.message}>
                    <Input id="currency" maxLength={3} placeholder="USD" {...register("currency")} />
                  </Field>
                  <Field label="Disponibilidad" htmlFor="availability" error={errors.availability?.message} required>
                    <Select id="availability" {...register("availability")}>
                      {availabilityOptions.map(([value, label]) => (
                        <option key={value} value={value}>
                          {label}
                        </option>
                      ))}
                    </Select>
                  </Field>
                </div>
                <Field
                  label="Habilidades"
                  htmlFor="skills"
                  hint="Sepáralas con comas. Se validan automáticamente."
                  error={errors.skills?.message}
                >
                  <Input id="skills" placeholder="React, TypeScript, Spring Boot" {...register("skills")} />
                </Field>
              </>
            ) : (
              <>
                <Field label="Nombre de la empresa" htmlFor="companyName" error={errors.companyName?.message} required>
                  <Input id="companyName" placeholder="Acme S.A." invalid={Boolean(errors.companyName)} {...register("companyName")} />
                </Field>
                <Field label="Descripción" htmlFor="description" error={errors.description?.message}>
                  <Textarea id="description" placeholder="Qué hace tu empresa y qué tipo de proyectos publica." {...register("description")} />
                </Field>
              </>
            )}

            <div className="flex justify-end">
              <Button type="submit" loading={isSubmitting || updateProfile.isPending}>
                Guardar perfil
              </Button>
            </div>
          </form>
        </Card>

        <div className="space-y-6">
          <Card>
            <CardHeader title="Vista previa" description="Así te verán en la plataforma." />
            <div className="mt-4 space-y-3">
              <div>
                <p className="text-lg font-semibold text-ink-950">
                  {profile?.displayName ?? user?.displayName}
                </p>
                <p className="text-sm text-ink-500">
                  {profile?.headline || profile?.companyName || "Perfil sin completar"}
                </p>
              </div>
              <div className="flex flex-wrap gap-2">
                <Badge tone="brand">{role === "FREELANCER" ? "Freelancer" : "Cliente"}</Badge>
                {profile?.availability ? (
                  <Badge tone="info">{availabilityLabels[profile.availability]}</Badge>
                ) : null}
              </div>
              {profile?.hourlyRate ? (
                <p className="text-sm text-ink-600">
                  Tarifa: <strong>{formatMoney(profile.hourlyRate, profile.currency ?? "USD")}</strong> / hora
                </p>
              ) : null}
              {profile?.skills?.length ? (
                <div className="flex flex-wrap gap-1.5 pt-1">
                  {profile.skills.map((skill) => (
                    <Badge key={skill.id}>{skill.name}</Badge>
                  ))}
                </div>
              ) : (
                <p className="text-sm text-ink-500">Aún no hay habilidades registradas.</p>
              )}
            </div>
          </Card>

          {role === "FREELANCER" && (
            <Card>
              <CardHeader
                title="Portafolio"
                description="Evidencias y adjuntos visibles en tu perfil."
                action={
                  <Button size="sm" variant="outline" icon={<Plus className="size-4" />} onClick={() => setPortfolioOpen(true)}>
                    Agregar
                  </Button>
                }
              />
              {attachmentError ? <ErrorAlert error={attachmentError} className="mt-3" /> : null}
              {downloadError ? <ErrorAlert error={downloadError} className="mt-3" /> : null}
              {profile?.portfolio?.length ? (
                <ul className="mt-4 space-y-3">
                  {profile.portfolio.map((item) => (
                    <li key={item.id} className="rounded-xl border border-ink-200 p-3">
                      <p className="font-medium text-ink-900">{item.title}</p>
                      {item.description && <p className="mt-1 text-sm text-ink-500">{item.description}</p>}
                      {item.url && (
                        <a
                          href={item.url}
                          target="_blank"
                          rel="noreferrer noopener"
                          className="mt-1 block truncate text-sm text-brand-700 hover:underline"
                        >
                          {item.url}
                        </a>
                      )}
                      <div className="mt-3 flex flex-wrap items-center gap-2">
                        <input
                          ref={(element) => {
                            fileInputRefs.current[item.id] = element;
                          }}
                          type="file"
                          className="sr-only"
                          id={`attachment-${item.id}`}
                          onChange={(event) => handleFile(item.id, event.target.files?.[0])}
                        />
                        <label
                          htmlFor={`attachment-${item.id}`}
                          className="inline-flex h-9 cursor-pointer items-center gap-2 rounded-xl border border-ink-300 bg-white px-3 text-sm font-semibold text-ink-800 hover:bg-ink-50"
                        >
                          <Upload className="size-4" aria-hidden="true" />
                          {uploadingId === item.id ? "Subiendo…" : "Adjuntar"}
                        </label>
                        <Button
                          size="sm"
                          variant="ghost"
                          icon={<Download className="size-4" />}
                          onClick={() => handleDownload(item.id)}
                        >
                          Descargar adjunto
                        </Button>
                      </div>
                    </li>
                  ))}
                </ul>
              ) : (
                <EmptyState
                  title="Portafolio vacío"
                  description="Agrega un proyecto o evidencia y adjunta un archivo."
                  action={
                    <Button size="sm" onClick={() => setPortfolioOpen(true)}>
                      Agregar primer elemento
                    </Button>
                  }
                />
              )}
            </Card>
          )}
        </div>
      </div>

      <Modal
        open={portfolioOpen}
        onClose={() => setPortfolioOpen(false)}
        title="Agregar al portafolio"
        description="El título es obligatorio. La URL y la descripción son opcionales."
        footer={
          <>
            <Button variant="outline" onClick={() => setPortfolioOpen(false)}>
              Cancelar
            </Button>
            <Button onClick={handleAddPortfolio} loading={addPortfolio.isPending} disabled={!newItem.title.trim()}>
              Guardar
            </Button>
          </>
        }
      >
        <div className="space-y-4">
          {portfolioError ? <ErrorAlert error={portfolioError} /> : null}
          <Field label="Título" htmlFor="portfolio-title" required>
            <Input
              id="portfolio-title"
              value={newItem.title}
              onChange={(event) => setNewItem((value) => ({ ...value, title: event.target.value }))}
            />
          </Field>
          <Field label="URL" htmlFor="portfolio-url">
            <Input
              id="portfolio-url"
              type="url"
              placeholder="https://…"
              value={newItem.url}
              onChange={(event) => setNewItem((value) => ({ ...value, url: event.target.value }))}
            />
          </Field>
          <Field label="Descripción" htmlFor="portfolio-description">
            <Textarea
              id="portfolio-description"
              value={newItem.description}
              onChange={(event) => setNewItem((value) => ({ ...value, description: event.target.value }))}
            />
          </Field>
        </div>
      </Modal>
    </div>
  );
}
