# Design system del prototipo

Esta carpeta es la **superficie pública** de componentes reutilizables. Los
componentes canónicos siguen viviendo en `src/shared/components` para no romper
los imports existentes; aquí se re-exportan y se añaden los componentes nuevos
(Avatar, Tabs, Toast, ThemeToggle).

## Inventario

| Componente | Variantes / notas |
|------------|-------------------|
| `Button` | `primary`, `secondary`, `outline`, `ghost`, `danger`, `subtle`; tamaños `sm`, `md`, `lg`, `icon`. |
| `Input`, `Textarea`, `Select`, `Field` | Foco visible, estado `invalid`, `aria-invalid`, error con `role="alert"`. |
| `Card`, `CardHeader` | `default`, `muted`, `interactive`. |
| `Badge`, `StatusBadge` | Tonos semánticos y punto de estado. |
| `Avatar` | Iniciales con color estable; `src` opcional. |
| `Modal`, `ConfirmDialog` | Trampa de foco, Escape, `aria-modal`, animación y bottom-sheet en móvil. |
| `ToastProvider`, `useToast` | Toasts `success`, `error`, `warning`, `info`; `role="status"`. |
| `Skeleton`, `CardSkeleton`, `ListSkeleton`, `DetailSkeleton` | Carga con shimmer. |
| `EmptyState` | Ilustración SVG, icono, texto y CTA. |
| `Spinner`, `PageLoader` | Carga inline y de página. |
| `Tabs` | Tablist con flechas, Home y End. |
| `Pagination` | Paginación accesible. |
| `ThemeToggle` | Tema claro/oscuro (mínimo) persistido en localStorage. |

## Tokens

Definidos en `tailwind.config.js` e `src/index.css`:

- Colores: `brand` (primario), `accent` (secundario), `ink` (grises), `success`, `warning`, `danger`, `info`.
- Radios: `xl`, `2xl`, `3xl`, `4xl`.
- Sombras: `soft`, `card`, `float`, `glow`.
- Tipografía: `Inter` con fallback al sistema; jerarquías en `PageHeader` y `CardHeader`.
- Movimiento: 150–250 ms con `ease-smooth`; respeta `prefers-reduced-motion`.

## Reglas

- No uses emojis como iconos: usa `lucide-react`.
- Todo control interactivo debe tener foco visible, label asociado y estado `disabled` claro.
- Los textos nuevos van en español y sin jerga innecesaria.
- Si un componente necesita lógica de negocio, no pertenece aquí.
