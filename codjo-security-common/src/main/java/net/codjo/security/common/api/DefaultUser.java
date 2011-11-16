package net.codjo.security.common.api;
import java.util.ArrayList;
import java.util.List;
import net.codjo.agent.UserId;
/**
 * Représente un utilisateur configuré à partir d'un fichier XML.
 *
 * @see net.codjo.security.common.datagen.DatagenUserFactory#getUser(net.codjo.agent.UserId,
 *      net.codjo.security.common.api.SecurityContext)
 */
public class DefaultUser implements User {
    private final List<Role> roles;
    private final UserId userId;
    private final SecurityContext securityContext;


    public DefaultUser(UserId userId, List<Role> allRoles, SecurityContext securityContext) {
        this.userId = userId;
        this.securityContext = securityContext;
        this.roles = new ArrayList<Role>(allRoles.size());
        for (Role role : allRoles) {
            if (securityContext.isCallerInRole(role.getRoleId())) {
                roles.add(role);
            }
        }
    }


    public UserId getId() {
        return userId;
    }


    public boolean isAllowedTo(String function) {
        for (Role role : roles) {
            if (role.isAllowedTo(function)) {
                return true;
            }
        }
        return false;
    }


    public boolean isInRole(String roleId) {
        return securityContext.isCallerInRole(roleId);
    }
}
