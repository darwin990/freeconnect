import { expect, test } from "@playwright/test";
import { login, logout } from "./helpers";

/**
 * Smoke del panel de administración con la cuenta sembrada por el backend.
 * Verifica que las cuatro secciones responden y que la navegación por rol
 * no expone secciones de clientes/freelancers.
 */
test("panel de administración accesible y protegido por rol", async ({ page }) => {
  test.setTimeout(120_000);
  await login(page, { email: "admin@freeconnect.io", password: "Admin#FreeConnect2026" });

  await expect(page.getByRole("heading", { name: /Hola,/ })).toBeVisible();

  await page.goto("/app/admin/reportes");
  await expect(page.getByRole("heading", { name: "Reportes agregados" })).toBeVisible();
  await expect(page.getByText("No se pudieron cargar los reportes")).toHaveCount(0);

  await page.goto("/app/admin/moderacion");
  await expect(page.getByRole("heading", { name: "Moderación de proyectos" })).toBeVisible();

  await page.goto("/app/admin/usuarios");
  await expect(page.getByRole("heading", { name: "Suspensión de usuarios" })).toBeVisible();

  await page.goto("/app/admin/auditoria");
  await expect(page.getByRole("heading", { name: "Auditoría" })).toBeVisible();

  await page.goto("/app/mis-proyectos");
  await expect(page.getByText("No tienes permiso para ver esta sección")).toBeVisible();

  await logout(page);
});
