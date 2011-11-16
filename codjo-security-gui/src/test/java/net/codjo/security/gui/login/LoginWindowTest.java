package net.codjo.security.gui.login;
import javax.swing.JFrame;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import net.codjo.security.gui.login.LoginConfig.Ldap;
import net.codjo.security.gui.login.LoginConfig.Server;
import net.codjo.security.gui.login.LoginWindow.LoginCallback;
import net.codjo.test.common.LogString;
import org.uispec4j.CheckBox;
import org.uispec4j.ComboBox;
import org.uispec4j.Key;
import org.uispec4j.PasswordField;
import org.uispec4j.TextBox;
import org.uispec4j.Trigger;
import org.uispec4j.UISpecTestCase;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;
/**
 *
 */
public class LoginWindowTest extends UISpecTestCase {
    private LogString log = new LogString();
    private LoginCallbackMock loginHelperMock = new LoginCallbackMock(log);
    private LoginWindow loginWindow;
    private LoginConfig loginConfig;


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        loginConfig = createLoginConfig(false, true);
        loginWindow = new LoginWindow(loginConfig, loginHelperMock, null);
    }


    public void test_frameIcon() throws Exception {
        loginConfig.setApplicationIcon("/images/security.icon.gif");
        loginWindow = new LoginWindow(loginConfig, loginHelperMock, null);
        Window window = new Window(loginWindow);

        JFrame frame = (JFrame)window.getAwtComponent();
        assertNotNull(frame.getIconImage());
    }


    public void test_frameIconNotSet() throws Exception {
        loginConfig.setApplicationIcon(null);
        loginWindow = new LoginWindow(loginConfig, loginHelperMock, null);
        Window window = new Window(loginWindow);

        JFrame frame = (JFrame)window.getAwtComponent();
        assertNull(frame.getIconImage());
    }


    public void test_loginCallback_doConnectNotInThreadSwing() throws Exception {
        LoginCallbackMock loginCallbackMock = new LoginCallbackMock(log);
        loginWindow = new LoginWindow(loginConfig, loginCallbackMock, null);

        Window window = new Window(loginWindow);
        TextBox login = window.getTextBox("login");
        PasswordField password = window.getPasswordField("password");

        login.setText("login");
        password.setPassword("password");
        Window splashScreen = clickButton("OK", window);

        assertLog("doConnect(login, password, localhost:35700, null)");

        assertFalse(splashScreen.isVisible());
    }


    public void test_buttonsEnability() {
        Window window = new Window(loginWindow);
        window.getTextBox("Veuillez saisir vos identifiants Windows :");
        window.getTextBox("Compte");
        window.getTextBox("Mot de passe");
        TextBox login = window.getTextBox("login");
        PasswordField password = window.getPasswordField("password");
        ComboBox comboBox = window.getComboBox("server");

        assertEquals("Connexion à GABI v-1.05.00.00", window.getTitle());
        assertEquals("", login.getText());
        assertTrue(password.passwordEquals(""));
        assertTrue(comboBox.contentEquals(new String[]{"Développement", "Intégration"}));
        assertTrue(comboBox.selectionEquals("Développement"));
        assertFalse(window.getButton("OK").isEnabled());
        assertTrue(window.getButton("Quitter").isEnabled());

        login.pressKey(Key.L);
        assertFalse(window.getButton("OK").isEnabled());

        password.setPassword("P");
        assertTrue(window.getButton("OK").isEnabled());

        login.setText("");
        assertFalse(window.getButton("OK").isEnabled());
    }


    public void test_multildap() {
        loginConfig = createLoginConfig(false, false);
        loginWindow = new LoginWindow(loginConfig, loginHelperMock, null);
        Window window = new Window(loginWindow);
        ComboBox comboBox = window.getComboBox("ldap");
        assertTrue(comboBox.contentEquals(new String[]{"AM", "gdoLdap"}));
    }


    public void test_okSingleLDapButton() throws Exception {
        Window window = fillLoginWindow();

        Window splash = clickButton("OK", window);

        assertEquals(AnimatedSplash.class, splash.getAwtComponent().getClass());

        assertLog("doConnect(USER, PASSWORD, localhost:35700, null)");
    }


    public void test_okDefaultLDapButton() throws Exception {
        loginConfig = createLoginConfig(false, false);
        loginWindow = new LoginWindow(loginConfig, loginHelperMock, null);

        Window window = fillLoginWindow();
        window.getComboBox("ldap").select("AM");
        clickButton("OK", window);

        window = fillLoginWindow();
        window.getComboBox("ldap").selectionEquals("AM").check();
        window.getComboBox("ldap").select("gdoLdap");
        clickButton("OK", window);

        window = fillLoginWindow();
        window.getComboBox("ldap").selectionEquals("gdoLdap").check();
    }


    public void test_okMultiLDapButton() throws Exception {
        loginConfig = createLoginConfig(false, false);
        loginWindow = new LoginWindow(loginConfig, loginHelperMock, null);

        Window window = fillLoginWindow();
        window.getComboBox("ldap").select("gdoLdap");

        clickButton("OK", window);

        assertLog("doConnect(USER, PASSWORD, localhost:35700, gdoLdap)");
    }


    public void test_extraComponentContainer() {
        loginConfig = createLoginConfig(false, false);
        loginWindow = new LoginWindow(loginConfig, loginHelperMock, new JTextField("specific text"));
        Window window = new Window(loginWindow);

        TextBox extraOne = window.getTextBox("specific text");

        assertTrue(extraOne.isVisible());
    }


    private Window fillLoginWindow() {
        Window window = new Window(loginWindow);
        window.getTextBox("login").setText("USER");
        window.getPasswordField("password").setPassword("PASSWORD");
        return window;
    }


    public void test_defaultAccountValue() {
        loginConfig.setDefaultLogin("USER");
        loginConfig.setDefaultPassword("PASSWORD");
        loginWindow = new LoginWindow(loginConfig, loginHelperMock, null);
        Window window = new Window(loginWindow);

        assertEquals("USER", window.getTextBox("login").getText());
        assertTrue(window.getPasswordField("password").passwordEquals("PASSWORD"));
        assertTrue(window.getButton("OK").isEnabled());
        assertTrue(window.getButton("Quitter").isEnabled());

        assertFalse(window.getComboBox("ldap").isVisible());
    }


    public void test_defaultLoginValue() {
        loginConfig = createLoginConfig(true, true);
        loginWindow = new LoginWindow(loginConfig, loginHelperMock, null);
        Window window = new Window(loginWindow);
        String dicid = System.getProperty("user.name");

        assertEquals(dicid, window.getTextBox("login").getText());
        assertTrue(window.getPasswordField("password").passwordEquals(""));
        assertFalse(window.getButton("OK").isEnabled());
        assertTrue(window.getButton("Quitter").isEnabled());
    }


    public void test_quitButton() throws Exception {
        Window window = new Window(loginWindow);

        window.getButton("Quitter").click();

        assertLog("doQuit()");
    }


    public void test_okButton() throws Exception {
        Window window = new Window(loginWindow);

        window.getTextBox("login").setText("USER");
        window.getPasswordField("password").setPassword("PASSWORD");
        clickButton("OK", window);

        assertLog("doConnect(USER, PASSWORD, localhost:35700, null)");
    }


    public void test_seePassword() throws Exception {
        Window window = new Window(loginWindow);
        JPasswordField passwordField = (JPasswordField)window.getPasswordField("password").getAwtComponent();
        CheckBox seePasswordCheckBox = window.getCheckBox("seePassword");
        assertEquals('*', passwordField.getEchoChar());

        seePasswordCheckBox.select();
        assertEquals(0, passwordField.getEchoChar());

        seePasswordCheckBox.unselect();
        assertEquals('*', passwordField.getEchoChar());
    }


    public void test_unaivailableSplahImageSet() throws Exception {
        loginConfig.setApplicationSplashImage("/images/dummy.jpg");
        loginWindow = new LoginWindow(loginConfig, loginHelperMock, null);

        Window window = new Window(loginWindow);

        window.getTextBox("login").setText("USER");
        window.getPasswordField("password").setPassword("PASSWORD");
        clickButton("OK", window);
    }


    private static LoginConfig createLoginConfig(boolean singleServer, boolean singleLdap) {
        LoginConfig loginConfig = new LoginConfig();
        loginConfig.setApplicationName("GABI");
        loginConfig.setApplicationVersion("1.05.00.00");
        loginConfig.setApplicationIcon(null);
        loginConfig.setApplicationSplashImage("/images/splash.jpg");
        if (!singleServer) {
            loginConfig.addServer(new Server("Développement", "localhost:35700"));
        }
        loginConfig.addServer(new Server("Intégration", "martinique:35700"));
        if (!singleLdap) {
            loginConfig.addLdap(new Ldap("default", "AM"));
            loginConfig.addLdap(new Ldap("gdo2", "gdoLdap"));
        }
        return loginConfig;
    }


    private Window clickButton(final String name, final Window window) {
        return WindowInterceptor.run(new Trigger() {
            public void run() throws Exception {
                window.getButton(name).click();
            }
        });
    }


    private void assertLog(String expected) {
        AssertionError error = null;
        long start = System.currentTimeMillis();
        do {
            try {
                Thread.sleep(50);
            }
            catch (InterruptedException e) {
                ;
            }
            try {
                log.assertContent(expected);
            }
            catch (AssertionError e) {
                error = e;
            }
        }
        while (System.currentTimeMillis() - start <= 1000);

        if (error != null) {
            throw error;
        }
    }


    private static class LoginCallbackMock implements LoginCallback {
        private final LogString log;


        private LoginCallbackMock(LogString log) {
            this.log = log;
        }


        public void doConnect(String login, String password, Server server, Ldap ldap) {
            log.call("doConnect",
                     login,
                     password,
                     server == null ? null : server.getValue(),
                     ldap == null ? null : ldap.getValue());
        }


        public void doQuit() {
            log.call("doQuit");
        }
    }
}
