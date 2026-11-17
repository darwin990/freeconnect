import type { components } from "@/types/api";

type Schemas = components["schemas"];
type WithRequired<T, K extends keyof T> = Omit<T, K> & { [P in K]-?: NonNullable<T[P]> };

export type ProjectStatus = NonNullable<Schemas["ProjectView"]["status"]>;
export type ProjectCategory = NonNullable<Schemas["ProjectView"]["category"]>;
export type Modality = NonNullable<Schemas["ProjectView"]["modality"]>;
export type ProposalStatus = NonNullable<Schemas["ProposalView"]["status"]>;
export type ContractStatus = NonNullable<Schemas["ContractView"]["status"]>;
export type MilestoneStatus = NonNullable<Schemas["MilestoneView"]["status"]>;
export type DeliverableStatus = NonNullable<Schemas["DeliverableView"]["status"]>;
export type PaymentStatus = NonNullable<Schemas["PaymentView"]["status"]>;
export type Availability = NonNullable<Schemas["ProfileView"]["availability"]>;
export type NotificationType = NonNullable<Schemas["NotificationView"]["type"]>;
export type Role = NonNullable<Schemas["UserSummary"]["role"]>;
export type CommissionPlan = NonNullable<Schemas["AcceptProposalRequest"]["commissionPlan"]>;
export type MilestoneType = NonNullable<Schemas["AcceptProposalRequest"]["milestoneType"]>;

export type UserSummary = WithRequired<
  Schemas["UserSummary"],
  "id" | "email" | "displayName" | "role"
>;

export type UserView = WithRequired<Schemas["UserView"], "id" | "email" | "displayName" | "role" | "status">;

export type AuthResponse = Omit<
  WithRequired<Schemas["AuthResponse"], "accessToken" | "refreshToken" | "tokenType" | "expiresIn">,
  "user"
> & { user: UserSummary };

export type Project = WithRequired<
  Schemas["ProjectView"],
  "id" | "clientId" | "title" | "category" | "budgetAmount" | "currency" | "modality" | "status"
> & {
  description?: string | null;
  skills?: string[];
  deadline?: string | null;
  publishedAt?: string | null;
  createdAt?: string | null;
};

export type ProjectPage = Omit<
  WithRequired<
    Schemas["PageResponseProjectView"],
    "content" | "page" | "size" | "totalElements" | "totalPages" | "hasNext"
  >,
  "content"
> & { content: Project[] };

export type ProjectTemplate = Omit<
  WithRequired<Schemas["ProjectTemplateView"], "id" | "name" | "title" | "budget" | "currency">,
  "category" | "modality" | "description" | "skills"
> & {
  category: ProjectCategory;
  modality: Modality;
  description?: string | null;
  skills?: string[];
};

export type Skill = WithRequired<Schemas["SkillView"], "id" | "name"> & { category?: string | null };

export type PortfolioItem = WithRequired<Schemas["PortfolioItemView"], "id" | "title"> & {
  url?: string | null;
  description?: string | null;
};

export type Profile = Omit<
  WithRequired<
    Schemas["ProfileView"],
    "id" | "userId" | "type" | "displayName" | "ratingAverage" | "ratingCount"
  >,
  | "headline"
  | "bio"
  | "hourlyRate"
  | "currency"
  | "availability"
  | "companyName"
  | "description"
  | "skills"
  | "portfolio"
> & {
  headline?: string | null;
  bio?: string | null;
  hourlyRate?: number | null;
  currency?: string | null;
  availability?: Availability | null;
  companyName?: string | null;
  description?: string | null;
  skills?: Skill[];
  portfolio?: PortfolioItem[];
};

export type Proposal = WithRequired<
  Schemas["ProposalView"],
  "id" | "projectId" | "freelancerId" | "coverLetter" | "amount" | "currency" | "estimatedDays" | "status"
> & {
  evaluationScore?: number | null;
  evaluationNote?: string | null;
  createdAt?: string | null;
};

export type Money = WithRequired<Schemas["Money"], "amount" | "currency">;

export type Task = WithRequired<Schemas["TaskView"], "id" | "title" | "done">;

export type Comment = WithRequired<Schemas["CommentView"], "id" | "authorId" | "body" | "createdAt">;

export type Deliverable = Omit<
  WithRequired<Schemas["DeliverableView"], "id" | "title" | "status" | "progress">,
  "description" | "url" | "submittedAt" | "tasks" | "comments"
> & {
  description?: string | null;
  url?: string | null;
  submittedAt?: string | null;
  tasks?: Task[];
  comments?: Comment[];
};

export type Milestone = Omit<
  WithRequired<Schemas["MilestoneView"], "id" | "title" | "amount" | "position" | "status" | "progress">,
  "amount" | "description" | "dueDate" | "deliverables"
> & {
  amount: Money;
  description?: string | null;
  dueDate?: string | null;
  deliverables?: Deliverable[];
};

export type Contract = Omit<
  WithRequired<
    Schemas["ContractView"],
    | "id"
    | "projectId"
    | "proposalId"
    | "clientId"
    | "freelancerId"
    | "title"
    | "amount"
    | "commission"
    | "status"
    | "progress"
    | "approvedMilestones"
    | "milestones"
  >,
  | "amount"
  | "commission"
  | "scope"
  | "startDate"
  | "endDate"
  | "createdAt"
  | "updatedAt"
  | "completedAt"
  | "milestones"
> & {
  amount: Money;
  commission: Money;
  scope?: string | null;
  startDate?: string | null;
  endDate?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
  completedAt?: string | null;
  milestones: Milestone[];
};

export type Conversation = Omit<
  WithRequired<
    Schemas["ConversationView"],
    "id" | "contractId" | "clientId" | "freelancerId" | "status" | "unread" | "createdAt" | "updatedAt"
  >,
  "subject" | "lastMessageAt" | "messages"
> & {
  subject?: string | null;
  lastMessageAt?: string | null;
  messages?: Message[];
};

export type Message = WithRequired<Schemas["MessageView"], "id" | "senderId" | "body" | "sentAt"> & {
  readAt?: string | null;
};

export type Review = WithRequired<
  Schemas["ReviewView"],
  "id" | "contractId" | "authorId" | "targetId" | "rating" | "createdAt"
> & { comment?: string | null };

export type Payment = Omit<
  WithRequired<
    Schemas["PaymentView"],
    | "id"
    | "contractId"
    | "payerId"
    | "payeeId"
    | "amount"
    | "commission"
    | "netAmount"
    | "status"
    | "createdAt"
  >,
  | "amount"
  | "commission"
  | "netAmount"
  | "provider"
  | "providerReference"
  | "processedAt"
  | "failureReason"
> & {
  amount: Money;
  commission: Money;
  netAmount: Money;
  provider?: string | null;
  providerReference?: string | null;
  processedAt?: string | null;
  failureReason?: string | null;
};

export type Notification = WithRequired<
  Schemas["NotificationView"],
  "id" | "type" | "channel" | "title" | "body" | "status" | "attempts" | "createdAt"
> & {
  sentAt?: string | null;
  lastError?: string | null;
};

export type AdminReport = Omit<
  WithRequired<Schemas["AdminReport"], "report" | "metrics" | "generatedAt">,
  "metrics"
> & {
  metrics: Record<string, unknown>;
};

export type AuditLog = WithRequired<
  Schemas["AuditLogView"],
  "id" | "action" | "entityType" | "occurredAt"
> & {
  actorId?: string | null;
  entityId?: string | null;
  details?: string | null;
};

export type SuspendResult = WithRequired<Schemas["SuspendResult"], "id" | "status">;

export type RegisterPayload = WithRequired<
  Schemas["RegisterRequest"],
  "email" | "password" | "displayName" | "role"
>;
export type LoginPayload = WithRequired<Schemas["LoginRequest"], "email" | "password">;
export type ProjectPayload = WithRequired<Schemas["ProjectRequest"], "title" | "category" | "budget"> & {
  description?: string;
  currency?: string;
  modality?: Modality;
  deadline?: string;
  skills?: string[];
};
export type UpdateProfilePayload = Schemas["UpdateProfileRequest"];
export type SubmitProposalPayload = WithRequired<
  Schemas["SubmitProposalRequest"],
  "coverLetter" | "amount"
> & { currency?: string; estimatedDays?: number };
export type AcceptProposalPayload = Schemas["AcceptProposalRequest"];
export type CreatePortfolioPayload = WithRequired<Schemas["CreatePortfolioItemRequest"], "title"> & {
  url?: string;
  description?: string;
};
export type AddDeliverablePayload = WithRequired<Schemas["AddDeliverableRequest"], "title"> & {
  description?: string;
  url?: string;
};
export type ReleasePaymentPayload = Schemas["ReleasePaymentRequest"];
