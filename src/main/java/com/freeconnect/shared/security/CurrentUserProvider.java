package com.freeconnect.shared.security;

import com.freeconnect.shared.exception.ForbiddenException;
import com.freeconnect.shared.exception.UnauthorizedException;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Acceso tipado al actor autenticado y guardas de propiedad de recurso.
 *
 * <p><b>Patron: Proxy de seguridad.</b> Centraliza la verificacion de identidad
 * y propiedad para que los casos de uso no dependan de Spring Security.</p>
 */
@Component
public class CurrentUserProvider {

    public AuthenticatedUser require() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new UnauthorizedException("Se requiere autenticacion para esta operacion");
        }
        return user;
    }

    public UUID requireUserId() {
        return require().id();
    }

    public UUID currentUserIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return user.id();
        }
        return null;
    }

    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user
                && user.isAdmin();
    }

    public void requireAdmin() {
        if (!isAdmin()) {
            throw new ForbiddenException("Se requiere rol ADMIN");
        }
    }

    /** Verifica que el actor sea el propietario indicado (o ADMIN). */
    public void requireOwner(UUID ownerId, String resource) {
        AuthenticatedUser user = require();
        if (!user.isAdmin() && !user.id().equals(ownerId)) {
            throw ForbiddenException.notOwner(resource);
        }
    }

    /** Verifica que el actor sea una de las partes (o ADMIN). */
    public void requireParticipant(UUID firstParty, UUID secondParty, String resource) {
        AuthenticatedUser user = require();
        if (user.isAdmin()) {
            return;
        }
        if (!user.id().equals(firstParty) && !user.id().equals(secondParty)) {
            throw ForbiddenException.notOwner(resource);
        }
    }

    /** Verifica el rol requerido (ADMIN siempre pasa). */
    public void requireRole(Role role) {
        AuthenticatedUser user = require();
        if (!user.isAdmin() && user.role() != role) {
            throw new ForbiddenException("Se requiere rol " + role.name());
        }
    }
}
