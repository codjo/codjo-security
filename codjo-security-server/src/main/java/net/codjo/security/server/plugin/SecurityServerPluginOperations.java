package net.codjo.security.server.plugin;
import net.codjo.agent.UserId;
import net.codjo.security.server.api.Storage;
import net.codjo.security.server.api.StorageFactory;
/**
 *
 */
public class SecurityServerPluginOperations {
    private final ServerModelManagerImpl serverModelManager;
    private StorageFactory storageFactory;


    public SecurityServerPluginOperations(ServerModelManagerImpl serverModelManager) {
        this.serverModelManager = serverModelManager;
    }


    public void setStorageFactory(StorageFactory storageFactory) {
        this.storageFactory = storageFactory;
    }


    public void reloadRoles(UserId userId) throws Exception {
        Storage storage = storageFactory.create(userId);
        try {
            serverModelManager.reloadRoles(storage);
        }
        finally {
            storageFactory.release();
        }
    }
}
