package net.codjo.security.server.service;
import net.codjo.agent.UserId;
import net.codjo.security.server.model.ServerModelManagerMock;
import net.codjo.security.server.storage.StorageFactoryMock;
import net.codjo.security.server.storage.StorageMock;
import net.codjo.test.common.LogString;
import org.junit.Before;
import org.junit.Test;
/**
 *
 */
public class UserConnectionLifecycleTest {
    private LogString log = new LogString();
    private UserConnectionLifecycle userConnectionLifecycle;


    @Before
    public void setUp() throws Exception {
        StorageFactoryMock storageFactoryMock = new StorageFactoryMock(new LogString("storageFactory", log));
        storageFactoryMock.mockStorage(new StorageMock(new LogString("storage", log)));
        userConnectionLifecycle
              = new UserConnectionLifecycle(new ServerModelManagerMock(new LogString("modelManager", log)),
                                            storageFactoryMock);
    }


    @Test
    public void test_lastLogin() throws Exception {
        userConnectionLifecycle.handleSessionStart(UserId.createId("smith", "secret"));

        log.assertContent("storageFactory.create(smith, secret)",
                          "modelManager.updateUserLastLogin(StorageMock, smith)",
                          "storageFactory.release()");
    }


    @Test
    public void test_lastLogout() throws Exception {
        userConnectionLifecycle.handleSessionStop(UserId.createId("smith", "secret"));

        log.assertContent("storageFactory.create(smith, secret)",
                          "modelManager.updateUserLastLogout(StorageMock, smith)",
                          "storageFactory.release()");
    }
}
