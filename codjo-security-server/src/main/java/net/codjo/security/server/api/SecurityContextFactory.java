package net.codjo.security.server.api;
import net.codjo.agent.UserId;
import net.codjo.security.common.api.SecurityContext;
/**
 * Construit le contexte de sécurité associé à un utilisateur.
 *
 *
 * <p> Cette classe a la connaissance sur l'association Utilisateur/Role. </p>
 */
public interface SecurityContextFactory {
    SecurityContext createSecurityContext(UserId id, Storage storage) throws Exception;
}
