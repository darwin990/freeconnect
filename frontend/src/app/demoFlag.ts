/**
 * Bandera de la galería `/demo`.
 *
 * La demo visual es útil durante el desarrollo y en presentaciones, pero no
 * debe aparecer en una entrega de producción salvo que se habilite de forma
 * explícita con `VITE_ENABLE_DEMO=true` en el build.
 */
export const demoEnabled =
  import.meta.env.DEV || import.meta.env.VITE_ENABLE_DEMO === "true";
