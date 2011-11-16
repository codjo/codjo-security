package net.codjo.security.gui.communication;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import net.codjo.agent.AclMessage;
import net.codjo.agent.MessageTemplate;
import net.codjo.agent.UserId;
import net.codjo.agent.test.AgentAssert;
import net.codjo.agent.test.AgentContainerFixture;
import net.codjo.agent.test.Story;
import net.codjo.security.common.Constants;
import net.codjo.security.common.message.DefaultModelManager;
import net.codjo.security.common.message.MessageBody;
import net.codjo.security.common.message.ModelManager;
import net.codjo.security.common.message.SecurityEngineConfiguration;
import net.codjo.security.common.message.XmlCodec;

import static net.codjo.security.common.message.SecurityEngineConfiguration.defaultConfiguration;
/**
 *
 */
public class SecurityConfiguratorAgentTest extends TestCase {
    private Story story = new Story();
    private HandlerMock handlerMock = new HandlerMock();
    private UserId userId = UserId.createId("root", "secret");


    public void test_query_securityModel() throws Exception {
        story.record().startTester("manager-mock")
              .registerToDF(Constants.SECURITY_MANAGER_SERVICE)
              .then()
              .receiveMessage()
              .assertReceivedMessage(MessageTemplate.matchPerformative(AclMessage.Performative.QUERY))
              .assertReceivedMessage(MessageTemplate.matchContent("securityModel"))
              .assertReceivedMessageUserId(userId)
              .replyWith(AclMessage.Performative.INFORM, emptySecurityModel());

        story.record().assertNumberOfAgentWithService(1, Constants.SECURITY_MANAGER_SERVICE);

        story.record().startAgent("configurator", new SecurityConfiguratorAgent(handlerMock, userId));

        story.record().addAssert(new AgentAssert.Assertion() {
            public void check() throws AssertionFailedError {
                assertEquals(emptyModelManager(), XmlCodec.toXml(handlerMock.model));
                assertEquals(defaultConfiguration(), handlerMock.engineConfiguration);
            }
        });

        story.execute();
    }


    public void test_query_error() throws Exception {
        story.record().startTester("manager-mock")
              .registerToDF(Constants.SECURITY_MANAGER_SERVICE)
              .then()
              .receiveMessage()
              .replyWith(AclMessage.Performative.FAILURE, "error has occured");

        story.record().assertNumberOfAgentWithService(1, Constants.SECURITY_MANAGER_SERVICE);

        story.record().startAgent("configurator", new SecurityConfiguratorAgent(handlerMock, userId));

        story.record().addAssert(new AgentAssert.Assertion() {
            public void check() throws AssertionFailedError {
                assertNotNull(handlerMock.error);
                assertTrue(handlerMock.error.contains("error has occured"));
            }
        });

        story.execute();
    }


    public void test_save_securityModel() throws Exception {
        story.record().startTester("manager-mock")
              .registerToDF(Constants.SECURITY_MANAGER_SERVICE)
              .then()
              .receiveMessage(MessageTemplate.matchPerformative(AclMessage.Performative.QUERY))
              .then()
              .receiveMessage()
              .assertReceivedMessage(MessageTemplate.matchPerformative(AclMessage.Performative.REQUEST))
              .assertReceivedMessage(MessageTemplate.matchContent(emptyModelManager()))
              .assertReceivedMessageUserId(userId)
              .replyWith(AclMessage.Performative.INFORM, "done");

        story.record().assertNumberOfAgentWithService(1, Constants.SECURITY_MANAGER_SERVICE);

        story.record().startAgent("configurator", new SecurityConfiguratorAgent(handlerMock, userId));

        story.record().addAction(new AgentContainerFixture.Runnable() {
            public void run() throws Exception {
                story.getAgent("configurator").putO2AObject(new DefaultModelManager());
            }
        });

        story.record().addAssert(new AgentAssert.Assertion() {
            public void check() throws AssertionFailedError {
                assertTrue(handlerMock.saveIsASuccess);
            }
        });

        story.execute();
    }


    @Override
    protected void setUp() throws Exception {
        story.doSetUp();
    }


    @Override
    protected void tearDown() throws Exception {
        story.doTearDown();
    }


    private static String emptyModelManager() {
        return XmlCodec.toXml(new DefaultModelManager());
    }


    private static String emptySecurityModel() {
        return XmlCodec.toXml(new MessageBody(new DefaultModelManager(), defaultConfiguration()));
    }


    private static class HandlerMock implements SecurityConfiguratorAgent.Handler {
        ModelManager model;
        SecurityEngineConfiguration engineConfiguration;
        String error;
        boolean saveIsASuccess = false;


        public void handleReceiveSecurityModel(ModelManager modelManager,
                                               SecurityEngineConfiguration configuration) {
            this.model = modelManager;
            this.engineConfiguration = configuration;
        }


        public void handleCommunicationError(String badThingsHappen) {
            this.error = badThingsHappen;
        }


        public void handleSaveSucceed() {
            saveIsASuccess = true;
        }
    }
}
