package net.codjo.security.gui.user;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import net.codjo.gui.toolkit.text.SearchTextField;
import net.codjo.security.common.message.ModelManager;
import net.codjo.security.common.message.Role;
import net.codjo.security.common.message.RoleComposite;
import net.codjo.security.common.message.SecurityEngineConfiguration;
import net.codjo.security.common.message.User;
import net.codjo.security.gui.model.GuiListener;
import net.codjo.security.gui.model.GuiModelManager;
import net.codjo.security.gui.model.ObjectAlreadyExistsException;
import net.codjo.security.gui.model.ObjectContentEvent;
import net.codjo.security.gui.model.ObjectEvent;
import net.codjo.security.gui.search.SearchAction;
import net.codjo.security.gui.user.FileChooserActionListener.FileCommand;
import net.codjo.security.gui.user.RoleAssignementToUserUtil.UserRoleContainer;
import net.codjo.security.gui.user.RoleAssignementToUserUtil.UserRoleContainerEventManager;
import net.codjo.util.file.FileUtil;

import static net.codjo.security.gui.user.GuiUtil.format;
/**
 *
 */
public class UserTabForInternalManagement implements RoleAssignementManager.RoleAssignementGui, UserTab {
    private JPanel mainPanel;
    private JButton userAddButton;
    private JButton removeUserButton;
    private JList userList;
    private SearchTextField searchField;
    private JList unassignedRoleList;
    private JList assignedRoleList;
    private JButton assignRoleButton;
    private JButton unassignRoleButton;
    private JTextField nameField;
    private JTextField lastLoginField;
    private JTextField lastLogoutField;
    private JButton editLeftCompositeButton;
    private JButton editRightCompositeButton;
    private JButton exportCSVButton;
    private GuiModelManager guiManager;
    private UserSearchAdapter searchAdapter;
    private RoleAssignementManager<User> roleAssignementManager;


    public UserTabForInternalManagement() {
        editLeftCompositeButton.setName("editLeftCompositeButton");
        editRightCompositeButton.setName("editRightCompositeButton");

        userList.setCellRenderer(new UserCellRenderer());
        userList.setModel(new DefaultListModel());
        userList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                if (userList.getSelectedIndex() == -1) {
                    setEditedUser(null);
                    removeUserButton.setEnabled(false);
                }
                else {
                    setEditedUser(getSelectedUser());
                    removeUserButton.setEnabled(true);
                }
            }
        });

        removeUserButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                guiManager.removeUser(getSelectedUser());
            }
        });
        userAddButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String name = JOptionPane.showInputDialog(userAddButton, "Nom de l'utilisateur:");
                if (name != null && !"".equals(name.trim())) {
                    try {
                        User user = new User(name.trim());
                        guiManager.addUser(user);
                        userList.requestFocus();
                        userList.setSelectedValue(user, true);
                    }
                    catch (ObjectAlreadyExistsException e) {
                        JOptionPane.showMessageDialog(userAddButton,
                                                      e.getMessage(), "Erreur",
                                                      JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
    }


    public void linkToTabRole(JTabbedPane tabbedPane, RoleTab roleTab) {
        editLeftCompositeButton.setAction(createEditAction(roleTab, tabbedPane, getUnassignedRoleList()));
        editRightCompositeButton.setAction(createEditAction(roleTab, tabbedPane, getAssignedRoleList()));
        new FileChooserActionListener(exportCSVButton, new CsvFileFilter(), new FileCommand() {
            public void execute(File file) throws IOException {
                FileUtil.saveContent(file, CsvCodec.toCsv(guiManager.getModel()));
            }
        }, roleTab.getFileChooserConfiguration());
    }


    public void initialize(GuiModelManager manager, SecurityEngineConfiguration engineConfiguration) {
        this.guiManager = manager;
        roleAssignementManager = new RoleAssignementManager<User>(this);
        roleAssignementManager.listenToThis(new UserRoleContainerEventManager(guiManager));

        GuiUtil.updateModel(userList, new ArrayList<User>(guiManager.getUsers()));

        guiManager.addUserListener(GuiUtil.<User>updateListModel(userList));

        guiManager.addUserContentListener(new GuiListener<ObjectContentEvent<User>>() {
            public void eventTriggered(ObjectContentEvent<User> event) {
                searchField.setText(searchField.getText());
            }
        });
        guiManager.addUserContentListener(GuiUtil.<User>selectUpdatedObject(userList));

        guiManager.addCompositeListener(new GuiListener<ObjectEvent<RoleComposite>>() {
            public void eventTriggered(ObjectEvent<RoleComposite> event) {
                User user = getSelectedUser();
                setEditedUser(user);
            }
        });

        connectSearchFeature();

        userList.repaint();
        userList.invalidate();
    }


    private void connectSearchFeature() {
        searchAdapter = new UserSearchAdapter(guiManager, userList);
        searchField.requestFocus();
        searchField.setName("searchField");
        SearchAction.activate(mainPanel, searchField, searchAdapter);
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (KeyEvent.VK_DOWN == e.getKeyCode()) {
                    userList.requestFocus();
                    userList.setSelectedIndex(userList.getFirstVisibleIndex());
                }
            }
        });
        guiManager.addUserListener(new GuiListener<ObjectEvent<User>>() {
            public void eventTriggered(ObjectEvent<User> event) {
                searchField.setText("");
            }
        });
    }


    private void setEditedUser(final User user) {
        if (user == null) {
            nameField.setText("");
            lastLoginField.setText("");
            lastLogoutField.setText("");
            roleAssignementManager.setRoleContainer(null);
        }
        else {
            roleAssignementManager.setRoleContainer(new UserRoleContainer(guiManager, user));
            nameField.setText(user.getName());
            lastLoginField.setText(format(user.getLastLogin()));
            lastLogoutField.setText(format(user.getLastLogout()));
        }
    }


    private User getSelectedUser() {
        return (User)userList.getSelectedValue();
    }


    public GuiModelManager getGuiManager() {
        return guiManager;
    }


    public JPanel getMainPanel() {
        return mainPanel;
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


    private static AbstractAction createEditAction(final RoleTab roleTab,
                                                   final JTabbedPane tabbedPane,
                                                   final JList roleList) {
        final AbstractAction action = new AbstractAction("Editer") {
            public void actionPerformed(ActionEvent e) {
                tabbedPane.setSelectedIndex(MainForm.ROLE_TAB_INDEX);
                roleTab.setSelectedComposite(roleList.getSelectedValue());
            }
        };
        action.setEnabled(false);
        roleList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }
                if (e.getFirstIndex() == -1) {
                    return;
                }
                Object selectedValue = roleList.getSelectedValue();
                action.setEnabled(selectedValue instanceof RoleComposite);
            }
        });
        return action;
    }


    private class UserCellRenderer extends DefaultListCellRenderer {
        private UserIcon icon = new UserIcon();
        private boolean directMatch;
        private ImageIcon notDirectMatchIcon = new ImageIcon(UserTabForInternalManagement.class.getResource(
              "role.png"));


        @Override
        public Component getListCellRendererComponent(JList list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            User user = (User)value;
            setText(user.getName());
            setIcon(icon.setUser(user));
            directMatch = searchAdapter.directMatch(user, searchField.getText());
            return this;
        }


        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!directMatch) {
                notDirectMatchIcon.paintIcon(this, g, getWidth() - 32, 0);
                g.setColor(new Color(0f, 0f, 0f, 0.2f));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }

    private class UserIcon implements Icon {
        private final ImageIcon mainIcon = new ImageIcon(getClass().getResource("user.png"));
        private final ImageIcon newPins = new ImageIcon(getClass().getResource("pins-new.png"));
        private final ImageIcon warningPins = new ImageIcon(getClass().getResource("pins-warning.png"));
        private User user;


        @SuppressWarnings({"MethodParameterNamingConvention"})
        public void paintIcon(Component c, Graphics g, int x, int y) {
            mainIcon.paintIcon(c, g, x, y);
            if (user.getLastLogin() == null) {
                newPins.paintIcon(c, g, x + 16, y + 16);
            }
            if (!guiManager.hasGrants(user)) {
                warningPins.paintIcon(c, g, x, y + 16);
            }
        }


        public int getIconWidth() {
            return mainIcon.getIconWidth();
        }


        public int getIconHeight() {
            return mainIcon.getIconHeight();
        }


        Icon setUser(User aUser) {
            this.user = aUser;
            return this;
        }
    }

    private static class CsvCodec {
        static final String NEW_LINE = "\r\n";


        private CsvCodec() {
        }


        public static String toCsv(ModelManager model) {
            StringBuilder csv = new StringBuilder();
            for (Role role : model.getRoles()) {
                csv.append(";").append(role.getName());
            }
            csv.append(NEW_LINE);

            for (User user : model.getUsers()) {
                csv.append(user.getName());
                List<Role> userRoles = model.getUserRoles(user);
                for (Role role : model.getRoles()) {
                    csv.append(";");
                    if (userRoles.contains(role)) {
                        csv.append("x");
                    }
                }
                csv.append(NEW_LINE);
            }

            return csv.toString();
        }
    }
}
