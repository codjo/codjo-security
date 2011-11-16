package net.codjo.security.server.service;
import net.codjo.agent.ContainerConfiguration;
import net.codjo.plugin.common.session.SessionManager;
import net.codjo.security.server.api.SecurityService;
import net.codjo.security.server.api.StorageFactory;
import net.codjo.security.server.model.ServerModelManager;
/**
 *
 */
public abstract class AbstractSecurityService extends SecurityService {
    private final ServerModelManager serverModelManager;
    private final StorageFactory storageFactory;


    protected AbstractSecurityService(ServerModelManager serverModelManager,
                                      SessionManager sessionManager,
                                      StorageFactory storageFactory) {
        this.serverModelManager = serverModelManager;
        this.storageFactory = storageFactory;

        initUserConnectionLifeCycle(sessionManager);
    }


    private void initUserConnectionLifeCycle(SessionManager sessionManager) {
        sessionManager.addListener(new UserConnectionLifecycle(serverModelManager, getStorageFactory()));
    }


    public ServerModelManager getServerModelManager() {
        return serverModelManager;
    }


    protected StorageFactory getStorageFactory() {
        return storageFactory;
    }


    protected static String get(ContainerConfiguration configuration, String configKey, String defaultValue) {
        String value = configuration.getParameter(configKey);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }
}
