package net.codjo.security.server.login;
import net.codjo.agent.Agent;
import net.codjo.agent.Aid;
import net.codjo.agent.UserId;
import net.codjo.agent.test.AgentContainerFixture;
import net.codjo.agent.test.DummyAgent;
import net.codjo.agent.test.Story;
import net.codjo.plugin.common.session.SessionListenerMock;
import net.codjo.plugin.common.session.SessionManager;
import net.codjo.test.common.LogString;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static net.codjo.agent.test.AgentAssert.log;
/**
 *
 */
public class SessionLifecycleBehaviourTest {
    private Story story = new Story();
    private LogString log = new LogString();
    private SessionManager sessionManager = new SessionManager();
    private UserId userId = UserId.decodeUserId("smith/6e594b55386d41376c37593d/5/1");
    private SessionLifecycleBehaviour sessionLifecycleBehaviour;


    @Before
    public void setUp() throws Exception {
        sessionLifecycleBehaviour = new SessionLifecycleBehaviour(sessionManager);
        sessionManager.addListener(new SessionListenerMock(log));
        story.doSetUp();
    }


    @After
    public void tearDown() throws Exception {
        story.doTearDown();
    }


    @Test
    public void test_declare() throws Exception {
        sessionLifecycleBehaviour.declare(new Aid("loginAgent1"), userId);
        sessionLifecycleBehaviour.declare(new Aid("loginAgent2"), UserId.createId("myLogin", "p"));

        log.assertAndClear("handleSessionStart(smith)", "handleSessionStart(myLogin)");
    }


    @Test
    public void test_kill() throws Exception {
        sessionLifecycleBehaviour.declare(new Aid("loginAgent1"), userId);
        sessionLifecycleBehaviour.declare(new Aid("loginAgent2"), UserId.createId("myLogin", "p"));
        log.assertAndClear("handleSessionStart(smith)", "handleSessionStart(myLogin)");

        story.record().startAgent("SLA", createServerLoginAgent(sessionLifecycleBehaviour));
        story.record().assertContainsAgent("SLA");

        story.record().startAgent("loginAgent1", createAgent());
        story.record().assertContainsAgent("loginAgent1");

        story.record().addAction(killAgent("loginAgent1"));
        story.record().assertNotContainsAgent("loginAgent1");

        story.record().addAssert(log(log, "handleSessionStop(smith)"));

        story.execute();
    }


    private AgentContainerFixture.Runnable killAgent(final String agentName) {
        return new AgentContainerFixture.Runnable() {
            public void run() throws Exception {
                Thread.sleep(100);
                story.getAgentContainerFixture().killAgent(agentName);
            }
        };
    }


    private DummyAgent createAgent() {
        return new DummyAgent();
    }


    private Agent createServerLoginAgent(SessionLifecycleBehaviour behaviour) {
        return new DummyAgent(behaviour);
    }
}
