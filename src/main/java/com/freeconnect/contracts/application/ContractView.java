package com.freeconnect.contracts.application;

import com.freeconnect.contracts.domain.Contract;
import com.freeconnect.contracts.domain.Deliverable;
import com.freeconnect.contracts.domain.DeliverableComment;
import com.freeconnect.contracts.domain.DeliverableStatus;
import com.freeconnect.contracts.domain.Milestone;
import com.freeconnect.contracts.domain.MilestoneStatus;
import com.freeconnect.contracts.domain.composite.TaskItem;
import com.freeconnect.contracts.domain.ContractStatus;
import com.freeconnect.shared.domain.Money;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Vista de salida de un contrato con su jerarquia de hitos y entregables.
 */
public record ContractView(UUID id, UUID projectId, UUID proposalId, UUID clientId, UUID freelancerId,
                           String title, String scope, Money amount, Money commission, LocalDate startDate,
                           LocalDate endDate, ContractStatus status, int progress, long approvedMilestones,
                           Instant createdAt, Instant updatedAt, Instant completedAt,
                           List<MilestoneView> milestones) {

    public static ContractView from(Contract contract) {
        return new ContractView(contract.id(), contract.projectId(), contract.proposalId(), contract.clientId(),
                contract.freelancerId(), contract.title(), contract.scope(), contract.amount(),
                contract.commission(), contract.startDate(), contract.endDate(), contract.status(),
                contract.progress(), contract.approvedMilestones(), contract.createdAt(), contract.updatedAt(),
                contract.completedAt(), contract.milestones().stream().map(MilestoneView::from).toList());
    }

    public record MilestoneView(UUID id, String title, String description, Money amount, LocalDate dueDate,
                                int position, MilestoneStatus status, int progress,
                                List<DeliverableView> deliverables) {

        static MilestoneView from(Milestone milestone) {
            return new MilestoneView(milestone.id(), milestone.title(), milestone.description(),
                    milestone.amount(), milestone.dueDate(), milestone.position(), milestone.status(),
                    milestone.progress(), milestone.deliverables().stream().map(DeliverableView::from).toList());
        }
    }

    public record DeliverableView(UUID id, String title, String description, String url,
                                  DeliverableStatus status, Instant submittedAt, int progress,
                                  List<TaskView> tasks, List<CommentView> comments) {

        static DeliverableView from(Deliverable deliverable) {
            return new DeliverableView(deliverable.id(), deliverable.title(), deliverable.description(),
                    deliverable.url(), deliverable.status(), deliverable.submittedAt(), deliverable.progress(),
                    deliverable.tasks().stream().map(TaskView::from).toList(),
                    deliverable.comments().stream().map(CommentView::from).toList());
        }
    }

    public record TaskView(UUID id, String title, boolean done) {

        static TaskView from(TaskItem task) {
            return new TaskView(task.id(), task.title(), task.isDone());
        }
    }

    public record CommentView(UUID id, UUID authorId, String body, Instant createdAt) {

        static CommentView from(DeliverableComment comment) {
            return new CommentView(comment.id(), comment.authorId(), comment.body(), comment.createdAt());
        }
    }
}
