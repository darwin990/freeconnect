export interface ApiProblemDetail {
  field?: string;
  message?: string;
}

export interface ApiProblem {
  timestamp?: string;
  status?: number;
  code?: string;
  message?: string;
  title?: string;
  detail?: string;
  type?: string;
  instance?: string;
  path?: string;
  errors?: ApiProblemDetail[];
  details?: ApiProblemDetail[];
}

export class ApiError extends Error {
  readonly status: number;
  readonly code: string;
  readonly details: ApiProblemDetail[];
  readonly problem?: ApiProblem;

  constructor(status: number, code: string, message: string, problem?: ApiProblem) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.code = code;
    this.problem = problem;
    this.details = problem?.details ?? problem?.errors ?? [];
  }
}

export function isApiError(error: unknown): error is ApiError {
  return error instanceof ApiError;
}

export function errorMessage(error: unknown, fallback = "Ocurrió un error inesperado."): string {
  if (error instanceof ApiError) {
    if (error.status === 0) {
      return "No se pudo contactar con el servidor. Comprueba que la API esté disponible.";
    }
    return error.message || fallback;
  }
  if (error instanceof Error) return error.message || fallback;
  return fallback;
}

export async function parseApiError(response: Response): Promise<ApiError> {
  const raw = await response.text().catch(() => "");
  let problem: ApiProblem | undefined;
  if (raw) {
    try {
      problem = JSON.parse(raw) as ApiProblem;
    } catch {
      problem = undefined;
    }
  }

  const message =
    problem?.detail ??
    problem?.message ??
    problem?.title ??
    (raw && !problem ? raw.slice(0, 400) : "") ??
    "";

  return new ApiError(
    response.status,
    problem?.code ?? `HTTP_${response.status}`,
    message || `La solicitud falló (${response.status}).`,
    problem,
  );
}
