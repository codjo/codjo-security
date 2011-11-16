package net.codjo.security.gui.user;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import javax.swing.AbstractAction;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import net.codjo.security.common.api.User;
import net.codjo.security.common.message.ModelManager;
import net.codjo.security.common.message.SecurityEngineConfiguration;
import net.codjo.security.gui.api.ConfigurationSession;
import net.codjo.security.gui.api.ConfigurationSessionFactory;
import net.codjo.security.gui.api.OpenHandler;
import net.codjo.security.gui.api.SaveHandler;
import net.codjo.security.gui.plugin.SecurityFunctions;
import org.apache.log4j.Logger;
/**
 *
 */
public class UserFormAction extends AbstractAction {
    private final Logger logger = Logger.getLogger(AbstractAction.class);
    private final JDesktopPane desktopPane;
    private final ConfigurationSessionFactory sessionFactory;


    public UserFormAction(JDesktopPane desktopPane, User user, ConfigurationSessionFactory sessionFactory) {
        super("Administration utilisateur");
        this.desktopPane = desktopPane;
        this.sessionFactory = sessionFactory;
        setEnabled(user.isAllowedTo(SecurityFunctions.ADMINISTRATE_USER));
    }


    public void actionPerformed(ActionEvent event) {
        try {
            JInternalFrame frame =
                  new JInternalFrame("Administration des utilisateurs", true, false, true, true);

            MainForm mainForm = new MainForm();
            ConfigurationSession session = sessionFactory.createConfigurationSession();
            ConfigurationSessionHandler handler = new ConfigurationSessionHandler(frame, mainForm, session);

            mainForm.setGuiResultHandler(handler);
            frame.setContentPane(mainForm.getMainPanel());

            session.open(handler);

            desktopPane.add(frame);
            frame.pack();
            frame.setVisible(true);
            frame.setSelected(true);
            setEnabled(false);
        }
        catch (Exception ex) {
            logger.error(ex);
            JOptionPane.showInternalMessageDialog(desktopPane, ex.getLocalizedMessage(),
                                                  "Echec", JOptionPane.ERROR_MESSAGE);
        }
    }


    private class ConfigurationSessionHandler implements GuiResultHandler, OpenHandler, SaveHandler {
        private final JInternalFrame frame;
        private final MainForm mainForm;
        private final ConfigurationSession session;


        private ConfigurationSessionHandler(JInternalFrame frame,
                                            MainForm userForm,
                                            ConfigurationSession session) {
            this.frame = frame;
            this.mainForm = userForm;
            this.session = session;
        }


        public void handleCancel() {
            closeSession();
        }


        public void handleValidate() {
            try {
                session.save(this);
            }
            catch (SecurityException e) {
                JOptionPane.showInternalMessageDialog(desktopPane,
                                                      e.getLocalizedMessage(),
                                                      "Echec",
                                                      JOptionPane.ERROR_MESSAGE);
                closeSession();
            }
        }


        public void handleReceiveSecurityModel(final ModelManager modelManager,
                                               final SecurityEngineConfiguration engineConfiguration) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    mainForm.setManager(modelManager, engineConfiguration);
                }
            });
        }


        public void handleSaveSucceed() {
            closeSession();
        }


        public void handleCommunicationError(String badThingsHappen) {
            JOptionPane.showInternalMessageDialog(desktopPane, badThingsHappen,
                                                  "Erreur", JOptionPane.ERROR_MESSAGE);
            closeSession();
        }


        private void closeSession() {
            closeInternalFrame();
            session.close();
            setEnabled(true);
        }


        private void closeInternalFrame() {
            try {
                frame.setClosed(true);
            }
            catch (PropertyVetoException e) {
                logger.warn("Erreur impossible :), impossible de fermer la fenetre", e);
            }
        }
    }
}
