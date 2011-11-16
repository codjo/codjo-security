package net.codjo.security.gui.user;
import javax.swing.Action;
import javax.swing.JDesktopPane;
import net.codjo.agent.AclMessage;
import net.codjo.agent.UserId;
import net.codjo.agent.test.AgentContainerFixture;
import net.codjo.agent.test.Story;
import net.codjo.security.common.Constants;
import net.codjo.security.common.api.UserMock;
import net.codjo.security.common.message.DefaultModelManager;
import net.codjo.security.common.message.MessageBody;
import net.codjo.security.common.message.User;
import net.codjo.security.common.message.XmlCodec;
import net.codjo.security.gui.api.ConfigurationSession;
import net.codjo.security.gui.api.ConfigurationSessionFactory;
import net.codjo.security.gui.communication.DefaultConfigurationSession;
import org.uispec4j.Desktop;
import org.uispec4j.UISpecTestCase;

import static net.codjo.security.common.message.SecurityEngineConfiguration.defaultConfiguration;
import static net.codjo.security.gui.plugin.SecurityFunctions.ADMINISTRATE_USER;
/**
 *
 */
public class UserFormActionTest extends UISpecTestCase {
    private JDesktopPane jDesktopPane = new JDesktopPane();
    private Desktop desktop = new Desktop(jDesktopPane);
    private UserMock user = new UserMock().mockIsAllowedTo(true);
    private static final String WINDOW_TITLE = "Administration des utilisateurs";
    private Story story = new Story();
    private UserId id = UserId.createId("smith", "secret");


    public void test_security() throws Exception {
        UserFormAction action = new UserFormAction(jDesktopPane, user.mockIsAllowedTo(false), null);
        assertFalse(action.isEnabled());

        action = new UserFormAction(jDesktopPane, user.mockIsAllowedTo(ADMINISTRATE_USER, true), null);
        assertTrue(action.isEnabled());
    }


    public void test_displayForm() throws Exception {
        story.record().startTester("manager-mock")
              .registerToDF(Constants.SECURITY_MANAGER_SERVICE)
              .then()
              .receiveMessage()
              .replyWith(AclMessage.Performative.INFORM, securityModelWithOneUser());

        story.record().assertNumberOfAgentWithService(1, Constants.SECURITY_MANAGER_SERVICE);

        story.record().addAction(new AgentContainerFixture.Runnable() {
            public void run() throws Exception {
                assertDisplayForm();
            }
        });

        story.execute();
    }


    private void assertDisplayForm() {
        UserFormAction action = new UserFormAction(jDesktopPane, user, new MyConfigurationSessionFactory());

        assertEquals("Administration utilisateur", action.getValue(Action.NAME));

        action.actionPerformed(null);

        assertTrue(desktop.containsWindow(WINDOW_TITLE));
        assertTrue(desktop.getWindows().length == 1);
        assertFalse(action.isEnabled());

        assertTrue(desktop.getWindow(WINDOW_TITLE).getTabGroup().selectedTabEquals("Utilisateurs"));
        assertTrue(desktop.getWindow(WINDOW_TITLE).getListBox("userList").contains("smith"));

        desktop.getWindow(WINDOW_TITLE).getButton("Annuler").click();

        assertTrue(action.isEnabled());
        assertTrue(desktop.getWindows().length == 0);
    }


    @Override
    protected void setUp() throws Exception {
        story.doSetUp();
    }


    @Override
    protected void tearDown() throws Exception {
        story.doTearDown();
    }


    private String securityModelWithOneUser() {
        DefaultModelManager modelManager = new DefaultModelManager();
        modelManager.addUser(new User("smith"));
        return XmlCodec.toXml(new MessageBody(modelManager, defaultConfiguration()));
    }


    private class MyConfigurationSessionFactory implements ConfigurationSessionFactory {
        public ConfigurationSession createConfigurationSession() {
            return new DefaultConfigurationSession(user, id,
                                                   story.getContainer());
        }
    }
}
