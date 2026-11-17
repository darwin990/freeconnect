import { Link } from "react-router-dom";
import { Star } from "lucide-react";
import { useAuth } from "@/features/auth/AuthProvider";
import { useUserReviews } from "@/features/reviews/hooks";
import { ErrorAlert } from "@/shared/components/Alert";
import { EmptyState } from "@/shared/components/EmptyState";
import { PageHeader } from "@/shared/components/PageHeader";
import { CardSkeleton } from "@/shared/components/Spinner";
import { useDocumentTitle } from "@/shared/hooks/useDocumentTitle";
import { formatDateTime, shortId } from "@/shared/utils/format";

export function ReviewsPage() {
  useDocumentTitle("Reseñas");
  const { user } = useAuth();
  const query = useUserReviews(user?.id);

  const reviews = query.data ?? [];
  const average = reviews.length
    ? reviews.reduce((total, review) => total + review.rating, 0) / reviews.length
    : 0;

  return (
    <div className="space-y-6">
      <PageHeader
        title="Reseñas recibidas"
        description="Las reseñas se generan al completar un contrato y solo pueden emitirlas sus participantes."
      />

      {query.isError ? (
        <ErrorAlert error={query.error} title="No se pudieron cargar las reseñas" />
      ) : query.isLoading ? (
        <div className="space-y-3">
          {Array.from({ length: 3 }).map((_, index) => (
            <CardSkeleton key={index} />
          ))}
        </div>
      ) : reviews.length === 0 ? (
        <EmptyState
          title="Aún no tienes reseñas"
          description="Cuando completes un contrato, la contraparte podrá dejarte una reseña."
          icon={<Star className="size-6" aria-hidden="true" />}
        />
      ) : (
        <>
          <div className="flex flex-wrap items-center gap-4 rounded-2xl border border-ink-200 bg-white p-5 shadow-soft">
            <div className="flex size-16 flex-col items-center justify-center rounded-xl bg-amber-50 text-amber-700">
              <span className="text-2xl font-bold">{average.toFixed(1)}</span>
              <span className="text-[10px] font-semibold uppercase">de 5</span>
            </div>
            <div>
              <p className="text-sm font-semibold text-ink-900">{reviews.length} reseña(s) recibida(s)</p>
              <div className="mt-1 flex items-center gap-1 text-amber-500">
                {Array.from({ length: 5 }).map((_, index) => (
                  <Star
                    key={index}
                    className={`size-4 ${index < Math.round(average) ? "fill-amber-400" : "text-ink-300"}`}
                    aria-hidden="true"
                  />
                ))}
              </div>
            </div>
          </div>

          <ul className="space-y-3">
            {reviews.map((review) => (
              <li key={review.id} className="rounded-2xl border border-ink-200 bg-white p-5 shadow-soft">
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div className="flex items-center gap-1 text-amber-500" aria-label={`${review.rating} de 5 estrellas`}>
                    {Array.from({ length: 5 }).map((_, index) => (
                      <Star
                        key={index}
                        className={`size-4 ${index < review.rating ? "fill-amber-400" : "text-ink-300"}`}
                        aria-hidden="true"
                      />
                    ))}
                    <span className="ml-2 text-sm font-semibold text-ink-900">{review.rating} / 5</span>
                  </div>
                  <span className="text-xs text-ink-500">{formatDateTime(review.createdAt)}</span>
                </div>
                {review.comment ? (
                  <p className="mt-3 whitespace-pre-line text-sm text-ink-700">{review.comment}</p>
                ) : (
                  <p className="mt-3 text-sm italic text-ink-500">Sin comentario.</p>
                )}
                <div className="mt-3 flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-ink-500">
                  <span>Autor: {shortId(review.authorId)}</span>
                  <Link
                    to={`/app/contratos/${review.contractId}`}
                    className="font-semibold text-brand-700 hover:underline"
                  >
                    Ver contrato {shortId(review.contractId)}
                  </Link>
                </div>
              </li>
            ))}
          </ul>
        </>
      )}
    </div>
  );
}
