import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { Pagination } from "@/shared/components/Pagination";
import { EmptyState } from "@/shared/components/EmptyState";

describe("Pagination", () => {
  it("deshabilita anterior en la primera página y permite avanzar", async () => {
    const onPageChange = vi.fn();
    render(<Pagination page={0} totalPages={3} totalElements={30} onPageChange={onPageChange} />);

    expect(screen.getByRole("button", { name: /anterior/i })).toBeDisabled();
    await userEvent.click(screen.getByRole("button", { name: /siguiente/i }));

    expect(onPageChange).toHaveBeenCalledWith(1);
  });

  it("muestra el total cuando solo hay una página", () => {
    render(<Pagination page={0} totalPages={1} totalElements={4} onPageChange={() => undefined} label="proyectos" />);

    expect(screen.getByText("4 proyectos")).toBeInTheDocument();
  });
});

describe("EmptyState", () => {
  it("muestra título, descripción y acción", () => {
    render(
      <EmptyState
        title="Sin elementos"
        description="Crea el primero para empezar."
        action={<button type="button">Crear</button>}
      />,
    );

    expect(screen.getByRole("heading", { name: "Sin elementos" })).toBeInTheDocument();
    expect(screen.getByText("Crea el primero para empezar.")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Crear" })).toBeInTheDocument();
  });
});
