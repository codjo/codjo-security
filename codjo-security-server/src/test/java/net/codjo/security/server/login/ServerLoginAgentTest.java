package net.codjo.security.server.login;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import net.codjo.agent.AclMessage;
import net.codjo.agent.AclMessage.Performative;
import net.codjo.agent.Agent;
import net.codjo.agent.Aid;
import net.codjo.agent.MessageTemplate;
import net.codjo.agent.ServiceHelper;
import net.codjo.agent.UserId;
import net.codjo.agent.test.AssertMatchExpression;
import net.codjo.agent.test.Story;
import net.codjo.agent.test.SubStep;
import net.codjo.plugin.common.session.SessionListenerMock;
import net.codjo.plugin.common.session.SessionManager;
import net.codjo.security.common.SecurityLevel;
import net.codjo.security.common.login.LoginAction;
import net.codjo.security.common.login.LoginEvent;
import net.codjo.security.common.login.LoginProtocol;
import net.codjo.security.server.api.SecurityService;
import net.codjo.security.server.api.SecurityServiceHelperMock;
import net.codjo.test.common.LogString;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static net.codjo.agent.test.AgentAssert.log;
import static net.codjo.test.common.matcher.JUnitMatchers.fail;
/**
 *
 */
public class ServerLoginAgentTest {
    public static final String SERVER_LOGIN_AGENT = "server-login-agent";
    private Story story = new Story();
    private LogString log = new LogString();
    private SessionManager sessionManager;


    @Test
    public void test_simpleLogin() throws Exception {
        story.installService(SecurityServiceMock.class);

        story.record().startAgent(SERVER_LOGIN_AGENT, new ServerLoginAgent(sessionManager, "v1"));

        story.record().startTester("john")
              .sendMessage(withLoginRequest("john", "secret"))
              .then()
              .receiveMessage()
              .assertReceivedMessage(loginEvent("john / logged=true"))
              .add(commitSuicide());

        story.record().addAssert(log(log, "handleSessionStart(john), handleSessionStop(john)"));

        story.execute();
    }


    @Test
    public void test_badLoginActionThatKillAgent() throws Exception {
        story.installService(SecurityServiceMock.class);
        story.record().startAgent(SERVER_LOGIN_AGENT, new ServerLoginAgent(sessionManager, "v1"));

        story.record().startTester("a dummy agent")
              .sendMessage(withBadContent("I'm not a LoginAction"))
              .then()
              .receiveMessage()
              .assertReceivedMessage(loginEvent("not logged / java.lang.ClassCastException: java.lang.String"));

        story.record().assertContainsAgent(SERVER_LOGIN_AGENT);

        story.execute();
    }


    @Test
    public void test_badErrorDuringEventThatKillAgent() throws Exception {
        story.installService(SecurityServiceMock.class);
        sessionManager = new SessionManager();
        sessionManager.addListener(new SessionListenerMock(log) {
            @Override
            public void handleSessionStop(UserId userId) {
                super.handleSessionStop(userId);
                fail("uncaught exception in SessionLifecycleBehaviour");
            }
        });

        story.record().startAgent(SERVER_LOGIN_AGENT, new ServerLoginAgent(sessionManager, "v1"));

        story.record().startTester("john")
              .sendMessage(withLoginRequest("john", "secret"))
              .then()
              .receiveMessage()
              .assertReceivedMessage(loginEvent("john / logged=true"))
              .add(commitSuicide());

        story.record().addAssert(log(log, "handleSessionStart(john), handleSessionStop(john)"));

        story.record().assertContainsAgent(SERVER_LOGIN_AGENT);

        story.execute();
    }


    private MessageTemplate loginEvent(String expected) {
        return MessageTemplate.matchWith(new AssertMatchExpression("login result", expected) {
            @Override
            protected Object extractActual(AclMessage message) {
                LoginEvent loginEvent = (LoginEvent)message.getContentObject();
                if (loginEvent.hasFailed()) {
                    return "not logged / " + loginEvent.getLoginFailureException();
                }
                else {
                    return loginEvent.getUserId().getLogin() + " / logged=" + !loginEvent.hasFailed();
                }
            }
        });
    }


    private AclMessage withLoginRequest(String login, String password) throws UnknownHostException {
        return createLoginMessage(new LoginAction(login,
                                                  password,
                                                  "",
                                                  "v1",
                                                  InetAddress.getLocalHost().getHostAddress(),
                                                  SecurityLevel.USER),
                                  new Aid(SERVER_LOGIN_AGENT));
    }


    private AclMessage withBadContent(Serializable anObject) throws UnknownHostException {
        AclMessage aclMessage = withLoginRequest("", "");
        aclMessage.setContentObject(anObject);
        return aclMessage;
    }


    private static SubStep commitSuicide() {
        return new SubStep() {
            public void run(Agent agent, AclMessage message) throws Exception {
                agent.die();
            }
        };
    }

    // Code a revoir

    public static class SecurityServiceMock extends SecurityService {

        @SuppressWarnings({"UnusedDeclaration"})
        public SecurityServiceMock() {
        }


        public ServiceHelper getServiceHelper(Agent agent) {
            return new SecurityServiceHelperMock(new LogString());
        }
    }


    private AclMessage createLoginMessage(LoginAction loginAction, Aid serverAid) {
        AclMessage aclMessage = new AclMessage(Performative.REQUEST);
        aclMessage.setLanguage(AclMessage.OBJECT_LANGUAGE);
        aclMessage.setProtocol(LoginProtocol.ID);
        aclMessage.setContentObject(loginAction);
        aclMessage.addReceiver(serverAid);
        return aclMessage;
    }

    // end code a revoir


    @Before
    public void setUp() throws Exception {
        story.doSetUp();
        sessionManager = new SessionManager();
        sessionManager.addListener(new SessionListenerMock(log));
    }


    @After
    public void tearDown() throws Exception {
        story.doTearDown();
    }
}
