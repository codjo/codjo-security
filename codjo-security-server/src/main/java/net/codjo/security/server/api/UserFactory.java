package net.codjo.security.server.api;
import java.util.List;
import net.codjo.agent.UserId;
import net.codjo.security.common.api.SecurityContext;
import net.codjo.security.common.api.User;
/**
 * Permet de construire le profil associé à un utilisateur.
 *
 * <p> Cette classe a la connaissance sur l'association Role/fonction. </p>
 *
 * @see SecurityContextFactory
 */
public interface UserFactory {
    User getUser(UserId userId, SecurityContext securityContext);


    List<String> getRoleNames();
}
