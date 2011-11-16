package net.codjo.security.server.plugin;
import net.codjo.agent.UserId;
import net.codjo.security.common.message.DefaultModelManager;
import net.codjo.security.server.storage.StorageFactoryMock;
import net.codjo.test.common.LogString;
import org.junit.Before;
import org.junit.Test;
/**
 *
 */
public class SecurityServerPluginOperationsTest {
    private LogString log = new LogString();
    private UserId userId = UserId.createId("toto", "tata");
    private SecurityServerPluginOperations pluginOperations;


    @Before
    public void setUp() throws Exception {
        pluginOperations = new SecurityServerPluginOperations(
              new ServerModelManagerImplMock(log, new DefaultModelManager()));
        pluginOperations.setStorageFactory(new StorageFactoryMock(log));
    }


    @Test
    public void test_reloadRoles() throws Exception {
        pluginOperations.reloadRoles(userId);
        log.assertContent("create(toto, tata)", "reloadRoles()", "release()");
    }
}
