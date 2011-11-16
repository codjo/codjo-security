package net.codjo.security.gui.login;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import net.codjo.gui.toolkit.button.HyperLink;
/**
 *
 */
public class ExternalAccountLockedDialog {
    private final String unlockUrl;
    private JPanel mainPanel;
    private HyperLink link;


    public ExternalAccountLockedDialog(String unlockUrl) {
        this.unlockUrl = unlockUrl;
        link.setText(unlockUrl);
    }


    public void show(Component parentComponent) {
        link.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    Runtime.getRuntime()
                          .exec(String.format("cmd /c start %s", unlockUrl));
                }
                catch (IOException e) {
                    JOptionPane.showMessageDialog(link,
                                                  e.getMessage(),
                                                  "Impossible d'ouvrir le lien",
                                                  JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        Object[] options = {"OK"};
        JOptionPane optionPane = new JOptionPane(mainPanel,
                                                 JOptionPane.ERROR_MESSAGE,
                                                 JOptionPane.DEFAULT_OPTION,
                                                 null,
                                                 options,
                                                 options[0]);
        JDialog dialog = optionPane.createDialog(parentComponent, "Erreur");
        dialog.setResizable(true);
        dialog.setVisible(true);
    }
}
