package net.codjo.security.gui.user;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import net.codjo.security.common.message.Role;
import net.codjo.security.gui.model.GuiListener;
import net.codjo.security.gui.model.ObjectContentEvent;
/**
 *
 */
class RoleAssignementManager<T> implements GuiListener<ObjectContentEvent<T>> {
    private RoleContainer roleContainer;
    private RoleAssignementGui gui;


    RoleAssignementManager(RoleAssignementGui roleAssignementGui) {
        this.gui = roleAssignementGui;
        gui.getUnassignedRoleList().setName("unassignedRoles");
        gui.getUnassignedRoleList().setCellRenderer(new RoleCellRenderer());
        gui.getAssignedRoleList().setName("assignedRoles");
        gui.getAssignedRoleList().setCellRenderer(new RoleCellRenderer());

        // Assign action
        final Action assignRoleAction = createMoveAction(">", "addRoleToUser",
                                                         gui.getUnassignedRoleList());
        gui.getAssignRoleButton().setAction(assignRoleAction);

        // Unassign action
        final Action unassignRoleAction = createMoveAction("<", "removeRoleFromUser",
                                                           gui.getAssignedRoleList());
        gui.getUnassignRoleButton().setAction(unassignRoleAction);

        // Action status
        ListSelectionListener listener = new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                assignRoleAction.setEnabled(gui.getUnassignedRoleList().getSelectedIndex() != -1);
                unassignRoleAction.setEnabled(gui.getAssignedRoleList().getSelectedIndex() != -1);
            }
        };
        gui.getUnassignedRoleList().addListSelectionListener(listener);
        gui.getAssignedRoleList().addListSelectionListener(listener);

        // Selection auto sur focus
        gui.getUnassignedRoleList().addFocusListener(new SelectFirstOnFocus(gui.getUnassignedRoleList()));
        gui.getAssignedRoleList().addFocusListener(new SelectFirstOnFocus(gui.getAssignedRoleList()));
    }


    public void listenToThis(RoleContainerEventManager<T> containerEventManager) {
        containerEventManager.addContentListener(this);
    }


    public void setRoleContainer(RoleContainer container) {
        this.roleContainer = container;
        if (roleContainer == null) {
            setPanelState(false);
            return;
        }
        setPanelState(true);

        List<Role> roles = new ArrayList<Role>(roleContainer.getAllAssignableRoles());
        List<Role> userRoles = new ArrayList<Role>(roleContainer.getRoles());

        roles.removeAll(userRoles);

        GuiUtil.updateModel(gui.getUnassignedRoleList(), roles);
        GuiUtil.updateModel(gui.getAssignedRoleList(), userRoles);
    }


    private void setPanelState(boolean enabled) {
        gui.getUnassignedRoleList().setEnabled(enabled);
        GuiUtil.getModel(gui.getUnassignedRoleList()).clear();
        gui.getAssignedRoleList().setEnabled(enabled);
        GuiUtil.getModel(gui.getAssignedRoleList()).clear();
    }


    private void moveSelectedLine(JList from) {
        for (Object selectedRole : from.getSelectedValues()) {
            try {
                moveRole(from, selectedRole);
            }
            catch (Exception e) {
                JOptionPane.showMessageDialog(gui.getAssignRoleButton(),
                                              e.getMessage(), "Erreur",
                                              JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private void moveRole(JList from, Object selectedRole) {
        if (from == gui.getUnassignedRoleList()) {
            roleContainer.addRole((Role)selectedRole);
        }
        else {
            roleContainer.removeRole((Role)selectedRole);
        }
    }


    public void eventTriggered(ObjectContentEvent event) {
        if (roleContainer == null || !roleContainer.isRelatedTo(event)) {
            return;
        }
        if (event.getType() == ObjectContentEvent.TYPE.ROLE_ADDED) {
            GuiUtil.getModel(gui.getUnassignedRoleList()).removeElement(event.getRole());
            GuiUtil.getModel(gui.getAssignedRoleList()).addElement(event.getRole());
        }
        else {
            GuiUtil.getModel(gui.getAssignedRoleList()).removeElement(event.getRole());
            GuiUtil.getModel(gui.getUnassignedRoleList()).addElement(event.getRole());
        }
    }


    private Action createMoveAction(String label, String actionId, final JList from) {
        final Action moveAction = new AbstractAction(label) {
            public void actionPerformed(ActionEvent evt) {
                moveSelectedLine(from);
            }
        };
        moveAction.setEnabled(false);
        from.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), actionId);
        from.getActionMap().put(actionId, moveAction);
        return moveAction;
    }


    private static class SelectFirstOnFocus extends FocusAdapter {
        private JList list;


        private SelectFirstOnFocus(JList list) {
            this.list = list;
        }


        @Override
        public void focusGained(FocusEvent event) {
            if (list.getSelectedIndex() == -1) {
                list.setSelectedIndex(0);
            }
        }
    }
    interface RoleContainer {
        void addRole(Role role);


        void removeRole(Role role);


        List<Role> getRoles();


        boolean isRelatedTo(ObjectContentEvent event);


        List<Role> getAllAssignableRoles();
    }
    interface RoleAssignementGui {
        JList getUnassignedRoleList();


        JList getAssignedRoleList();


        JButton getUnassignRoleButton();


        JButton getAssignRoleButton();
    }
    interface RoleContainerEventManager<T> {

        void addContentListener(GuiListener<ObjectContentEvent<T>> listener);
    }
}