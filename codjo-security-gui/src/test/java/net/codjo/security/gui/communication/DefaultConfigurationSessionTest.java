package net.codjo.security.gui.communication;
import junit.framework.TestCase;
import net.codjo.agent.test.AgentContainerFixture;
import net.codjo.agent.test.Story;
import net.codjo.security.common.api.UserMock;
import net.codjo.security.gui.api.OpenHandlerAdapter;
/**
 *
 */
public class DefaultConfigurationSessionTest extends TestCase {
    private UserMock user = new UserMock().mockIsAllowedTo(true);
    private Story story = new Story();


    public void test_security() throws Exception {
        DefaultConfigurationSession action = new DefaultConfigurationSession(user, null, null);

        user.mockIsAllowedTo(false);
        try {
            action.open(new MyOpenHandlerAdapter());
            fail();
        }
        catch (SecurityException e) {
            assertEquals("Vous n'avez pas les droits de configurer la couche sécurité.", e.getMessage());
        }
    }


    public void test_alreadyOpen() throws Exception {
        story.record().startTester("security-configurator");

        final DefaultConfigurationSession action = new DefaultConfigurationSession(user,
                                                                                   null,
                                                                                   story.getContainer());

        story.record().addAction(new AgentContainerFixture.Runnable() {
            public void run() throws Exception {
                try {
                    action.open(new MyOpenHandlerAdapter());
                    fail();
                }
                catch (SecurityException e) {
                    assertEquals("Une session de configuration est deja ouverte.", e.getLocalizedMessage());
                }
            }
        });

        story.execute();
    }


    private static class MyOpenHandlerAdapter extends OpenHandlerAdapter {
    }
}
