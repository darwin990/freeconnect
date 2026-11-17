package com.freeconnect.profiles.application;

import com.freeconnect.profiles.domain.FreelancerProfile;
import com.freeconnect.profiles.domain.ObjectStore;
import com.freeconnect.profiles.domain.PortfolioItem;
import com.freeconnect.profiles.domain.ProfileRepository;
import com.freeconnect.shared.application.UseCase;
import com.freeconnect.shared.exception.NotFoundException;
import com.freeconnect.shared.security.CurrentUserProvider;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso RF-02: descargar un adjunto del portafolio propio (o como ADMIN).
 */
@Service
public class DownloadPortfolioAttachmentUseCase implements UseCase<UUID, DownloadPortfolioAttachmentUseCase.File> {

    private final ProfileRepository profileRepository;
    private final ObjectStore objectStore;
    private final CurrentUserProvider currentUser;

    public DownloadPortfolioAttachmentUseCase(ProfileRepository profileRepository, ObjectStore objectStore,
                                              CurrentUserProvider currentUser) {
        this.profileRepository = profileRepository;
        this.objectStore = objectStore;
        this.currentUser = currentUser;
    }

    @Override
    @Transactional(readOnly = true)
    public File execute(UUID itemId) {
        UUID userId = currentUser.requireUserId();
        FreelancerProfile profile = profileRepository.findFreelancerByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Perfil de freelancer", userId));
        PortfolioItem item = profile.portfolio().stream()
                .filter(candidate -> candidate.id().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Elemento de portafolio", itemId));
        if (item.storageKey() == null) {
            throw new NotFoundException("El elemento de portafolio no tiene adjunto");
        }
        ObjectStore.StoredObject stored = objectStore.get(item.storageKey())
                .orElseThrow(() -> new NotFoundException("Adjunto de portafolio", itemId));
        return new File(stored.content(), stored.contentType(), item.title());
    }

    public record File(byte[] content, String contentType, String fileName) {
    }
}
