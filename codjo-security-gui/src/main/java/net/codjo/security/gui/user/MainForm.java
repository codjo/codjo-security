package net.codjo.security.gui.user;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import net.codjo.gui.toolkit.HelpButton;
import net.codjo.security.common.message.ModelManager;
import net.codjo.security.common.message.Role;
import net.codjo.security.common.message.SecurityEngineConfiguration;
import net.codjo.security.common.message.SecurityEngineConfiguration.UserManagement;
import net.codjo.security.common.message.User;
import net.codjo.security.common.message.XmlCodec;
import net.codjo.security.gui.model.GuiModelManager;
import net.codjo.security.gui.user.FileChooserActionListener.CustomFileFilter;
import net.codjo.security.gui.user.FileChooserActionListener.FileCommand;
import net.codjo.util.file.FileUtil;

import static java.text.MessageFormat.format;
/**
 *
 */
public class MainForm {
    static final int ROLE_TAB_INDEX = 1;
    private static final String SECURITY_FILE_POSTFIX = ".security";

    private JPanel mainPanel;
    private JButton okButton;
    private JButton cancelButton;
    private UserAuditTab userAuditTab;
    private RoleAuditTab roleAuditTab;
    private RoleTab roleTab;
    private JTabbedPane tabbedPane;
    private HelpButton helpButton;
    private JButton exportButton;
    private JButton importButton;
    private GuiModelManager guiManager;


    MainForm(SecurityEngineConfiguration engineConfiguration, ModelManager modelManager) {
        this();
        setManager(modelManager, engineConfiguration);
    }


    public MainForm() {
        mainPanel.setPreferredSize(new Dimension(650, 550));

        FileChooserConfiguration fileChooserConfiguration = new FileChooserConfiguration();
        SecurityFileFilter securityFileFilter = new SecurityFileFilter();
        new FileChooserActionListener(importButton, securityFileFilter, new FileCommand() {
            public void execute(File file) throws IOException {
                importSecurityModel(file);
            }
        }, fileChooserConfiguration);

        new FileChooserActionListener(exportButton, securityFileFilter, new FileCommand() {
            public void execute(File file) throws IOException {
                FileUtil.saveContent(file, XmlCodec.toXml(guiManager.getModel()));
            }
        }, fileChooserConfiguration);
        roleTab.initFileChooserConfiguration(fileChooserConfiguration);
    }


    public void setManager(ModelManager manager, SecurityEngineConfiguration engineConfiguration) {
        UserTab userTab = instanciateUserTabFor(engineConfiguration.getUserManagementType());
        tabbedPane.insertTab("Utilisateurs", null, userTab.getMainPanel(), null, 0);
        tabbedPane.setSelectedIndex(0);
        userTab.linkToTabRole(tabbedPane, roleTab);

        guiManager = new GuiModelManager(manager);

        InputMap inputMap = mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

        inputMap.put(KeyStroke.getKeyStroke("control Z"), "undo");
        mainPanel.getActionMap().put("undo", guiManager.getUndoAction());

        inputMap.put(KeyStroke.getKeyStroke("control Y"), "redo");
        mainPanel.getActionMap().put("redo", guiManager.getRedoAction());

        userTab.initialize(guiManager, engineConfiguration);
        userAuditTab.initialize(guiManager);
        roleAuditTab.initialize(guiManager);
        roleTab.initialize(guiManager);

        helpButton.setHelpUrl(engineConfiguration.getHelpUrl());
    }


    static UserTab instanciateUserTabFor(UserManagement userManagementType) {
        String className = "net.codjo.security.gui.user.UserTabFor" + userManagementType.getId();
        try {
            return (UserTab)Class.forName(className).newInstance();
        }
        catch (Exception e) {
            throw new UnsupportedOperationException(format("Aucune IHM pour l''onglet Utilisateurs ({0})",
                                                           userManagementType),
                                                    e);
        }
    }


    public JPanel getMainPanel() {
        return mainPanel;
    }


    public void setGuiResultHandler(final GuiResultHandler guiResultHandler) {
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                guiResultHandler.handleCancel();
            }
        });
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                int selectedOption =
                      JOptionPane.showConfirmDialog(okButton,
                                                    "Voulez-vous vraiment enregistrer le modèle ?",
                                                    "Confirmation d'enregistrement",
                                                    JOptionPane.OK_CANCEL_OPTION);
                if (selectedOption == JOptionPane.CANCEL_OPTION) {
                    return;
                }
                guiResultHandler.handleValidate();
            }
        });
    }


    public GuiModelManager getGuiManager() {
        return guiManager;
    }


    private void importSecurityModel(File selectedFile) throws IOException {
        if (!selectedFile.exists()) {
            throw new IOException("Fichier introuvable");
        }
        FileReader reader = new FileReader(selectedFile);
        try {
            ModelManager newManager = XmlCodec.createFromXml(reader);

            for (User user : copyList(guiManager.getUsers())) {
                guiManager.removeUser(user);
            }
            for (Role role : copyList(guiManager.getAllRoles())) {
                guiManager.removeRole(role);
            }

            for (Role role : copyList(newManager.getRoles())) {
                guiManager.addRole(role);
            }
            for (User user : copyList(newManager.getUsers())) {
                guiManager.addUser(user);
                for (Role role : copyList(newManager.getUserRoles(user))) {
                    guiManager.addRoleToUser(role, user);
                }
            }
        }
        finally {
            reader.close();
        }
    }


    private static <T> Collection<T> copyList(Collection<T> list) {
        List<T> copy = new ArrayList<T>();
        copy.addAll(list);
        return copy;
    }


    private static class SecurityFileFilter extends CustomFileFilter {

        @Override
        public String getPostfix() {
            return SECURITY_FILE_POSTFIX;
        }


        @Override
        public String getDescription() {
            return "Modèle de sécurité (*" + getPostfix() + ")";
        }
    }
}
