import { expect, test } from "@playwright/test";
import { idFromUrl, login, logout, register, uniqueEmail } from "./helpers";

/**
 * Smoke E2E del ciclo de negocio principal sobre el backend real.
 *
 * Cubre: registro de cliente y freelancer, publicación de proyecto, propuesta,
 * aceptación con contrato, mensajería bidireccional, entregable, envío de hito,
 * aprobación, pago simulado, reseña y protección de rutas por rol.
 *
 * Requiere backend en http://localhost:8080/api/v1 con H2 en memoria. Cada
 * ejecución crea cuentas y proyectos nuevos con sufijo temporal.
 */
test("ciclo completo cliente-freelancer", async ({ page }) => {
  test.setTimeout(240_000);

  const suffix = Date.now();
  const projectTitle = `Proyecto E2E ${suffix}`;
  const projectDescription =
    "API REST para reservas con autenticación, pagos simulados y panel administrativo.";
  const coverLetter =
    "Tengo experiencia construyendo APIs REST con Spring Boot, PostgreSQL y pruebas automatizadas. " +
    "Puedo entregar el alcance completo en cuatro semanas con documentación y despliegue.";
  const clientMessage = `Hola, coordinemos el inicio del proyecto E2E ${suffix}.`;
  const freelancerMessage = `Confirmado, empiezo con el análisis y los primeros entregables.`;
  const deliverableTitle = `Evidencia E2E ${suffix}`;
  const reviewComment = `Excelente trabajo en el flujo E2E ${suffix}.`;

  const clientEmail = uniqueEmail("cliente.e2e");
  const freelancerEmail = uniqueEmail("freelancer.e2e");

  // 1. Cliente: registro, proyecto y publicación.
  await register(page, { displayName: "Cliente E2E", email: clientEmail, role: "CLIENT" });
  await expect(page.getByRole("heading", { name: /Hola, Cliente E2E/ })).toBeVisible();

  await page.goto("/app/proyectos/nuevo");
  await page.getByLabel(/^Título/).fill(projectTitle);
  await page.getByLabel(/^Descripción/).fill(projectDescription);
  await page.getByLabel(/^Presupuesto/).fill("1500");
  await page.getByRole("button", { name: "Crear borrador", exact: true }).click();

  await expect(page).toHaveURL(/\/proyectos\/[0-9a-f-]{36}$/);
  const projectId = idFromUrl(page);
  await expect(page.getByRole("heading", { name: projectTitle, level: 1 })).toBeVisible();
  await expect(page.getByText("Borrador", { exact: true })).toBeVisible();

  await page.getByRole("button", { name: "Publicar", exact: true }).click();
  await expect(page.getByRole("button", { name: "Pausar", exact: true })).toBeVisible();
  await expect(page.getByText("Publicado", { exact: true })).toBeVisible();

  await logout(page);

  // 2. Freelancer: registro y propuesta.
  await register(page, {
    displayName: "Freelancer E2E",
    email: freelancerEmail,
    role: "FREELANCER",
  });

  // El backend exige perfil profesional antes de aceptar propuestas.
  await page.goto("/app/perfil");
  await page
    .getByLabel(/^Titular profesional/)
    .fill("Desarrollador full-stack · React + Spring Boot");
  await page.getByLabel(/^Disponibilidad/).selectOption("AVAILABLE");
  await page.getByRole("button", { name: "Guardar perfil", exact: true }).click();
  await expect(page.getByText("Perfil guardado correctamente.")).toBeVisible();

  await page.goto(`/proyectos/${projectId}`);
  await expect(page.getByRole("heading", { name: projectTitle, level: 1 })).toBeVisible();
  await page.getByLabel(/^Carta de presentación/).fill(coverLetter);
  await page.getByRole("button", { name: "Enviar propuesta", exact: true }).click();
  // La propuesta se invalida y la tarjeta sustituye al formulario.
  await expect(page.getByRole("button", { name: "Retirar propuesta", exact: true })).toBeVisible();
  await expect(page.getByText("Enviada", { exact: true })).toBeVisible();

  await logout(page);

  // 3. Cliente: acepta la propuesta y se crea el contrato.
  await login(page, { email: clientEmail });
  await page.goto(`/app/propuestas?project=${projectId}`);
  await expect(page.getByRole("heading", { name: projectTitle, level: 1 })).toBeVisible();

  await page.getByRole("button", { name: "Aceptar y contratar", exact: true }).click();
  const acceptDialog = page.getByRole("dialog", { name: "Aceptar propuesta y crear contrato" });
  await expect(acceptDialog).toBeVisible();
  await acceptDialog.getByRole("button", { name: "Aceptar propuesta", exact: true }).click();

  await expect(page).toHaveURL(/\/app\/contratos\/[0-9a-f-]{36}$/);
  const contractId = idFromUrl(page);
  const contractUrl = `/app/contratos/${contractId}`;
  await expect(page.getByRole("heading", { name: projectTitle, level: 1 })).toBeVisible();
  await expect(page.getByText("Eres el cliente", { exact: true })).toBeVisible();
  await expect(page.getByText("Activo", { exact: true })).toBeVisible();

  // 4. Cliente: abre conversación y envía el primer mensaje.
  await page.getByRole("button", { name: "Abrir conversación", exact: true }).click();
  await expect(page).toHaveURL(/\/app\/mensajes\/[0-9a-f-]{36}$/);
  const conversationId = idFromUrl(page);
  const conversationUrl = `/app/mensajes/${conversationId}`;

  await page.getByLabel(/^Nuevo mensaje/).fill(clientMessage);
  await page.getByRole("button", { name: "Enviar mensaje", exact: true }).click();
  await expect(page.getByText(clientMessage, { exact: true })).toBeVisible();

  await page.goto(contractUrl);
  await logout(page);

  // 5. Freelancer: responde, registra entregable y envía el hito a revisión.
  await login(page, { email: freelancerEmail });
  await page.goto(conversationUrl);
  await expect(page.getByText(clientMessage, { exact: true })).toBeVisible();
  await page.getByLabel(/^Nuevo mensaje/).fill(freelancerMessage);
  await page.getByRole("button", { name: "Enviar mensaje", exact: true }).click();
  await expect(page.getByText(freelancerMessage, { exact: true })).toBeVisible();

  await page.goto(contractUrl);
  await expect(page.getByText("Eres el freelancer", { exact: true })).toBeVisible();
  await page.getByRole("button", { name: "Agregar entregable", exact: true }).click();
  const deliverableDialog = page.getByRole("dialog", { name: "Registrar entregable" });
  await expect(deliverableDialog).toBeVisible();
  await deliverableDialog.getByLabel(/^Título/).fill(deliverableTitle);
  await deliverableDialog
    .getByLabel(/^Descripción/)
    .fill("Repositorio con la API, pruebas y colección de requests.");
  await deliverableDialog.getByRole("button", { name: "Guardar entregable", exact: true }).click();
  await expect(deliverableDialog).toBeHidden();
  await expect(page.getByText(deliverableTitle, { exact: true })).toBeVisible();

  await page.getByRole("button", { name: "Enviar a revisión", exact: true }).click();
  await expect(page.getByText("En revisión", { exact: true })).toBeVisible();

  // Protección de rutas por rol: un freelancer no ve "Mis proyectos".
  await page.goto("/app/mis-proyectos");
  await expect(page.getByText("No tienes permiso para ver esta sección")).toBeVisible();

  // 6. Cliente: aprueba el hito (contrato completado), paga y reseña.
  await logout(page);
  await login(page, { email: clientEmail });
  await page.goto(contractUrl);
  await expect(page.getByRole("button", { name: "Aprobar hito", exact: true })).toBeVisible();
  await page.getByRole("button", { name: "Aprobar hito", exact: true }).click();
  await expect(page.getByText("Completado", { exact: true })).toBeVisible();

  await page.getByRole("button", { name: "Liberar pago", exact: true }).click();
  await expect(page.getByText("Pago registrado correctamente.")).toBeVisible();
  await expect(page.getByText("Liberado", { exact: true })).toBeVisible();

  await page.getByLabel(/^Comentario/).fill(reviewComment);
  await page.getByRole("button", { name: "Enviar reseña", exact: true }).click();
  await expect(page.getByText(reviewComment, { exact: true })).toBeVisible();
  await expect(page.getByText("Ya enviaste tu reseña para este contrato.")).toBeVisible();
});
