package net.codjo.security.common.message;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 *
 */
public class DefaultModelManager implements ModelManager {
    private List<Role> roles;
    private Map<User, List<Role>> grants = new HashMap<User, List<Role>>();


    public Set<User> getUsers() {
        return Collections.unmodifiableSet(grants.keySet());
    }


    public List<Role> getRoles() {
        return Collections.unmodifiableList(roles());
    }


    public void setRoles(List<Role> allRoles) {
        this.roles = new ArrayList<Role>(allRoles);
    }


    public List<Role> getUserRoles(User user) {
        List<Role> userRoles = grants.get(user);
        if (userRoles == null) {
            userRoles = Collections.emptyList();
        }
        return userRoles;
    }


    public void visitUserRoles(User user, RoleVisitor visitor) {
        for (Role role : getUserRoles(user)) {
            role.accept(visitor);
        }
    }


    public void addUser(User user) {
        grants.put(user, new ArrayList<Role>(0));
    }


    public void removeUser(User user) {
        grants.remove(user);
    }


    public void addRoleToUser(Role role, User user) {
        if (!hasGrants(user)) {
            addUser(user);
        }
        List<Role> userRoles = grants.get(user);
        if (!userRoles.contains(role)) {
            userRoles.add(addRoleIfNeeded(role));
        }
    }


    public void removeRoleToUser(Role role, User user) {
        if (!hasGrants(user)) {
            return;
        }
        grants.get(user).remove(role);
    }


    public void addRole(Role role) {
        if (roles().contains(role)) {
            throw new IllegalArgumentException("Le rôle '" + role.getName() + "' existe déjà.");
        }
        roles().add(role);
    }


    public void removeRole(final Role role) {
        roles.remove(role);

        for (Map.Entry<User, List<Role>> entry : grants.entrySet()) {
            entry.getValue().remove(role);
        }

        RoleVisitorAdapter.visitAll(roles, new RoleVisitorAdapter() {
            @Override
            public void visitComposite(RoleComposite composite) {
                composite.removeRole(role);
            }
        });
    }


    public void addRoleToComposite(Role role, RoleComposite composite) {
        composite = (RoleComposite)addRoleIfNeeded(composite);
        composite.addRole(addRoleIfNeeded(role));
    }


    public void removeRoleToComposite(Role role, RoleComposite composite) {
        composite = (RoleComposite)findRole(composite.getName());
        composite.removeRole(role);
    }


    public boolean hasGrants(User user) {
        return grants.containsKey(user) && !grants.get(user).isEmpty();
    }


    public User findUser(String name) {
        User expected = new User(name);
        for (User user : grants.keySet()) {
            if (expected.equals(user)) {
                return user;
            }
        }
        return null;
    }


    public Role findRole(String roleName) {
        for (Role role : roles()) {
            if (role.getName().equals(roleName)) {
                return role;
            }
        }
        return null;
    }


    private Role addRoleIfNeeded(Role searchedRole) {
        for (Role role : roles()) {
            if (role.equals(searchedRole)) {
                return role;
            }
        }
        roles.add(searchedRole);
        return searchedRole;
    }


    private List<Role> roles() {
        if (roles == null) {
            roles = new ArrayList<Role>();
        }
        return roles;
    }
}
