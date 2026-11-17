/**
 * API pública del design system.
 *
 * Los componentes canónicos viven en `src/shared/components` (para no romper
 * imports existentes); esta carpeta expone la superficie estable que deben usar
 * las pantallas nuevas y los prototipos.
 */
export { Alert, ErrorAlert } from "./Alert";
export { Avatar } from "./Avatar";
export { Badge, StatusBadge, toneForStatus } from "./Badge";
export type { BadgeTone } from "./Badge";
export { Button, buttonClasses } from "./Button";
export { Card, CardHeader } from "./Card";
export { EmptyState } from "./EmptyState";
export { Field, Input } from "./Input";
export { Modal, ConfirmDialog } from "./Modal";
export { Pagination } from "./Pagination";
export { Select } from "./Select";
export { Skeleton, CardSkeleton, ListSkeleton, DetailSkeleton } from "./Skeleton";
export { Spinner, PageLoader } from "./Spinner";
export { Tabs } from "./Tabs";
export type { TabItem } from "./Tabs";
export { Textarea } from "./Textarea";
export { ThemeToggle } from "./ThemeToggle";
export { ToastProvider, useToast } from "./Toast";
export type { ToastOptions, ToastTone } from "./Toast";
