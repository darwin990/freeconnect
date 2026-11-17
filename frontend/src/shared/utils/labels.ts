import type {
  Availability,
  CommissionPlan,
  ContractStatus,
  DeliverableStatus,
  MilestoneStatus,
  MilestoneType,
  Modality,
  NotificationType,
  PaymentStatus,
  ProjectCategory,
  ProjectStatus,
  ProposalStatus,
  Role,
} from "@/types/models";

export const roleLabels: Record<Role, string> = {
  CLIENT: "Cliente",
  FREELANCER: "Freelancer",
  ADMIN: "Administrador",
};

export const projectStatusLabels: Record<ProjectStatus, string> = {
  DRAFT: "Borrador",
  PUBLISHED: "Publicado",
  PAUSED: "Pausado",
  IN_PROGRESS: "En progreso",
  FINISHED: "Finalizado",
  CANCELLED: "Cancelado",
};

export const categoryLabels: Record<ProjectCategory, string> = {
  SOFTWARE: "Software",
  DESIGN: "Diseño",
  MARKETING: "Marketing",
  WRITING: "Redacción",
  DATA: "Datos",
  OTHER: "Otro",
};

export const modalityLabels: Record<Modality, string> = {
  REMOTE: "Remoto",
  ONSITE: "Presencial",
  HYBRID: "Híbrido",
};

export const proposalStatusLabels: Record<ProposalStatus, string> = {
  SUBMITTED: "Enviada",
  WITHDRAWN: "Retirada",
  ACCEPTED: "Aceptada",
  REJECTED: "Rechazada",
};

export const contractStatusLabels: Record<ContractStatus, string> = {
  DRAFT: "Borrador",
  ACTIVE: "Activo",
  COMPLETED: "Completado",
  CANCELLED: "Cancelado",
};

export const milestoneStatusLabels: Record<MilestoneStatus, string> = {
  PENDING: "Pendiente",
  IN_PROGRESS: "En progreso",
  SUBMITTED: "En revisión",
  APPROVED: "Aprobado",
  REJECTED: "Rechazado",
};

export const deliverableStatusLabels: Record<DeliverableStatus, string> = {
  DRAFT: "Borrador",
  SUBMITTED: "Enviado",
  APPROVED: "Aprobado",
  REJECTED: "Rechazado",
};

export const paymentStatusLabels: Record<PaymentStatus, string> = {
  PENDING: "Pendiente",
  RELEASED: "Liberado",
  FAILED: "Fallido",
  REFUNDED: "Reembolsado",
};

export const availabilityLabels: Record<Availability, string> = {
  AVAILABLE: "Disponible",
  PART_TIME: "Tiempo parcial",
  UNAVAILABLE: "No disponible",
};

export const commissionLabels: Record<CommissionPlan, string> = {
  STANDARD: "Estándar (10%)",
  PREMIUM: "Premium (5%)",
};

export const milestoneTypeLabels: Record<MilestoneType, string> = {
  SINGLE: "Pago único",
  EQUAL_SPLIT: "Reparto igual",
  CUSTOM: "Personalizado",
};

export const notificationTypeLabels: Record<NotificationType, string> = {
  PROPOSAL_RECEIVED: "Propuesta recibida",
  PROPOSAL_ACCEPTED: "Propuesta aceptada",
  CONTRACT_CREATED: "Contrato creado",
  CONTRACT_COMPLETED: "Contrato completado",
  MESSAGE_RECEIVED: "Mensaje recibido",
  MILESTONE_SUBMITTED: "Hito enviado",
  MILESTONE_APPROVED: "Hito aprobado",
  REVIEW_RECEIVED: "Reseña recibida",
  PAYMENT_RELEASED: "Pago liberado",
};

export const categoryOptions = Object.entries(categoryLabels) as [ProjectCategory, string][];
export const modalityOptions = Object.entries(modalityLabels) as [Modality, string][];
export const projectStatusOptions = Object.entries(projectStatusLabels) as [ProjectStatus, string][];
export const availabilityOptions = Object.entries(availabilityLabels) as [Availability, string][];
export const commissionOptions = Object.entries(commissionLabels) as [CommissionPlan, string][];
export const milestoneTypeOptions = Object.entries(milestoneTypeLabels) as [MilestoneType, string][];
export const roleOptions: Role[] = ["CLIENT", "FREELANCER"];
