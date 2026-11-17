package com.freeconnect.profiles.api;

import com.freeconnect.profiles.api.dto.CreatePortfolioItemRequest;
import com.freeconnect.profiles.api.dto.UpdateProfileRequest;
import com.freeconnect.profiles.application.AddPortfolioItemUseCase;
import com.freeconnect.profiles.application.DownloadPortfolioAttachmentUseCase;
import com.freeconnect.profiles.application.GetMyProfileUseCase;
import com.freeconnect.profiles.application.GetProfileByIdUseCase;
import com.freeconnect.profiles.application.ProfileView;
import com.freeconnect.profiles.application.UpdateMyProfileUseCase;
import com.freeconnect.profiles.application.UpdateProfileCommand;
import com.freeconnect.profiles.application.UploadPortfolioAttachmentUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * API de perfiles (RF-02).
 */
@RestController
@RequestMapping("/api/v1/profiles")
@Tag(name = "Perfiles", description = "Perfil profesional, habilidades y portafolio")
@PreAuthorize("isAuthenticated()")
public class ProfileController {

    private final GetMyProfileUseCase getMyProfileUseCase;
    private final GetProfileByIdUseCase getProfileByIdUseCase;
    private final UpdateMyProfileUseCase updateMyProfileUseCase;
    private final AddPortfolioItemUseCase addPortfolioItemUseCase;
    private final UploadPortfolioAttachmentUseCase uploadPortfolioAttachmentUseCase;
    private final DownloadPortfolioAttachmentUseCase downloadPortfolioAttachmentUseCase;

    public ProfileController(GetMyProfileUseCase getMyProfileUseCase,
                             GetProfileByIdUseCase getProfileByIdUseCase,
                             UpdateMyProfileUseCase updateMyProfileUseCase,
                             AddPortfolioItemUseCase addPortfolioItemUseCase,
                             UploadPortfolioAttachmentUseCase uploadPortfolioAttachmentUseCase,
                             DownloadPortfolioAttachmentUseCase downloadPortfolioAttachmentUseCase) {
        this.getMyProfileUseCase = getMyProfileUseCase;
        this.getProfileByIdUseCase = getProfileByIdUseCase;
        this.updateMyProfileUseCase = updateMyProfileUseCase;
        this.addPortfolioItemUseCase = addPortfolioItemUseCase;
        this.uploadPortfolioAttachmentUseCase = uploadPortfolioAttachmentUseCase;
        this.downloadPortfolioAttachmentUseCase = downloadPortfolioAttachmentUseCase;
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener el perfil propio")
    public ProfileView myProfile() {
        return getMyProfileUseCase.execute(null);
    }

    @PutMapping("/me")
    @Operation(summary = "Crear o actualizar el perfil propio")
    public ProfileView updateMyProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return updateMyProfileUseCase.execute(new UpdateProfileCommand(request.headline(), request.bio(),
                request.hourlyRate(), request.currency(), request.availability(), request.skills(),
                request.companyName(), request.description()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un perfil publico por id")
    public ProfileView profileById(@PathVariable UUID id) {
        return getProfileByIdUseCase.execute(id);
    }

    @PostMapping("/me/portfolio")
    @PreAuthorize("hasRole('FREELANCER')")
    @Operation(summary = "Agregar un elemento al portafolio")
    public ResponseEntity<ProfileView> addPortfolioItem(@Valid @RequestBody CreatePortfolioItemRequest request) {
        ProfileView view = addPortfolioItemUseCase.execute(
                new AddPortfolioItemUseCase.Command(request.title(), request.url(), request.description()));
        return ResponseEntity.status(HttpStatus.CREATED).body(view);
    }

    @PostMapping(value = "/me/portfolio/{itemId}/attachment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('FREELANCER')")
    @Operation(summary = "Adjuntar archivo a un elemento del portafolio")
    public ProfileView uploadAttachment(@PathVariable UUID itemId, @RequestParam("file") MultipartFile file)
            throws IOException {
        return uploadPortfolioAttachmentUseCase.execute(new UploadPortfolioAttachmentUseCase.Command(itemId,
                file.getOriginalFilename() == null ? "archivo" : file.getOriginalFilename(),
                file.getContentType() == null ? "application/octet-stream" : file.getContentType(),
                file.getBytes()));
    }

    @GetMapping("/me/portfolio/{itemId}/attachment")
    @PreAuthorize("hasRole('FREELANCER')")
    @Operation(summary = "Descargar el adjunto del portafolio propio")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable UUID itemId) {
        DownloadPortfolioAttachmentUseCase.File file = downloadPortfolioAttachmentUseCase.execute(itemId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.fileName() + "\"")
                .contentType(MediaType.parseMediaType(file.contentType()))
                .body(file.content());
    }
}
