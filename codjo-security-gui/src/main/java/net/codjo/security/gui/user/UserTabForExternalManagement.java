package net.codjo.security.gui.user;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import net.codjo.security.common.message.SecurityEngineConfiguration;
import net.codjo.security.gui.model.GuiModelManager;
/**
 *
 */
public class UserTabForExternalManagement implements UserTab {
    private JButton startAdsBvGuiButton;
    private JPanel mainPanel;
    private String jnlpUrl;


    public UserTabForExternalManagement() {
        startAdsBvGuiButton.setName("startAdsBvGuiButton");
        startAdsBvGuiButton.setEnabled(false);
        startAdsBvGuiButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startJavaWebStart();
            }
        });
    }


    private void startJavaWebStart() {
        try {
            Runtime.getRuntime().exec("cmd /c start " + jnlpUrl);
        }
        catch (IOException e) {
            JOptionPane.showMessageDialog(startAdsBvGuiButton,
                                          e.getMessage(), "Impossible de lancer l'application",
                                          JOptionPane.ERROR_MESSAGE);
        }
    }


    public JPanel getMainPanel() {
        return mainPanel;
    }


    public void initialize(GuiModelManager manager, SecurityEngineConfiguration engineConfiguration) {
        jnlpUrl = engineConfiguration.getJnlp();
        startAdsBvGuiButton.setEnabled(true);
    }


    public void linkToTabRole(JTabbedPane tabbedPane, RoleTab roleTab) {
    }
}
