package net.codjo.security.common.message;
import java.util.List;
import java.util.Set;
/**
 *
 */
public interface ModelManager {
    Set<User> getUsers();


    List<Role> getRoles();


    List<Role> getUserRoles(User user);


    void visitUserRoles(User user, RoleVisitor visitor);


    void addUser(User user);


    void removeUser(User user);


    void addRoleToUser(Role role, User user);


    void removeRoleToUser(Role role, User user);


    void addRole(Role role);


    void removeRole(Role role);


    void addRoleToComposite(Role role, RoleComposite composite);


    void removeRoleToComposite(Role role, RoleComposite composite);


    boolean hasGrants(User user);


    User findUser(String user);


    Role findRole(String role);
}
