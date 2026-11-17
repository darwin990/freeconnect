package com.freeconnect.notifications.application;

import com.freeconnect.contracts.domain.Contract;
import com.freeconnect.contracts.domain.ContractCompletedEvent;
import com.freeconnect.contracts.domain.ContractCreatedEvent;
import com.freeconnect.contracts.domain.ContractRepository;
import com.freeconnect.contracts.domain.MilestoneStatus;
import com.freeconnect.contracts.domain.MilestoneStatusChangedEvent;
import com.freeconnect.messaging.domain.MessageSentEvent;
import com.freeconnect.notifications.domain.NotificationChannelType;
import com.freeconnect.notifications.domain.NotificationType;
import com.freeconnect.projects.domain.ProjectRepository;
import com.freeconnect.proposals.domain.ProposalAcceptedEvent;
import com.freeconnect.proposals.domain.ProposalSubmittedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Observador de eventos de dominio que genera notificaciones.
 *
 * <p><b>Patron: Observer.</b> El publicador de eventos no conoce este listener;
 * agregar un canal o una regla de notificacion no modifica los casos de uso.</p>
 */
@Component
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

    private final NotificationService notificationService;
    private final ProjectRepository projectRepository;
    private final ContractRepository contractRepository;

    public NotificationEventListener(NotificationService notificationService, ProjectRepository projectRepository,
                                     ContractRepository contractRepository) {
        this.notificationService = notificationService;
        this.projectRepository = projectRepository;
        this.contractRepository = contractRepository;
    }

    @EventListener
    public void onProposalSubmitted(ProposalSubmittedEvent event) {
        safe(() -> projectRepository.findById(event.projectId()).ifPresent(project ->
                notificationService.notify(project.clientId(), NotificationType.PROPOSAL_RECEIVED,
                        "Nueva propuesta recibida",
                        "Recibiste una propuesta de " + event.amount().amount() + " " + event.amount().currency(),
                        NotificationChannelType.IN_APP)));
    }

    @EventListener
    public void onProposalAccepted(ProposalAcceptedEvent event) {
        safe(() -> notificationService.notify(event.freelancerId(), NotificationType.PROPOSAL_ACCEPTED,
                "Propuesta aceptada", "Tu propuesta fue aceptada y se genero el contrato",
                NotificationChannelType.EMAIL));
    }

    @EventListener
    public void onContractCreated(ContractCreatedEvent event) {
        safe(() -> {
            notificationService.notify(event.clientId(), NotificationType.CONTRACT_CREATED,
                    "Contrato creado", "El contrato quedo activo por " + event.amount().amount() + " "
                            + event.amount().currency(), NotificationChannelType.IN_APP);
            notificationService.notify(event.freelancerId(), NotificationType.CONTRACT_CREATED,
                    "Nuevo contrato", "Tienes un contrato activo: registra tus entregables",
                    NotificationChannelType.IN_APP);
        });
    }

    @EventListener
    public void onMessageSent(MessageSentEvent event) {
        safe(() -> notificationService.notify(event.recipientId(), NotificationType.MESSAGE_RECEIVED,
                "Nuevo mensaje", "Tienes un mensaje nuevo en el contrato",
                NotificationChannelType.IN_APP));
    }

    @EventListener
    public void onMilestoneStatusChanged(MilestoneStatusChangedEvent event) {
        safe(() -> contractRepository.findById(event.aggregateId()).ifPresent(contract -> routeMilestone(contract,
                event)));
    }

    private void routeMilestone(Contract contract, MilestoneStatusChangedEvent event) {
        if (event.current() == MilestoneStatus.SUBMITTED) {
            notificationService.notify(contract.clientId(), NotificationType.MILESTONE_SUBMITTED,
                    "Hito enviado a revision", "Un hito espera tu aprobacion",
                    NotificationChannelType.IN_APP);
        } else if (event.current() == MilestoneStatus.APPROVED) {
            notificationService.notify(contract.freelancerId(), NotificationType.MILESTONE_APPROVED,
                    "Hito aprobado", "El cliente aprobo un hito de tu contrato",
                    NotificationChannelType.EMAIL);
        }
    }

    @EventListener
    public void onContractCompleted(ContractCompletedEvent event) {
        safe(() -> {
            notificationService.notify(event.clientId(), NotificationType.CONTRACT_COMPLETED,
                    "Contrato completado", "Todos los hitos fueron aprobados",
                    NotificationChannelType.IN_APP);
            notificationService.notify(event.freelancerId(), NotificationType.CONTRACT_COMPLETED,
                    "Contrato completado", "El contrato finalizo correctamente",
                    NotificationChannelType.EMAIL);
        });
    }

    private void safe(Runnable action) {
        try {
            action.run();
        } catch (RuntimeException ex) {
            log.warn("No se pudo generar la notificacion: {}", ex.getMessage());
        }
    }

    @EventListener
    public void onReviewCreated(com.freeconnect.reviews.domain.ReviewCreatedEvent event) {
        safe(() -> notificationService.notify(event.targetId(), NotificationType.REVIEW_RECEIVED,
                "Nueva resena recibida", "Recibiste una calificacion de " + event.rating() + "/5",
                NotificationChannelType.IN_APP));
    }

    @EventListener
    public void onPaymentReleased(com.freeconnect.payments.domain.PaymentReleasedEvent event) {
        safe(() -> notificationService.notify(event.payeeId(), NotificationType.PAYMENT_RELEASED,
                "Pago liberado", "Se libero un pago de " + event.amount().amount() + " "
                        + event.amount().currency(), NotificationChannelType.EMAIL));
    }
}
