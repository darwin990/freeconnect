import { Navigate, Route, Routes } from "react-router-dom";
import { AppShell } from "@/app/AppShell";
import { NotFoundPage } from "@/app/NotFoundPage";
import { PublicLayout } from "@/app/PublicLayout";
import { AdminAuditPage } from "@/features/admin/pages/AdminAuditPage";
import { AdminModerationPage } from "@/features/admin/pages/AdminModerationPage";
import { AdminReportsPage } from "@/features/admin/pages/AdminReportsPage";
import { AdminUsersPage } from "@/features/admin/pages/AdminUsersPage";
import { LoginPage } from "@/features/auth/pages/LoginPage";
import { RegisterPage } from "@/features/auth/pages/RegisterPage";
import { PublicOnlyRoute, RequireAuth, RequireRole } from "@/features/auth/guards";
import { ContractDetailPage } from "@/features/contracts/pages/ContractDetailPage";
import { ContractsPage } from "@/features/contracts/pages/ContractsPage";
import { DashboardPage } from "@/features/dashboard/DashboardPage";
import { LandingPage } from "@/features/landing/LandingPage";
import { ConversationDetailPage } from "@/features/messaging/pages/ConversationDetailPage";
import { ConversationsPage } from "@/features/messaging/pages/ConversationsPage";
import { NotificationsPage } from "@/features/notifications/pages/NotificationsPage";
import { PaymentsPage } from "@/features/payments/pages/PaymentsPage";
import { ProfilePage } from "@/features/profiles/pages/ProfilePage";
import { MyProjectsPage } from "@/features/projects/pages/MyProjectsPage";
import { ProjectDetailPage } from "@/features/projects/pages/ProjectDetailPage";
import { ProjectFormPage } from "@/features/projects/pages/ProjectFormPage";
import { ProjectsPage } from "@/features/projects/pages/ProjectsPage";
import { ProposalsPage } from "@/features/proposals/pages/ProposalsPage";
import { ReviewsPage } from "@/features/reviews/pages/ReviewsPage";

const PARTICIPANTS = ["CLIENT", "FREELANCER"] as const;

export function AppRoutes() {
  return (
    <Routes>
      <Route element={<PublicLayout />}>
        <Route index element={<LandingPage />} />
        <Route path="proyectos" element={<ProjectsPage />} />
        <Route path="proyectos/:id" element={<ProjectDetailPage />} />
      </Route>

      <Route
        path="login"
        element={
          <PublicOnlyRoute>
            <LoginPage />
          </PublicOnlyRoute>
        }
      />
      <Route
        path="registro"
        element={
          <PublicOnlyRoute>
            <RegisterPage />
          </PublicOnlyRoute>
        }
      />

      <Route
        path="app"
        element={
          <RequireAuth>
            <AppShell />
          </RequireAuth>
        }
      >
        <Route index element={<DashboardPage />} />
        <Route path="perfil" element={<ProfilePage />} />
        <Route path="notificaciones" element={<NotificationsPage />} />

        <Route
          path="mis-proyectos"
          element={
            <RequireRole roles={["CLIENT"]}>
              <MyProjectsPage />
            </RequireRole>
          }
        />
        <Route
          path="proyectos/nuevo"
          element={
            <RequireRole roles={["CLIENT"]}>
              <ProjectFormPage />
            </RequireRole>
          }
        />
        <Route
          path="proyectos/:id/editar"
          element={
            <RequireRole roles={["CLIENT"]}>
              <ProjectFormPage />
            </RequireRole>
          }
        />
        <Route
          path="propuestas"
          element={
            <RequireRole roles={["CLIENT"]}>
              <ProposalsPage />
            </RequireRole>
          }
        />

        <Route
          path="contratos"
          element={
            <RequireRole roles={[...PARTICIPANTS]}>
              <ContractsPage />
            </RequireRole>
          }
        />
        <Route
          path="contratos/:id"
          element={
            <RequireRole roles={[...PARTICIPANTS]}>
              <ContractDetailPage />
            </RequireRole>
          }
        />
        <Route
          path="pagos"
          element={
            <RequireRole roles={[...PARTICIPANTS]}>
              <PaymentsPage />
            </RequireRole>
          }
        />
        <Route
          path="resenas"
          element={
            <RequireRole roles={[...PARTICIPANTS]}>
              <ReviewsPage />
            </RequireRole>
          }
        />
        <Route
          path="mensajes"
          element={
            <RequireRole roles={[...PARTICIPANTS]}>
              <ConversationsPage />
            </RequireRole>
          }
        />
        <Route
          path="mensajes/:id"
          element={
            <RequireRole roles={[...PARTICIPANTS]}>
              <ConversationDetailPage />
            </RequireRole>
          }
        />

        <Route path="admin">
          <Route index element={<Navigate to="reportes" replace />} />
          <Route
            path="reportes"
            element={
              <RequireRole roles={["ADMIN"]}>
                <AdminReportsPage />
              </RequireRole>
            }
          />
          <Route
            path="moderacion"
            element={
              <RequireRole roles={["ADMIN"]}>
                <AdminModerationPage />
              </RequireRole>
            }
          />
          <Route
            path="usuarios"
            element={
              <RequireRole roles={["ADMIN"]}>
                <AdminUsersPage />
              </RequireRole>
            }
          />
          <Route
            path="auditoria"
            element={
              <RequireRole roles={["ADMIN"]}>
                <AdminAuditPage />
              </RequireRole>
            }
          />
        </Route>
      </Route>

      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}
