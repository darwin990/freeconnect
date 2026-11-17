import { describe, expect, it } from "vitest";
import { humanizeKey } from "@/shared/utils/labels";

describe("humanizeKey", () => {
  it("traduce claves de métricas conocidas en español", () => {
    expect(humanizeKey("inProgress")).toBe("En progreso");
    expect(humanizeKey("PUBLISHED")).toBe("Publicados");
    expect(humanizeKey("Clients")).toBe("Clientes");
    expect(humanizeKey("volumeByCurrency")).toBe("Volumen por moneda");
    expect(humanizeKey("average_rating")).toBe("Average rating");
  });

  it("separa camelCase y guiones bajos cuando no hay traducción", () => {
    expect(humanizeKey("totalRevenue")).toBe("Total Revenue");
    expect(humanizeKey("open_tickets")).toBe("Open tickets");
  });

  it("devuelve un guion para claves vacías", () => {
    expect(humanizeKey("   ")).toBe("—");
  });
});
