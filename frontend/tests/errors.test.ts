import { describe, expect, it } from "vitest";
import { ApiError, errorMessage, parseApiError } from "@/shared/api/errors";

function jsonResponse(body: unknown, status = 400) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}

describe("parseApiError", () => {
  it("interpreta el contrato propio del backend", async () => {
    const error = await parseApiError(
      jsonResponse(
        {
          timestamp: "2026-01-01T00:00:00Z",
          status: 422,
          code: "VALIDATION_ERROR",
          message: "Datos inválidos",
          path: "/api/v1/projects",
          details: [{ field: "title", message: "El título es obligatorio" }],
        },
        422,
      ),
    );

    expect(error).toBeInstanceOf(ApiError);
    expect(error.status).toBe(422);
    expect(error.code).toBe("VALIDATION_ERROR");
    expect(error.message).toBe("Datos inválidos");
    expect(error.details).toEqual([{ field: "title", message: "El título es obligatorio" }]);
  });

  it("interpreta application/problem+json (RFC 7807)", async () => {
    const error = await parseApiError(
      new Response(
        JSON.stringify({
          type: "about:blank",
          title: "Bad Request",
          status: 400,
          detail: "Falta un campo obligatorio",
          instance: "/api/v1/projects",
          errors: [{ field: "budget", message: "Debe ser mayor que cero" }],
        }),
        { status: 400, headers: { "Content-Type": "application/problem+json" } },
      ),
    );

    expect(error.status).toBe(400);
    expect(error.code).toBe("HTTP_400");
    expect(error.message).toBe("Falta un campo obligatorio");
    expect(error.details).toEqual([{ field: "budget", message: "Debe ser mayor que cero" }]);
  });

  it("usa el texto plano cuando la respuesta no es JSON", async () => {
    const error = await parseApiError(new Response("Servicio no disponible", { status: 503 }));

    expect(error.status).toBe(503);
    expect(error.message).toBe("Servicio no disponible");
  });

  it("genera un mensaje por defecto si no hay cuerpo", async () => {
    const error = await parseApiError(new Response("", { status: 500 }));

    expect(error.message).toBe("La solicitud falló (500).");
  });
});

describe("errorMessage", () => {
  it("traduce errores de red", () => {
    expect(errorMessage(new ApiError(0, "NETWORK_ERROR", "fallo"))).toContain("No se pudo contactar");
  });

  it("respeta el mensaje de un Error normal", () => {
    expect(errorMessage(new Error("algo pasó"))).toBe("algo pasó");
  });
});
