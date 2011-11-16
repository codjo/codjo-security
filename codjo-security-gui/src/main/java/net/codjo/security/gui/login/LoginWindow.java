package net.codjo.security.gui.login;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.prefs.Preferences;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.codjo.gui.toolkit.swing.SwingWorker;
import net.codjo.security.gui.login.LoginConfig.Ldap;
import net.codjo.security.gui.login.LoginConfig.Server;
import org.apache.log4j.Logger;
/**
 *
 */
public class LoginWindow extends JFrame {
    private static final Logger LOG = Logger.getLogger(LoginWindow.class);
    private static final String LAST_SELECTED_LDAP = "LAST_SELECTED_LDAP";
    private final LoginConfig loginConfig;
    private final LoginCallback loginCallback;
    private JPanel mainPanel;
    private JTextField loginField;
    private JPasswordField passwordField;
    private JCheckBox seePasswordCheckBox;
    private JComboBox ldapCombo;
    private JComboBox serverCombo;
    private JButton okButton;
    private JButton quitButton;
    private JPanel extraComponentContainer;


    public LoginWindow(LoginConfig loginConfig, LoginCallback loginCallback, JComponent extraComponent) {
        this.loginConfig = loginConfig;
        this.loginCallback = loginCallback;

        if (extraComponent != null) {
            extraComponentContainer.add(extraComponent, BorderLayout.CENTER);
        }

        setLayout(new BorderLayout());
        add(mainPanel);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initNames();
        initIcon();
        initGui();
        initServerCombo();
        initLDapCombo();

        if (isMultiEnvironment()) {
            loginField.setText(loginConfig.getDefaultLogin());
            passwordField.setText(loginConfig.getDefaultPassword());
        }
        else {
            loginField.setText(System.getProperty("user.name"));
            passwordField.requestFocus();
        }
    }


    public void resetPasswordAndFocus() {
        passwordField.setText("");
        passwordField.requestFocusInWindow();
    }


    private void initIcon() {
        String applicationIcon = loginConfig.getApplicationIcon();
        if (applicationIcon != null) {
            URL iconResource = getClass().getResource(applicationIcon);
            setIconImage(new ImageIcon(iconResource).getImage());
        }
    }


    private void initNames() {
        loginField.setName("login");
        passwordField.setName("password");
        seePasswordCheckBox.setName("seePassword");
        ldapCombo.setName("ldap");
        serverCombo.setName("server");
        okButton.setName("ok");
        quitButton.setName("quit");
    }


    private void initGui() {
        okButton.setActionCommand("OK");
        okButton.setMnemonic(KeyEvent.VK_O);
        okButton.setEnabled(false);

        quitButton.setActionCommand("Quitter");
        quitButton.setMnemonic(KeyEvent.VK_Q);

        DocumentListener listener =
              new ButtonEnabilityListener(loginField, passwordField, okButton);
        loginField.getDocument().addDocumentListener(listener);
        passwordField.getDocument().addDocumentListener(listener);

        ldapCombo.setVisible(isMultiLDap());
        serverCombo.setVisible(isMultiEnvironment());

        setTitle(createTitle());
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        getRootPane().setDefaultButton(okButton);
        pack();
        centerWindow();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                loginCallback.doQuit();
            }
        });
        passwordField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                passwordField.selectAll();
            }
        });
        seePasswordCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                char echoChar;
                if (seePasswordCheckBox.isSelected()) {
                    echoChar = 0;
                }
                else {
                    echoChar = '*';
                }
                passwordField.setEchoChar(echoChar);
            }
        });
        quitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent event) {
                loginCallback.doQuit();
            }
        });
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent event) {
                savePreferences();

                final AnimatedSplash splash = new AnimatedSplash(LoginWindow.this,
                                                                 "Connexion en cours, veuillez patienter ...",
                                                                 getSplashIcon());
                setVisible(false);
                splash.start();
                new SwingWorker() {
                    @Override
                    public Object construct() {
                        loginCallback.doConnect(loginField.getText(),
                                                new String(passwordField.getPassword()),
                                                (Server)serverCombo.getSelectedItem(),
                                                (Ldap)ldapCombo.getSelectedItem());
                        return true;
                    }


                    @Override
                    public void finished() {
                        splash.stop();
                    }
                }.start();
            }
        });
    }


    private void initServerCombo() {
        for (Server server : loginConfig.getServerList()) {
            serverCombo.addItem(server);
        }
        serverCombo.setSelectedIndex(0);
    }


    private void initLDapCombo() {
        if (isMultiLDap()) {
            for (Ldap ldap : loginConfig.getLdapList()) {
                ldapCombo.addItem(ldap);
            }
            ldapCombo.setSelectedIndex(0);
            loadPreferences();
        }
    }


    private boolean isMultiLDap() {
        return loginConfig.getLdapList().size() > 1;
    }


    private boolean isMultiEnvironment() {
        return loginConfig.getServerList().size() > 1;
    }


    private void loadPreferences() {
        String selectedLdap = Preferences.userRoot().get(LAST_SELECTED_LDAP, "");
        if (selectedLdap != null) {
            for (int i = 0; i < ldapCombo.getItemCount(); i++) {
                Ldap ldap = (Ldap)ldapCombo.getItemAt(i);
                if (ldap.getKey().equals(selectedLdap)) {
                    ldapCombo.setSelectedIndex(i);
                }
            }
        }
    }


    private void savePreferences() {
        if (isMultiLDap()) {
            String selectedLDap = ((Ldap)ldapCombo.getSelectedItem()).getKey();
            Preferences.userRoot().put(LAST_SELECTED_LDAP, selectedLDap);
        }
    }


    private String createTitle() {
        return new StringBuilder().append("Connexion à ")
              .append(loginConfig.getApplicationName())
              .append(isMultiEnvironment() ? "" : " " + loginConfig.getServerList().get(0).getKey())
              .append(" v-")
              .append(loginConfig.getApplicationVersion())
              .toString();
    }


    private void centerWindow() {
        setLocationRelativeTo(null);
    }


    private ImageIcon getSplashIcon() {
        URL imageResource = null;
        String splashImage = loginConfig.getApplicationSplashImage();
        if (splashImage != null) {
            try {
                imageResource = getClass().getResource(splashImage);
            }
            catch (Exception e) {
                LOG.warn(String.format(
                      "Le splash screen '%s' est introuvable, utilisation de l'image par défaut.",
                      splashImage));
            }
        }
        if (imageResource == null) {
            imageResource = getClass().getResource("/images/security.logo.jpg");
        }
        return new ImageIcon(imageResource);
    }


    public static interface LoginCallback {
        void doConnect(String login, String password, Server server, Ldap ldap);


        void doQuit();
    }

    private static class ButtonEnabilityListener implements DocumentListener {
        private final JTextField loginField;
        private final JPasswordField passwordField;
        private final JButton okButton;


        ButtonEnabilityListener(JTextField loginField, JPasswordField passwordField,
                                JButton okButton) {
            this.loginField = loginField;
            this.passwordField = passwordField;
            this.okButton = okButton;
        }


        public void changedUpdate(DocumentEvent event) {
            okButton.setEnabled(loginField.getText().length() > 0
                                && passwordField.getPassword().length > 0);
        }


        public void insertUpdate(DocumentEvent event) {
            changedUpdate(event);
        }


        public void removeUpdate(DocumentEvent event) {
            changedUpdate(event);
        }
    }
}