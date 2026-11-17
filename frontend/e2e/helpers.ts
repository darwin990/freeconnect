import { expect, type Page } from "@playwright/test";

/**
 * Contraseña compartida por las cuentas efímeras del smoke test.
 * Cumple la política del backend: 12+ caracteres y al menos un dígito.
 */
export const E2E_PASSWORD = "E2ePassword#2026";

export type ParticipantRole = "CLIENT" | "FREELANCER";

export function uniqueEmail(prefix: string): string {
  const run = `${Date.now()}.${Math.random().toString(36).slice(2, 8)}`;
  return `${prefix}.${run}@example.com`;
}

export async function register(
  page: Page,
  options: { displayName: string; email: string; role?: ParticipantRole },
): Promise<void> {
  const role = options.role ?? "CLIENT";
  await page.goto("/registro");
  await expect(page.getByRole("heading", { name: "Crea tu cuenta" })).toBeVisible();

  if (role === "FREELANCER") {
    await page.getByText("Freelancer", { exact: true }).click();
    await expect(page.getByRole("radio", { name: /Freelancer/ })).toBeChecked();
  }

  await page.getByLabel(/^Nombre visible/).fill(options.displayName);
  await page.getByLabel(/^Correo electrónico/).fill(options.email);
  await page.getByLabel(/^Contraseña/).fill(E2E_PASSWORD);
  await page.getByLabel(/^Confirmar contraseña/).fill(E2E_PASSWORD);
  await page.getByRole("button", { name: "Crear cuenta", exact: true }).click();

  await expect(page).toHaveURL(/\/app$/);
}

export async function login(
  page: Page,
  options: { email: string; password?: string },
): Promise<void> {
  await page.goto("/login");
  await expect(page.getByRole("heading", { name: "Bienvenido de nuevo" })).toBeVisible();
  await page.getByLabel(/^Correo electrónico/).fill(options.email);
  await page.getByLabel(/^Contraseña/).fill(options.password ?? E2E_PASSWORD);
  await page.getByRole("button", { name: "Entrar", exact: true }).click();
  await expect(page).toHaveURL(/\/app$/);
}

export async function logout(page: Page): Promise<void> {
  // El botón solo existe en el layout autenticado; desde páginas públicas hay
  // que volver al panel antes de cerrar sesión. Si la sesión ya se perdió, la
  // app redirige a /login y no hay nada que cerrar.
  await page.goto("/app");
  const logoutButton = page.getByRole("button", { name: "Cerrar sesión", exact: true });

  await expect(async () => {
    if (/\/login$/.test(new URL(page.url()).pathname)) return;
    await expect(logoutButton).toBeVisible({ timeout: 1_000 });
  }).toPass({ timeout: 30_000 });

  if (/\/login$/.test(new URL(page.url()).pathname)) return;

  await logoutButton.click();
  await expect(page).toHaveURL(/\/login$/);
}

export function idFromUrl(page: Page): string {
  const segments = new URL(page.url()).pathname.split("/").filter(Boolean);
  const id = segments.at(-1);
  if (!id) throw new Error(`No se pudo extraer un id de la URL ${page.url()}`);
  return id;
}

export async function expectNoErrorAlert(page: Page): Promise<void> {
  await expect(page.getByRole("alert")).toHaveCount(0);
}
