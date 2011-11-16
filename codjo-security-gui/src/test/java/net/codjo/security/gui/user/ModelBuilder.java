package net.codjo.security.gui.user;
import java.util.HashMap;
import java.util.Map;
import net.codjo.security.common.message.DefaultModelManager;
import net.codjo.security.common.message.ModelManager;
import net.codjo.security.common.message.Role;
import net.codjo.security.common.message.RoleComposite;
import net.codjo.security.common.message.User;
/**
 *
 */
class ModelBuilder {
    private DefaultModelManager modelManager;
    private Map<String, User> nameToUser = new HashMap<String, User>();
    private Map<String, Role> nameToRole = new HashMap<String, Role>();


    ModelBuilder() {
        this.modelManager = new DefaultModelManager();
    }


    public ModelManager get() {
        return modelManager;
    }


    public ModelBuilder addUser(String userName) {
        return addUser(new User(userName));
    }


    public ModelBuilder addRole(String roleName) {
        return addRole(new Role(roleName));
    }


    public ModelBuilder addRoleToUser(String roleName, String userName) {
        Role role = nameToRole.get(roleName);
        if (role == null) {
            role = new Role(roleName);
            addRole(role);
        }
        User user = nameToUser.get(userName);
        if (user == null) {
            user = new User(userName);
            addUser(user);
        }
        modelManager.addRoleToUser(role, user);
        return this;
    }


    public ModelBuilder addUser(User user) {
        modelManager.addUser(user);
        nameToUser.put(user.getName(), user);
        return this;
    }


    public ModelBuilder addRole(Role role) {
        modelManager.addRole(role);
        nameToRole.put(role.getName(), role);
        return this;
    }


    public ModelBuilder addRoleComposite(String name) {
        addRole(new RoleComposite(name));
        return this;
    }


    public ModelBuilder addRoleComposite(String compositeName, String... roleNames) {
        RoleComposite composite = (RoleComposite)nameToRole.get(compositeName);
        if (composite == null) {
            composite = new RoleComposite(compositeName);
            addRole(composite);
        }
        for (String roleName : roleNames) {
            Role role = nameToRole.get(roleName);
            if (role == null) {
                role = new Role(roleName);
                addRole(role);
            }
            modelManager.addRoleToComposite(role, composite);
        }
        return this;
    }
}
