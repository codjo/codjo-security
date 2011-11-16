package net.codjo.security.gui.user;
import java.util.ArrayList;
import java.util.List;
import net.codjo.security.common.message.Role;
import net.codjo.security.common.message.RoleComposite;
import net.codjo.security.gui.model.GuiListener;
import net.codjo.security.gui.model.GuiModelManager;
import net.codjo.security.gui.model.ObjectContentEvent;
import net.codjo.security.gui.user.RoleAssignementManager.RoleContainer;
import net.codjo.security.gui.user.RoleAssignementManager.RoleContainerEventManager;
/**
 *
 */
class RoleAssignementToCompositeUtil {
    static class CompositeRoleContainer implements RoleContainer {
        private final RoleComposite composite;
        private GuiModelManager guiManager;


        CompositeRoleContainer(GuiModelManager guiManager, RoleComposite composite) {
            this.composite = composite;
            this.guiManager = guiManager;
        }


        public void addRole(Role role) {
            guiManager.addRoleToComposite(role, composite);
        }


        public void removeRole(Role role) {
            guiManager.removeRoleToComposite(role, composite);
        }


        public List<Role> getRoles() {
            return composite.getRoles();
        }


        public boolean isRelatedTo(ObjectContentEvent event) {
            return composite.equals(event.getObject());
        }


        public List<Role> getAllAssignableRoles() {
            List<Role> roleList = new ArrayList<Role>(guiManager.getAllRoles());
            roleList.remove(composite);
            return roleList;
        }


        public List<Role> getUnassignedRoles() {
            List<Role> roleList = getAllAssignableRoles();
            for (Role assignedRole : composite.getRoles()) {
                roleList.remove(assignedRole);
            }
            return roleList;
        }
    }

    static class CompositeRoleContainerEventManager implements RoleContainerEventManager<RoleComposite> {
        private GuiModelManager guiManager;


        CompositeRoleContainerEventManager(GuiModelManager guiManager) {
            this.guiManager = guiManager;
        }


        public void addContentListener(GuiListener<ObjectContentEvent<RoleComposite>> listener) {
            guiManager.addCompositeContentListener(listener);
        }
    }
}
