package net.codjo.security.server.service;
import net.codjo.agent.Agent;
import net.codjo.agent.ServiceException;
import net.codjo.agent.UserId;
import net.codjo.security.common.SecurityLevel;
import net.codjo.security.common.api.User;
import net.codjo.security.common.api.UserData;
import net.codjo.security.server.api.SecurityContextFactory;
import net.codjo.security.server.api.SecurityServiceHelper;
import net.codjo.security.server.api.Storage;
import net.codjo.security.server.api.StorageFactory;
import net.codjo.security.server.api.UserFactory;
import net.codjo.security.server.model.ServerModelManager;
import org.apache.log4j.Logger;
/**
 *
 */
public abstract class AbstractSecurityServiceHelper implements SecurityServiceHelper {
    private static final Logger LOG = Logger.getLogger(AbstractSecurityServiceHelper.class);
    protected final UserFactory userFactory;
    protected final ServerModelManager serverModelManager;
    protected final StorageFactory storageFactory;
    protected SecurityContextFactory securityContextFactory;


    protected AbstractSecurityServiceHelper(
          UserFactory userFactory,
          SecurityContextFactory securityContextFactory,
          ServerModelManager serverModelManager, StorageFactory storageFactory) {
        this.userFactory = userFactory;
        this.securityContextFactory = securityContextFactory;
        this.serverModelManager = serverModelManager;
        this.storageFactory = storageFactory;
    }


    public void init(Agent anAgent) {
    }


    public User getUser(final UserId userId) throws ServiceException {
        return executeCommand(userId, new MyCommand<User>() {
            public User proceed(Storage storage) throws Exception {
                return userFactory.getUser(userId,
                                           securityContextFactory.createSecurityContext(userId, storage));
            }
        });
    }


    public UserData getUserData(UserId userId, final String userName) throws ServiceException {
        return executeCommand(userId, new MyCommand<UserData>() {
            public UserData proceed(Storage storage) throws Exception {
                return serverModelManager.getUserData(storage, userName);
            }
        });
    }


    protected void logInfo(String login, SecurityLevel securityLevel) {
        LOG.info("Trying to login with (user: " + login + ", securityLevel: "
                 + securityLevel + ")");
    }


    private <T> T executeCommand(UserId userId, MyCommand<T> command) throws ServiceException {
        try {
            Storage storage = storageFactory.create(userId);
            try {
                return command.proceed(storage);
            }
            finally {
                storageFactory.release();
            }
        }
        catch (Exception e) {
            throw new ServiceException(e);
        }
    }


    static interface MyCommand<T> {
        T proceed(Storage storage) throws Exception;
    }
}
