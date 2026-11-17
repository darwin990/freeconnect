package com.freeconnect.profiles.api.dto;

import com.freeconnect.profiles.domain.Availability;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Set;

/**
 * Payload de actualizacion de perfil (freelancer o cliente).
 */
public record UpdateProfileRequest(
        @Size(max = 160) String headline,
        @Size(max = 2000) String bio,
        @PositiveOrZero BigDecimal hourlyRate,
        @Size(min = 3, max = 3) String currency,
        Availability availability,
        Set<@Size(max = 80) String> skills,
        @Size(max = 160) String companyName,
        @Size(max = 2000) String description) {
}
