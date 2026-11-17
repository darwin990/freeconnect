package com.freeconnect.profiles.application;

import com.freeconnect.profiles.domain.FreelancerProfile;
import com.freeconnect.profiles.domain.ObjectStore;
import com.freeconnect.profiles.domain.ProfileRepository;
import com.freeconnect.shared.application.AbstractUseCase;
import com.freeconnect.shared.exception.NotFoundException;
import com.freeconnect.shared.exception.ValidationException;
import com.freeconnect.shared.security.CurrentUserProvider;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso RF-02: adjuntar un archivo de portafolio.
 *
 * <p>El binario se almacena a traves de {@link ObjectStore}, que en produccion es
 * el {@code StorageProxy} con control de acceso y cache sobre S3.</p>
 */
@Service
public class UploadPortfolioAttachmentUseCase
        extends AbstractUseCase<UploadPortfolioAttachmentUseCase.Command, ProfileView> {

    private final ProfileRepository profileRepository;
    private final ObjectStore objectStore;
    private final CurrentUserProvider currentUser;
    private final ProfileViewMapper mapper;

    public UploadPortfolioAttachmentUseCase(ProfileRepository profileRepository, ObjectStore objectStore,
                                            CurrentUserProvider currentUser, ProfileViewMapper mapper) {
        this.profileRepository = profileRepository;
        this.objectStore = objectStore;
        this.currentUser = currentUser;
        this.mapper = mapper;
    }

    @Override
    protected void validate(Command command) {
        if (command.content() == null || command.content().length == 0) {
            throw new ValidationException("El archivo esta vacio");
        }
        if (command.content().length > 5 * 1024 * 1024) {
            throw new ValidationException("El archivo supera el limite de 5 MB");
        }
    }

    @Override
    @Transactional
    protected ProfileView handle(Command command) {
        UUID userId = currentUser.requireUserId();
        FreelancerProfile profile = profileRepository.findFreelancerByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Perfil de freelancer", userId));
        String key = ObjectStore.keyFor(userId, command.itemId(), command.fileName());
        ObjectStore.StoredObject stored = objectStore.put(key, command.content(), command.contentType());
        profile.attachPortfolioFile(command.itemId(), stored.url(), key);
        return mapper.toView(profileRepository.save(profile));
    }

    public record Command(UUID itemId, String fileName, String contentType, byte[] content) {
    }
}
