import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import { App } from "@/App";
import { ErrorBoundary } from "@/app/ErrorBoundary";
import { AppProviders } from "@/app/providers";
import "@/index.css";

const container = document.getElementById("root");
if (!container) throw new Error("No se encontró el contenedor #root");

createRoot(container).render(
  <StrictMode>
    <BrowserRouter>
      <AppProviders>
        <ErrorBoundary>
          <App />
        </ErrorBoundary>
      </AppProviders>
    </BrowserRouter>
  </StrictMode>,
);
