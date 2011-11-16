package net.codjo.security.server.service;
import net.codjo.agent.UserId;
import net.codjo.plugin.common.session.SessionListener;
import net.codjo.plugin.common.session.SessionRefusedException;
import net.codjo.security.server.api.Storage;
import net.codjo.security.server.api.StorageFactory;
import net.codjo.security.server.model.ServerModelManager;
import org.apache.log4j.Logger;
/**
 *
 */
class UserConnectionLifecycle implements SessionListener {
    private final Logger logger = Logger.getLogger(UserConnectionLifecycle.class);
    private final ServerModelManager serverModelManager;
    private final StorageFactory storageFactory;


    UserConnectionLifecycle(ServerModelManager serverModelManager,
                            StorageFactory storageFactory) {
        this.serverModelManager = serverModelManager;
        this.storageFactory = storageFactory;
    }


    public void handleSessionStart(UserId userId) throws SessionRefusedException {
        try {
            Storage storage = storageFactory.create(userId);
            try {
                serverModelManager.updateUserLastLogin(storage, userId.getLogin());
            }
            finally {
                storageFactory.release();
            }
        }
        catch (Exception e) {
            throw new SessionRefusedException("Impossible de mettre à jour la date de dernière connection",
                                              e);
        }
    }


    public void handleSessionStop(UserId userId) {
        try {
            Storage storage = storageFactory.create(userId);
            try {
                serverModelManager.updateUserLastLogout(storage, userId.getLogin());
            }
            finally {
                storageFactory.release();
            }
        }
        catch (Exception e) {
            logger.warn("Impossible de mettre à jour la date de dernière déconnexion", e);
        }
    }
}
