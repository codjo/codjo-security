package net.codjo.security.gui.user;
import java.util.List;
import net.codjo.security.common.message.Role;
import net.codjo.security.common.message.User;
import net.codjo.security.gui.model.GuiListener;
import net.codjo.security.gui.model.GuiModelManager;
import net.codjo.security.gui.model.ObjectContentEvent;
import net.codjo.security.gui.user.RoleAssignementManager.RoleContainer;
import net.codjo.security.gui.user.RoleAssignementManager.RoleContainerEventManager;
/**
 *
 */
class RoleAssignementToUserUtil {
    static class UserRoleContainer implements RoleContainer {
        private final User user;
        private GuiModelManager guiManager;


        UserRoleContainer(GuiModelManager guiManager, User user) {
            this.user = user;
            this.guiManager = guiManager;
        }


        public void addRole(Role role) {
            guiManager.addRoleToUser(role, user);
        }


        public void removeRole(Role role) {
            guiManager.removeRoleToUser(role, user);
        }


        public List<Role> getRoles() {
            return guiManager.getUserRoles(user);
        }


        public boolean isRelatedTo(ObjectContentEvent event) {
            return user.equals(event.getObject());
        }


        public List<Role> getAllAssignableRoles() {
            return guiManager.getAllRoles();
        }
    }
    static class UserRoleContainerEventManager implements RoleContainerEventManager<User> {
        private GuiModelManager guiManager;


        UserRoleContainerEventManager(GuiModelManager guiManager) {
            this.guiManager = guiManager;
        }


        public void addContentListener(GuiListener<ObjectContentEvent<User>> listener) {
            guiManager.addUserContentListener(listener);
        }
    }
}
