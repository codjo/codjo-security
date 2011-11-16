package net.codjo.security.server.plugin;
import junit.framework.TestCase;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Aid;
import net.codjo.agent.ContainerFailureException;
import net.codjo.agent.UserId;
import net.codjo.agent.protocol.RequestProtocol;
import net.codjo.agent.test.Story;
import net.codjo.security.common.Constants;
import net.codjo.security.common.message.DefaultModelManager;
import net.codjo.security.common.message.MessageBody;
import net.codjo.security.common.message.SecurityEngineConfiguration;
import net.codjo.security.common.message.XmlCodec;
import net.codjo.security.server.api.StorageFactory;
import net.codjo.security.server.storage.StorageFactoryMock;
import net.codjo.security.server.storage.StorageMock;
import net.codjo.test.common.LogString;

import static net.codjo.agent.AclMessage.Performative;
import static net.codjo.agent.MessageTemplate.matchContent;
import static net.codjo.agent.MessageTemplate.matchPerformative;
import static net.codjo.security.common.message.SecurityEngineConfiguration.defaultConfiguration;
/**
 *
 */
public class SecurityManagerAgentTest extends TestCase {
    private Story story = new Story();
    private LogString log = new LogString();
    private ServerModelManagerImplMock modelManager;
    private SecurityEngineConfiguration engineConfiguration = defaultConfiguration();
    private StorageMock storageMock = new StorageMock(log);
    private StorageFactoryMock storageFactoryMock = new StorageFactoryMock(log);


    public void test_getSecurityModel() throws ContainerFailureException {

        story.record()
              .startAgent("bebel",
                          securityAgent(modelManager, engineConfiguration, storageFactoryMock));

        story.record().assertAgentWithService(new String[]{"bebel"},
                                              Constants.SECURITY_MANAGER_SERVICE);

        story.record().startTester("gui-agent")
              .sendMessage(Performative.QUERY, RequestProtocol.QUERY, new Aid("bebel"), "securityModel")
              .then()
              .receiveMessage()
              .assertReceivedMessage(matchContent(emptySecurityModel()));

        story.execute();
    }


    public void test_save() throws ContainerFailureException {
        story.record()
              .startAgent("bebel",
                          securityAgent(modelManager, engineConfiguration, storageFactoryMock));

        story.record().assertAgentWithService(new String[]{"bebel"},
                                              Constants.SECURITY_MANAGER_SERVICE);

        story.record().startTester("gui-agent")
              .sendMessage(createSaveRequest(new Aid("bebel"), emptyModelManager()))
              .then()
              .receiveMessage()
              .assertReceivedMessage(matchPerformative(Performative.INFORM));

        story.execute();

        log.assertContent("create(root, secret)",
                          String.format("save(StorageMock, %s)", emptyModelManager()),
                          "release()");
    }


    public void test_save_failure() throws ContainerFailureException {
        modelManager.mockSaveFailure(new Exception("expected error"));

        story.record()
              .startAgent("bebel", securityAgent(modelManager, engineConfiguration, storageFactoryMock));

        story.record().startTester("gui-agent")
              .sendMessage(createSaveRequest(new Aid("bebel"), emptyModelManager()))
              .then()
              .receiveMessage()
              .assertReceivedMessage(matchPerformative(Performative.FAILURE));

        story.execute();
    }


    private AclMessage createSaveRequest(Aid receiver, String content) {
        AclMessage request = new AclMessage(Performative.REQUEST);
        request.setProtocol(RequestProtocol.REQUEST);
        request.addReceiver(receiver);
        request.setContent(content);
        request.encodeUserId(UserId.createId("root", "secret"));
        return request;
    }


    @Override
    protected void setUp() throws Exception {
        story.doSetUp();
        modelManager = new ServerModelManagerImplMock(log, new DefaultModelManager());
        storageFactoryMock.mockStorage(storageMock);
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


    private static SecurityManagerAgent securityAgent(ServerModelManagerImplMock modelManager,
                                                      SecurityEngineConfiguration configuration,
                                                      StorageFactory storageFactory) {
        return new SecurityManagerAgent(modelManager,
                                        storageFactory,
                                        configuration);
    }
}
