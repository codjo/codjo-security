package net.codjo.security.client.plugin;
import java.io.Serializable;
import net.codjo.agent.AclMessage;
import net.codjo.agent.AclMessage.Performative;
import net.codjo.agent.MessageTemplate;
import net.codjo.agent.MessageTemplate.MatchExpression;
import net.codjo.agent.UserId;
import net.codjo.agent.test.Story;
import net.codjo.security.common.SecurityLevel;
import net.codjo.security.common.api.UserMock;
import net.codjo.security.common.login.LoginAction;
import net.codjo.security.common.login.LoginEvent;
import net.codjo.security.common.login.LoginProtocol;
import net.codjo.test.common.LogString;
import net.codjo.util.system.EventSynchronizer;
import org.junit.Before;
import org.junit.Test;

import static net.codjo.agent.test.AgentAssert.log;
/**
 *
 */
public class LoginAgentTest {
    private LogString log = new LogString();
    private Story story = new Story();
    private EventSynchronizer<LoginEvent> loginEventSynchronizer = new LoginEventSynchronizerMock(log);
    private UserId userId = UserId.createId("myLogin", "myPassword");
    private static final String LOGIN_AGENT = "LoginAgent";
    private LoginAgent loginAgent;


    @Before
    public void setUp() throws Exception {
        story.doSetUp();
        loginAgent = new LoginAgent("myLogin",
                                    "myPassword",
                                    "myOptionalLdap",
                                    "myVersion",
                                    "myIp",
                                    SecurityLevel.USER, loginEventSynchronizer);
    }


    public void tearDown() throws Exception {
        story.doTearDown();
    }


    @Test
    public void test_loginSuccess() throws Exception {
        story.record().startTester(LoginProtocol.SERVER_LOGIN_AGENT)
              .receiveMessage(matchLoginAction(new LoginAction("myLogin",
                                                               "myPassword",
                                                               "myOptionalLdap",
                                                               "myVersion",
                                                               "myIp", SecurityLevel.USER)))
              .replyWithContent(Performative.AGREE, new LoginEvent(new UserMock(), userId));
        story.record().assertContainsAgent(LoginProtocol.SERVER_LOGIN_AGENT);

        story.record().startAgent(LOGIN_AGENT, loginAgent);

        story.record()
              .addAssert(log(log, String.format("receivedEvent(LoginEvent[userId='%s'])", userId.encode())));

        story.record().assertContainsAgent(LOGIN_AGENT);

        story.execute();
    }


    @Test
    public void test_loginFailure() throws Exception {
        story.record().startTester(LoginProtocol.SERVER_LOGIN_AGENT)
              .receiveMessage(matchLoginAction(new LoginAction("myLogin",
                                                               "myPassword",
                                                               "myOptionalLdap",
                                                               "myVersion",
                                                               "myIp", SecurityLevel.USER)))
              .replyWithContent(Performative.REFUSE,
                                new LoginEvent(new RuntimeException("Login impossible !!!")));
        story.record().assertContainsAgent(LoginProtocol.SERVER_LOGIN_AGENT);

        story.record().startAgent(LOGIN_AGENT, loginAgent);

        story.record()
              .addAssert(log(log, "receivedEvent(LoginEvent[loginFailureException='Login impossible !!!'])"));

        story.record().assertNotContainsAgent(LOGIN_AGENT);

        story.execute();
    }


    @Test
    public void test_tearDownHandler() throws Exception {
        loginAgent.setLoginAgentListener(new LoginAgentListener() {
            public void stopped() {
                log.call("stopped");
            }
        });

        loginAgent.tearDown();

        log.assertContent("stopped()");
    }


    @Test
    public void test_tearDownHandlerTwice() throws Exception {
        loginAgent.setLoginAgentListener(new LoginAgentListener() {
            public void stopped() {
                log.call("stopped");
            }
        });

        loginAgent.tearDown();
        loginAgent.tearDown();

        log.assertContent("stopped()");
    }


    @Test
    public void test_tearDownHandler_noHandler() throws Exception {
        loginAgent.tearDown();
        log.assertContent("");
    }


    private MessageTemplate matchLoginAction(final LoginAction loginAction) {
        return MessageTemplate.matchWith(new MatchExpression() {
            public boolean match(AclMessage aclMessage) {
                Performative performative = aclMessage.getPerformative();
                String protocol = aclMessage.getProtocol();
                String language = aclMessage.getLanguage();
                Serializable contentObject = aclMessage.getContentObject();
                return Performative.REQUEST.equals(performative)
                       && LoginProtocol.ID.equals(protocol)
                       && AclMessage.OBJECT_LANGUAGE.equals(language)
                       && assertLoginAction(loginAction, (LoginAction)contentObject);
            }
        });
    }


    private static boolean assertLoginAction(LoginAction expected, LoginAction actual) {
        return expected.getLogin().equals(actual.getLogin())
               && expected.getPassword().equals(actual.getPassword())
               && expected.getLdap().equals(actual.getLdap())
               && expected.getVersion().equals(actual.getVersion())
               && expected.getIp().equals(actual.getIp())
               && expected.getHostname().equals(actual.getHostname())
               && expected.getSecurityLevel().equals(actual.getSecurityLevel()
        );
    }
}
