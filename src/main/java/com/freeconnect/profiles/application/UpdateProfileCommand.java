package com.freeconnect.profiles.application;

import com.freeconnect.profiles.domain.Availability;
import java.math.BigDecimal;
import java.util.Set;

/**
 * Comando de creacion/actualizacion del perfil propio (RF-02).
 */
public record UpdateProfileCommand(String headline, String bio, BigDecimal hourlyRate, String currency,
                                   Availability availability, Set<String> skills, String companyName,
                                   String description) {
}
