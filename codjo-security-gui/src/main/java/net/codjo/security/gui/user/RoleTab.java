package net.codjo.security.gui.user;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import net.codjo.gui.toolkit.text.SearchTextField;
import net.codjo.security.common.message.Role;
import net.codjo.security.common.message.RoleComposite;
import net.codjo.security.common.message.User;
import net.codjo.security.gui.model.GuiListener;
import net.codjo.security.gui.model.GuiModelManager;
import net.codjo.security.gui.model.ObjectAlreadyExistsException;
import net.codjo.security.gui.model.ObjectContentEvent;
import net.codjo.security.gui.model.ObjectEvent;
import net.codjo.security.gui.search.SearchAction;
import net.codjo.security.gui.user.FileChooserActionListener.FileCommand;
import net.codjo.security.gui.user.RoleAssignementToCompositeUtil.CompositeRoleContainer;
import net.codjo.security.gui.user.RoleAssignementToCompositeUtil.CompositeRoleContainerEventManager;
import net.codjo.util.file.FileUtil;
/**
 *
 */
public class RoleTab implements RoleAssignementManager.RoleAssignementGui {
    private JPanel mainPanel;
    private JList compositeList;
    private JButton addCompositeButton;
    private JButton removeCompositeButton;
    private JList unassignedRoleList;
    private JList assignedRoleList;
    private JButton assignRoleButton;
    private JButton unassignRoleButton;
    private JTextField compositeNameField;
    private SearchTextField compositeRoleSearchField;
    private SearchTextField unassignedRoleSearchField;
    private JButton exportCSVButton;
    private GuiModelManager guiManager;
    private RoleAssignementManager<RoleComposite> roleAssignementManager;
    private CompositeRoleContainer assignableRoleContainer;
    private FileChooserConfiguration fileChooserConfiguration;


    public RoleTab() {
        compositeList.setName("compositeList");
        removeCompositeButton.setName("removeCompositeButton");
        addCompositeButton.setName("addCompositeButton");
        compositeNameField.setName("compositeNameField");
        compositeRoleSearchField.setName("compositeRoleSearchField");
        unassignedRoleSearchField.setName("assignableRoleSearchField");
        compositeList.setCellRenderer(new RoleCellRenderer());

        compositeList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                if (compositeList.getSelectedIndex() == -1) {
                    setEditedComposite(null);
                    removeCompositeButton.setEnabled(false);
                }
                else {
                    setEditedComposite(getSelectedComposite());
                    removeCompositeButton.setEnabled(true);
                }
                unassignedRoleSearchField.setText(unassignedRoleSearchField.getText());
            }
        });
        removeCompositeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                removeRole(getSelectedComposite());
            }
        });
        addCompositeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String name = JOptionPane.showInputDialog(addCompositeButton, "Nom du rôle composite:");
                if (name != null && !"".equals(name.trim())) {
                    try {
                        RoleComposite composite = new RoleComposite(name.trim());
                        guiManager.addRole(composite);
                        compositeList.requestFocus();
                        compositeList.setSelectedValue(composite, true);
                    }
                    catch (ObjectAlreadyExistsException e) {
                        JOptionPane.showMessageDialog(addCompositeButton,
                                                      e.getMessage(),
                                                      "Erreur",
                                                      JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
    }


    public void initFileChooserConfiguration(FileChooserConfiguration aFileChooserConfiguration) {
        this.fileChooserConfiguration = aFileChooserConfiguration;

        new FileChooserActionListener(exportCSVButton, new CsvFileFilter(), new FileCommand() {
            public void execute(File file) throws IOException {
                FileUtil.saveContent(file, new RolesCodec().toCsv(guiManager.getModel()));
            }
        }, aFileChooserConfiguration);
    }


    private void removeRole(RoleComposite role) {
        List<User> users = guiManager.findUserUsages(role);
        List<RoleComposite> roles = guiManager.findUsages(role);
        if (!roles.isEmpty() || !users.isEmpty()) {

            String roleMessage = toString("rôle", roles, new Stringifier<RoleComposite>() {
                public String toString(RoleComposite object) {
                    return object.getName();
                }
            });
            String userMessage = toString("utilisateur", users, new Stringifier<User>() {
                public String toString(User object) {
                    return object.getName();
                }
            });

            int selectedOption =
                  JOptionPane.showConfirmDialog(removeCompositeButton,
                                                "Ce rôle est affecté à:\n"
                                                + userMessage
                                                + roleMessage
                                                + "Voulez-vous vraiment le supprimer ?",
                                                "Confirmation de suppression",
                                                JOptionPane.OK_CANCEL_OPTION);
            if (selectedOption != JOptionPane.OK_OPTION) {
                return;
            }
        }
        guiManager.removeRole(role);
    }


    public void initialize(GuiModelManager manager) {
        this.guiManager = manager;

        roleAssignementManager = new RoleAssignementManager<RoleComposite>(this);
        roleAssignementManager.listenToThis(new CompositeRoleContainerEventManager(guiManager));

        GuiUtil.updateModel(compositeList, getCompositeRoles());

        guiManager.addCompositeListener(GuiUtil.<RoleComposite>updateListModel(compositeList));

        guiManager.addCompositeContentListener(new GuiListener<ObjectContentEvent<RoleComposite>>() {
            public void eventTriggered(ObjectContentEvent<RoleComposite> event) {
                if (event.getObject() != compositeList.getSelectedValue()) {
                    compositeList.setSelectedValue(event.getObject(), true);
                }
                compositeRoleSearchField.setText(compositeRoleSearchField.getText());
                unassignedRoleSearchField.setText(unassignedRoleSearchField.getText());
            }
        });

        CompositeRoleSearchAdapter compositeRoleAdapter = new CompositeRoleSearchAdapter(compositeList);
        connectSearchFields(compositeRoleAdapter, compositeRoleSearchField, compositeList);
        AssignableRoleSearchAdapter unassignedRoleAdapter
              = new AssignableRoleSearchAdapter(unassignedRoleList);
        connectSearchFields(unassignedRoleAdapter, unassignedRoleSearchField, unassignedRoleList);

        compositeRoleSearchField.requestFocus();
    }


    private void connectSearchFields(final RoleSearchAdapter<? extends Role> searchAdapter,
                                     final SearchTextField searchField,
                                     final JList listComponent) {
        SearchAction.activate(mainPanel, searchField, searchAdapter);
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (KeyEvent.VK_DOWN == e.getKeyCode()) {
                    listComponent.requestFocus();
                    listComponent.setSelectedIndex(listComponent.getFirstVisibleIndex());
                }
            }
        });
        guiManager.addCompositeListener(new GuiListener<ObjectEvent<RoleComposite>>() {
            public void eventTriggered(ObjectEvent<RoleComposite> event) {
                searchField.setText("");
            }
        });
    }


    private void setEditedComposite(final RoleComposite composite) {
        if (composite == null) {
            compositeNameField.setText("");
            roleAssignementManager.setRoleContainer(null);
        }
        else {
            compositeNameField.setText(composite.getName());
            assignableRoleContainer = new CompositeRoleContainer(guiManager, composite);
            roleAssignementManager.setRoleContainer(assignableRoleContainer);
        }
    }


    private RoleComposite getSelectedComposite() {
        return (RoleComposite)compositeList.getSelectedValue();
    }


    void setSelectedComposite(Object value) {
        compositeList.setSelectedValue(value, true);
    }


    public JPanel getMainPanel() {
        return mainPanel;
    }


    private List<RoleComposite> getCompositeRoles() {
        List<RoleComposite> roleComposites = new ArrayList<RoleComposite>();
        for (Role role : guiManager.getAllRoles()) {
            if (role instanceof RoleComposite) {
                roleComposites.add((RoleComposite)role);
            }
        }
        return roleComposites;
    }


    public GuiModelManager getGuiManager() {
        return guiManager;
    }


    public JList getUnassignedRoleList() {
        return unassignedRoleList;
    }


    public JList getAssignedRoleList() {
        return assignedRoleList;
    }


    public JButton getAssignRoleButton() {
        return assignRoleButton;
    }


    public JButton getUnassignRoleButton() {
        return unassignRoleButton;
    }


    private <T> String toString(String label, List<T> list, Stringifier<T> stringifier) {
        if (list.isEmpty()) {
            return "";
        }
        List<T> subList = list.subList(0, Math.min(2, list.size()));
        StringBuilder builder = new StringBuilder();
        for (T role : subList) {
            if (builder.length() != 0) {
                builder.append(", ");
            }
            builder.append(stringifier.toString(role));
        }
        if (2 < list.size()) {
            builder.append(", etc...");
        }
        return "  * " + list.size() + (" " + label + "(s) : ") + builder + "\n";
    }


    public FileChooserConfiguration getFileChooserConfiguration() {
        return fileChooserConfiguration;
    }


    static interface Stringifier<T> {
        String toString(T object);
    }

    private abstract class RoleSearchAdapter<T extends Role> extends AbstractSearchAdapter<T> {

        protected RoleSearchAdapter(JList list) {
            super(list);
        }


        protected abstract List<T> getAllSearchableRoles();


        public List<T> doSearch(String searchPattern) {
            List<T> foundRoles = new ArrayList<T>();
            for (T role : getAllSearchableRoles()) {
                if (roleMatch(role, searchPattern)) {
                    foundRoles.add(role);
                }
            }
            return foundRoles;
        }


        private boolean roleMatch(Role role, String searchPattern) {
            if (role.getName().toUpperCase().contains(searchPattern.toUpperCase())) {
                return true;
            }
            if (role instanceof RoleComposite) {
                for (Role innerRole : ((RoleComposite)role).getRoles()) {
                    if (roleMatch(innerRole, searchPattern)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
    private class AssignableRoleSearchAdapter extends RoleSearchAdapter<Role> {
        protected AssignableRoleSearchAdapter(JList list) {
            super(list);
        }


        @Override
        protected List<Role> getAllSearchableRoles() {
            return assignableRoleContainer.getUnassignedRoles();
        }
    }
    private class CompositeRoleSearchAdapter extends RoleSearchAdapter<RoleComposite> {

        private CompositeRoleSearchAdapter(JList list) {
            super(list);
        }


        @Override
        protected List<RoleComposite> getAllSearchableRoles() {
            return getCompositeRoles();
        }
    }
}
