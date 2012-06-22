package net.codjo.security.gui.plugin;
import net.codjo.i18n.gui.plugin.InternationalizationGuiPlugin;
import net.codjo.plugin.common.ApplicationCoreMock;
import net.codjo.plugin.gui.GuiConfigurationMock;
import net.codjo.security.common.api.User;
import net.codjo.security.common.api.UserMock;
import net.codjo.security.gui.api.ConfigurationSession;
import net.codjo.security.gui.api.ConfigurationSessionFactory;
import net.codjo.security.gui.communication.DefaultConfigurationSession;
import net.codjo.test.common.LogString;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
/**
 *
 */
public class SecurityGuiPluginTest {
    private LogString log = new LogString();
    private ApplicationCoreMock coreMock = new ApplicationCoreMock(log);
    private SecurityGuiPlugin guiPlugin = new SecurityGuiPlugin(coreMock, new InternationalizationGuiPlugin(coreMock));


    @Test
    public void test_other() throws Exception {
        guiPlugin.initContainer(null);
        guiPlugin.start(null);
        guiPlugin.stop();
    }


    @Test
    public void test_initGui() throws Exception {
        log.assertAndClear("setMainBehaviour(SecurityMainBehaviour)",
                           "addGlobalComponent(ConfigurationSessionFactory, MySessionFactory)");

        guiPlugin.initGui(new GuiConfigurationMock(log));

        log.assertContent("registerAction(SecurityGuiPlugin, UserForm, UserFormAction)");
    }


    @Test
    public void test_user() throws Exception {
        SecurityGuiPluginOperations operations = guiPlugin.getOperations();
        assertNotNull(operations);

        try {
            operations.getUser();
            fail();
        }
        catch (ConfigurationException ex) {
            assertEquals(SecurityGuiPlugin.USER_NOT_DEFINED_ERROR, ex.getLocalizedMessage());
        }

        UserMock user = new UserMock();
        coreMock.addGlobalComponent(User.class, user);
        assertSame(user, operations.getUser());
    }


    @Test
    public void test_configurationSession() throws Exception {
        SecurityGuiPluginOperations operations = guiPlugin.getOperations();

        coreMock.addGlobalComponent(User.class, new UserMock());

        ConfigurationSession session = operations.createConfigurationSession();
        assertNotNull(session);
        assertTrue(session instanceof DefaultConfigurationSession);
    }


    @Test
    public void test_configurationSessionFactory_inPico() throws Exception {
        assertNotNull(coreMock.getGlobalComponent(ConfigurationSessionFactory.class));
    }
}
