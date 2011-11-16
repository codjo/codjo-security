/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.security.server.plugin;
import junit.framework.TestCase;
import net.codjo.agent.Agent;
import net.codjo.agent.AgentContainer;
import net.codjo.agent.ContainerConfiguration;
import net.codjo.agent.ServiceException;
import net.codjo.agent.ServiceHelper;
import net.codjo.agent.behaviour.OneShotBehaviour;
import net.codjo.agent.test.AgentAssert;
import net.codjo.agent.test.AgentContainerFixture;
import net.codjo.agent.test.DummyAgent;
import net.codjo.plugin.common.CommandLineArguments;
import net.codjo.plugin.server.AbstractServerPlugin;
import net.codjo.plugin.server.ServerCoreMock;
import net.codjo.security.server.api.SecurityContextFactory;
import net.codjo.security.server.api.SecurityContextFactoryMock;
import net.codjo.security.server.api.SecurityService;
import net.codjo.security.server.api.SecurityServiceHelper;
import net.codjo.security.server.api.UserFactory;
import net.codjo.security.server.api.UserFactoryMock;
import net.codjo.security.server.storage.JdbcStorageFactory;
import net.codjo.sql.server.plugin.JdbcServerPlugin;
import net.codjo.test.common.LogString;
/**
 * Test que le Service par défaut s'instancie correctement.
 */
public class ReleaseTest extends TestCase {
    private LogString log = new LogString();
    private AgentContainerFixture fixture = new AgentContainerFixture();


    public void test_defaultServiceCanBeCreated() throws Exception {
        ServerCoreMock serverMock = new MyAgentServerMock();
        serverMock.addGlobalComponent(log);
        serverMock.getContainerConfig().setParameter(JdbcServerPlugin.DRIVER_PARAMETER, "fakedb.FakeDriver");
        serverMock.getContainerConfig().setParameter(JdbcServerPlugin.URL_PARAMETER, "cc");
        serverMock.getContainerConfig().setParameter(JdbcServerPlugin.CATALOG_PARAMETER, "cata");
        serverMock.getContainerConfig().setParameter(JdbcStorageFactory.CONFIG_JDBC_LOGIN, "capri");
        serverMock.getContainerConfig().setParameter(JdbcStorageFactory.CONFIG_JDBC_PASSWORD, "capri_pwd");
        log.clear();

        serverMock.addPlugin(JdbcServerPlugin.class);
        serverMock.addPlugin(SecurityServerPlugin.class);
        serverMock.addPlugin(ReleaseTest.ConfiguratorServerPlugin.class);
        serverMock.start(new CommandLineArguments(new String[0]));

        fixture.assertUntilOk(AgentAssert.log(log, "service activé LdapSecurityServiceHelper"));
    }


    @Override
    protected void setUp() throws Exception {
        fixture.doSetUp();
    }


    @Override
    protected void tearDown() throws Exception {
        fixture.doTearDown();
    }


    private class MyAgentServerMock extends ServerCoreMock {
        MyAgentServerMock() {
            super(new LogString(), ReleaseTest.this.fixture);
        }


        @Override
        protected AgentContainer createAgentContainer(ContainerConfiguration containerConfiguration) {
            fixture.startContainer(containerConfiguration);
            return super.createAgentContainer(containerConfiguration);
        }
    }

    public static class SecurityServiceMock extends SecurityService {
        @SuppressWarnings({"UnusedDeclaration"
                          })
        public SecurityServiceMock(UserFactory userFactory, SecurityContextFactory securityContextFactory) {
        }


        public ServiceHelper getServiceHelper(Agent agent) {
            return null;
        }
    }

    public static class ConfiguratorServerPlugin extends AbstractServerPlugin {
        private final LogString log;


        public ConfiguratorServerPlugin(SecurityServerPlugin plugin, LogString log) {
            this.log = log;
            plugin.getConfiguration().setSecurityContextFactory(new SecurityContextFactoryMock());
            plugin.getConfiguration().setUserFactory(new UserFactoryMock());
        }


        @Override
        public void start(AgentContainer agentContainer)
              throws Exception {
            agentContainer.acceptNewAgent("check-security",
                                          new DummyAgent(new GetSecurityServiceBehaviour(log))).start();
        }
    }

    private static class GetSecurityServiceBehaviour extends OneShotBehaviour {
        private LogString log;


        GetSecurityServiceBehaviour(LogString log) {
            this.log = log;
        }


        @Override
        protected void action() {
            try {
                ServiceHelper helper = getAgent().getHelper(SecurityServiceHelper.NAME);
                log.info("service activé " + helper.getClass().getSimpleName());
            }
            catch (ServiceException e) {
                log.info("pas de service " + e.getLocalizedMessage());
            }
        }
    }
}
