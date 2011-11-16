package net.codjo.security.gui.user;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import net.codjo.security.common.message.SecurityEngineConfiguration;
import net.codjo.security.gui.model.GuiModelManager;
/**
 *
 */
public interface UserTab {
    JPanel getMainPanel();


    void initialize(GuiModelManager manager, SecurityEngineConfiguration engineConfiguration);


    void linkToTabRole(JTabbedPane tabbedPane, RoleTab roleTab);
}
