package net.codjo.security.client.plugin;
import java.net.InetAddress;
import java.net.UnknownHostException;
import junit.framework.Assert;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Agent;
import net.codjo.agent.Aid;
import net.codjo.agent.ContainerConfiguration;
import net.codjo.agent.MessageTemplate;
import net.codjo.agent.UserId;
import net.codjo.agent.test.AgentContainerFixture;
import net.codjo.agent.test.Semaphore;
import net.codjo.agent.test.Story.ConnectionType;
import net.codjo.agent.test.SubStep;
import net.codjo.agent.test.TesterAgent;
import net.codjo.plugin.common.ApplicationCoreMock;
import net.codjo.plugin.common.session.SessionManager;
import net.codjo.security.common.SecurityLevel;
import net.codjo.security.common.SecurityLevelHelper;
import net.codjo.security.common.api.User;
import net.codjo.security.common.api.UserMock;
import net.codjo.security.common.login.LoginAction;
import net.codjo.security.common.login.LoginEvent;
import net.codjo.security.common.login.LoginProtocol;
import net.codjo.test.common.LogString;
import net.codjo.test.common.fixture.SystemExitFixture;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static net.codjo.agent.AclMessage.Performative;
import static net.codjo.agent.test.AgentAssert.log;
/**
 *
 */
public class SecurityClientPluginTest {
    private static final String LOGIN = "myLogin";
    private static final String PASSWORD = "myPassword";
    private static final String LOGIN_FAILURE_MESSAGE = "creve";
    private static final SystemExitFixture exitFixture = new SystemExitFixture();
    private LogString log = new LogString();
    private AgentContainerFixture fixture = new AgentContainerFixture();
    private SessionManager sessionManager = new SessionManager();
    private UserId userID = UserId.createId(LOGIN, PASSWORD);
    private Aid actualLoginAgentAid;
    private SecurityClientPlugin plugin;
    private ApplicationCoreMock applicationCoreMock;
    private SecurityLevel actualSecurityLevel;
//    private ContainerConfiguration containerConfiguration;


    @BeforeClass
    public static void globalSetUp() {
        exitFixture.doSetUp();
    }


    @Before
    public void setUp() {
        fixture.doSetUp();
        fixture.startContainer(ConnectionType.NO_CONNECTION);

        applicationCoreMock = new ApplicationCoreMock();
        plugin = new SecurityClientPlugin(applicationCoreMock, sessionManager);
    }


    @AfterClass
    public static void globalTearDown() {
        exitFixture.doTearDown();
    }


    @After
    public void tearDown() throws Exception {
        fixture.doTearDown();
    }


    @Test
    public void test_init() throws Exception {
        ContainerConfiguration containerConfiguration = new ContainerConfiguration();
        containerConfiguration.setParameter(SecurityClientPluginConfiguration.LOGIN_PARAMETER, "mylogin");
        containerConfiguration.setParameter(SecurityClientPluginConfiguration.PASSWORD_PARAMETER,
                                            "mypassword");

        plugin.initContainer(containerConfiguration);

        assertTrue(containerConfiguration.getContainerName().startsWith("country_of_mylogin"));
    }


    @Test
    public void test_initWithOptionalLDap() throws Exception {
        ContainerConfiguration containerConfiguration = new ContainerConfiguration();
        containerConfiguration.setParameter(SecurityClientPluginConfiguration.LOGIN_PARAMETER, "mylogin");
        containerConfiguration.setParameter(SecurityClientPluginConfiguration.PASSWORD_PARAMETER,
                                            "mypassword");
        containerConfiguration.setParameter(SecurityClientPluginConfiguration.LDAP_PARAMETER, "myDomain");

        plugin.initContainer(containerConfiguration);

        Assert.assertTrue(containerConfiguration.getContainerName().startsWith(
              "country_of_mylogin_in_myDomain"));
    }


    @Test
    public void test_initFailure() throws Exception {
        assertInitFailure(new ContainerConfiguration());

        ContainerConfiguration containerConfiguration = new ContainerConfiguration();
        containerConfiguration.setParameter(SecurityClientPluginConfiguration.LOGIN_PARAMETER, "mylogin");
        assertInitFailure(containerConfiguration);

        containerConfiguration = new ContainerConfiguration();
        containerConfiguration.setParameter(SecurityClientPluginConfiguration.PASSWORD_PARAMETER, PASSWORD);
        assertInitFailure(containerConfiguration);
    }


    @Test
    public void test_start() throws Exception {
        fixture.startNewAgent(LoginProtocol.SERVER_LOGIN_AGENT, createAcceptAllLoginAgent());

        assertNull(SecurityLevelHelper.getSecurityLevel(applicationCoreMock.getContainerConfig()));
        initPlugin(LOGIN, PASSWORD, null, SecurityLevel.USER);
        plugin.start(fixture.getContainer());

        assertNotNull(actualLoginAgentAid);
        fixture.assertContainsAgent(actualLoginAgentAid.getLocalName());
        assertStartWith("Login-" + LOGIN + "-" + getComputerName(), actualLoginAgentAid.getLocalName());
        assertEquals(LOGIN, System.getProperty("user.name"));

        User user = applicationCoreMock.getGlobalComponent(User.class);
        assertNotNull(user);
        assertFalse(user.isAllowedTo("ee"));
        assertEquals(userID, plugin.getUserId());
        assertEquals(userID, applicationCoreMock.getGlobalComponent(UserId.class));
        assertEquals(SecurityLevel.USER, actualSecurityLevel);
    }


    @Test
    public void test_startStop() throws Exception {
        fixture.startNewAgent(LoginProtocol.SERVER_LOGIN_AGENT, createAcceptAllLoginAgent());

        initPlugin(LOGIN, PASSWORD, null, SecurityLevel.USER);
        plugin.start(fixture.getContainer());
        assertNotNull(SecurityLevelHelper.getSecurityLevel(applicationCoreMock.getContainerConfig()));

        plugin.stop();
        assertNull(applicationCoreMock.getGlobalComponent(UserId.class));
        assertNull(applicationCoreMock.getGlobalComponent(User.class));
        assertNull(plugin.getUserId());
        assertNull(SecurityLevelHelper.getSecurityLevel(applicationCoreMock.getContainerConfig()));
    }


    @Test
    public void test_start_badLogin() throws Exception {
        fixture.startNewAgent(LoginProtocol.SERVER_LOGIN_AGENT, createRefuseAllLoginAgent());

        final String agentLogin = "badLogin";
        try {
            initPlugin(agentLogin, PASSWORD, null, SecurityLevel.SERVICE);
            plugin.start(fixture.getContainer());
            fail();
        }
        catch (SecurityClientPlugin.LoginFailureException ex) {
            assertEquals(LOGIN_FAILURE_MESSAGE, ex.getCause().getMessage());
            assertNotSame(agentLogin, System.getProperty("user.name"));
        }
        assertEquals(SecurityLevel.SERVICE,
                     SecurityLevelHelper.getSecurityLevel(applicationCoreMock.getContainerConfig()));
    }


    @Test
    public void test_stop() throws Exception {
        fixture.startNewAgent(LoginProtocol.SERVER_LOGIN_AGENT, createAcceptAllLoginAgent());

        initPlugin(LOGIN, PASSWORD, null, SecurityLevel.USER);
        plugin.start(fixture.getContainer());
        assertNotNull(actualLoginAgentAid);
        assertStartWith("Login-" + LOGIN + "-" + getComputerName(), actualLoginAgentAid.getLocalName());
        fixture.assertContainsAgent(actualLoginAgentAid.getLocalName());

        plugin.stop();
        fixture.assertNotContainsAgent(actualLoginAgentAid.getLocalName());
    }


    @Test
    public void test_presidentListener_override() throws Exception {
        final Semaphore semaphore = new Semaphore();

        fixture.startNewAgent(LoginProtocol.SERVER_LOGIN_AGENT, createAcceptAllLoginAgent());

        LoginAgentListener killListener = new LoginAgentListener() {
            public void stopped() {
                log.call("stopped");
                semaphore.release();
            }
        };

        plugin = new SecurityClientPlugin(killListener, new ApplicationCoreMock(), sessionManager);
        initPlugin(LOGIN, PASSWORD, null, SecurityLevel.USER);
        plugin.start(fixture.getContainer());

        fixture.assertContainsAgent(actualLoginAgentAid.getLocalName());

        fixture.getContainer().getAgent(actualLoginAgentAid.getLocalName()).kill();
        fixture.waitForAgentDeath(actualLoginAgentAid.getLocalName());

        semaphore.acquire();

        log.assertContent("stopped()");
    }


    @Test
    public void test_presidentListener_default() throws Exception {
        fixture.startNewAgent(LoginProtocol.SERVER_LOGIN_AGENT, createAcceptAllLoginAgent());

        plugin = new SecurityClientPlugin(new ApplicationCoreMock(log), sessionManager);
        initPlugin(LOGIN, PASSWORD, null, SecurityLevel.USER);
        plugin.start(fixture.getContainer());

        fixture.assertContainsAgent(actualLoginAgentAid.getLocalName());

        fixture.getContainer().getAgent(actualLoginAgentAid.getLocalName()).kill();
        fixture.waitForAgentDeath(actualLoginAgentAid.getLocalName());

        fixture.assertUntilOk(log(log, "addGlobalComponent(UserId)"
                                       + ", addGlobalComponent(User, UserWithLog)"
                                       + ", stop()"));

        assertEquals(DeathLoginAgentListener.INTERNAL_ERROR_LOGIN_AGENT_KILLED,
                     exitFixture.getFirstExitValue());
    }


    private void assertInitFailure(ContainerConfiguration configuration) {
        try {
            plugin.initContainer(configuration);
            fail();
        }
        catch (Exception e) {
            assertEquals(SecurityClientPlugin.MISSING_LOGIN_PASSWORD_MESSAGE, e.getMessage());
        }
    }


    private void assertStartWith(String expectedStart, String actual) {
        Assert.assertTrue("\nExpected : " + expectedStart + "\nActual   : " + actual,
                          actual.startsWith(expectedStart));
    }


    private String getComputerName() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostName();
    }


    private TesterAgent createAcceptAllLoginAgent() {
        return createSecurityServerLoginAgent(new LoginEvent(new UserMock(), userID));
    }


    private TesterAgent createRefuseAllLoginAgent() {
        return createSecurityServerLoginAgent(new LoginEvent(new Throwable(LOGIN_FAILURE_MESSAGE)));
    }


    private TesterAgent createSecurityServerLoginAgent(LoginEvent loginEvent) {
        TesterAgent agent = new TesterAgent();
        agent.record()
              .receiveMessage(MessageTemplate.matchPerformative(Performative.REQUEST))
              .add(new SubStep() {
                  public void run(Agent agent, AclMessage message) {
                      actualLoginAgentAid = message.getSender();
                      actualSecurityLevel = ((LoginAction)message.getContentObject()).getSecurityLevel();
                  }
              }).replyWithContent(Performative.INFORM, loginEvent);
        return agent;
    }


    private void initPlugin(String login, String password, String ldap, SecurityLevel securityLevel)
          throws Exception {
        ContainerConfiguration containerConfiguration = applicationCoreMock.getContainerConfig();
        containerConfiguration.setParameter(SecurityClientPluginConfiguration.LOGIN_PARAMETER, login);
        containerConfiguration.setParameter(SecurityClientPluginConfiguration.PASSWORD_PARAMETER, password);
        containerConfiguration.setParameter(SecurityClientPluginConfiguration.LDAP_PARAMETER, ldap);
        SecurityLevelHelper.setSecurityLevel(containerConfiguration, securityLevel);
        plugin.initContainer(containerConfiguration);
    }
}
