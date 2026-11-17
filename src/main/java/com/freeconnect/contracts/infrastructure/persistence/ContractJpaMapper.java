package com.freeconnect.contracts.infrastructure.persistence;

import com.freeconnect.contracts.domain.Contract;
import com.freeconnect.contracts.domain.Deliverable;
import com.freeconnect.contracts.domain.DeliverableComment;
import com.freeconnect.contracts.domain.Milestone;
import com.freeconnect.contracts.domain.composite.TaskItem;
import com.freeconnect.shared.domain.Money;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Traductor entre el agregado {@link Contract} y su modelo JPA.
 *
 * <p>Sincroniza hijos por identificador para respetar el ciclo de vida del
 * agregado sin recrear filas existentes.</p>
 */
final class ContractJpaMapper {

    private ContractJpaMapper() {
    }

    static void copyToEntity(Contract contract, ContractJpaEntity entity) {
        entity.setProjectId(contract.projectId());
        entity.setProposalId(contract.proposalId());
        entity.setClientId(contract.clientId());
        entity.setFreelancerId(contract.freelancerId());
        entity.setTitle(contract.title());
        entity.setScope(contract.scope());
        entity.setAmountAmount(contract.amount().amount());
        entity.setAmountCurrency(contract.amount().currency());
        entity.setCommissionAmount(contract.commission().amount());
        entity.setCommissionCurrency(contract.commission().currency());
        entity.setStartDate(contract.startDate());
        entity.setEndDate(contract.endDate());
        entity.setStatus(contract.status());
        entity.setCreatedAt(contract.createdAt());
        entity.setUpdatedAt(contract.updatedAt());
        entity.setCompletedAt(contract.completedAt());
        syncMilestones(contract.milestones(), entity.getMilestones());
    }

    static Contract toDomain(ContractJpaEntity entity) {
        return Contract.builder(entity.getProjectId(), entity.getProposalId(), entity.getClientId(),
                        entity.getFreelancerId())
                .id(entity.getId())
                .title(entity.getTitle())
                .scope(entity.getScope())
                .amount(Money.of(entity.getAmountAmount(), entity.getAmountCurrency()))
                .commission(Money.of(entity.getCommissionAmount(), entity.getCommissionCurrency()))
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .milestones(entity.getMilestones().stream()
                        .map(milestone -> toMilestone(entity.getId(), milestone))
                        .toList())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .completedAt(entity.getCompletedAt())
                .build();
    }

    private static Milestone toMilestone(UUID contractId, MilestoneJpaEntity entity) {
        return Milestone.rehydrate(entity.getId(), contractId, entity.getTitle(), entity.getDescription(),
                Money.of(entity.getAmountAmount(), entity.getAmountCurrency()), entity.getDueDate(),
                entity.getPosition(), entity.getStatus(),
                entity.getDeliverables().stream().map(ContractJpaMapper::toDeliverable).toList());
    }

    private static Deliverable toDeliverable(DeliverableJpaEntity entity) {
        return Deliverable.rehydrate(entity.getId(), entity.getTitle(), entity.getDescription(), entity.getUrl(),
                entity.getStatus(), entity.getSubmittedAt(),
                entity.getTasks().stream()
                        .map(task -> new TaskItem(task.getId(), task.getTitle(), task.isDone()))
                        .toList(),
                entity.getComments().stream()
                        .map(comment -> new DeliverableComment(comment.getId(), comment.getAuthorId(),
                                comment.getBody(), comment.getCreatedAt()))
                        .toList());
    }

    private static void syncMilestones(List<Milestone> domain, List<MilestoneJpaEntity> entities) {
        Map<UUID, MilestoneJpaEntity> existing = new HashMap<>();
        entities.forEach(entity -> existing.put(entity.getId(), entity));
        Set<UUID> domainIds = new HashSet<>(domain.stream().map(Milestone::id).toList());
        entities.removeIf(entity -> !domainIds.contains(entity.getId()));
        for (Milestone milestone : domain) {
            MilestoneJpaEntity entity = existing.get(milestone.id());
            if (entity == null) {
                entity = new MilestoneJpaEntity(milestone.id());
                entities.add(entity);
            }
            entity.setTitle(milestone.title());
            entity.setDescription(milestone.description());
            entity.setAmountAmount(milestone.amount().amount());
            entity.setAmountCurrency(milestone.amount().currency());
            entity.setDueDate(milestone.dueDate());
            entity.setPosition(milestone.position());
            entity.setStatus(milestone.status());
            syncDeliverables(milestone.deliverables(), entity.getDeliverables());
        }
    }

    private static void syncDeliverables(List<Deliverable> domain, List<DeliverableJpaEntity> entities) {
        Map<UUID, DeliverableJpaEntity> existing = new HashMap<>();
        entities.forEach(entity -> existing.put(entity.getId(), entity));
        Set<UUID> domainIds = new HashSet<>(domain.stream().map(Deliverable::id).toList());
        entities.removeIf(entity -> !domainIds.contains(entity.getId()));
        for (Deliverable deliverable : domain) {
            DeliverableJpaEntity entity = existing.get(deliverable.id());
            if (entity == null) {
                entity = new DeliverableJpaEntity(deliverable.id());
                entities.add(entity);
            }
            entity.setTitle(deliverable.title());
            entity.setDescription(deliverable.description());
            entity.setUrl(deliverable.url());
            entity.setStatus(deliverable.status());
            entity.setSubmittedAt(deliverable.submittedAt());
            syncTasks(deliverable.tasks(), entity.getTasks());
            syncComments(deliverable.comments(), entity.getComments());
        }
    }

    private static void syncTasks(List<TaskItem> domain, List<TaskItemJpaEntity> entities) {
        Map<UUID, TaskItemJpaEntity> existing = new HashMap<>();
        entities.forEach(entity -> existing.put(entity.getId(), entity));
        Set<UUID> domainIds = new HashSet<>(domain.stream().map(TaskItem::id).toList());
        entities.removeIf(entity -> !domainIds.contains(entity.getId()));
        for (TaskItem task : domain) {
            TaskItemJpaEntity entity = existing.get(task.id());
            if (entity == null) {
                entity = new TaskItemJpaEntity(task.id());
                entities.add(entity);
            }
            entity.setTitle(task.title());
            entity.setDone(task.isDone());
        }
    }

    private static void syncComments(List<DeliverableComment> domain, List<DeliverableCommentJpaEntity> entities) {
        Map<UUID, DeliverableCommentJpaEntity> existing = new HashMap<>();
        entities.forEach(entity -> existing.put(entity.getId(), entity));
        Set<UUID> domainIds = new HashSet<>(domain.stream().map(DeliverableComment::id).toList());
        entities.removeIf(entity -> !domainIds.contains(entity.getId()));
        for (DeliverableComment comment : domain) {
            DeliverableCommentJpaEntity entity = existing.get(comment.id());
            if (entity == null) {
                entity = new DeliverableCommentJpaEntity(comment.id());
                entities.add(entity);
            }
            entity.setAuthorId(comment.authorId());
            entity.setBody(comment.body());
            entity.setCreatedAt(comment.createdAt());
        }
    }
}
