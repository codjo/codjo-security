package net.codjo.security.server.plugin;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import net.codjo.ads.AdsException;
import net.codjo.agent.UserId;
import net.codjo.security.common.api.UserData;
import net.codjo.security.common.message.ModelManager;
import net.codjo.security.common.message.Role;
import net.codjo.security.common.message.RoleComposite;
import net.codjo.security.common.message.RoleVisitor;
import net.codjo.security.common.message.User;
import net.codjo.security.server.api.Storage;
import net.codjo.security.server.api.UserFactory;
import net.codjo.security.server.model.ServerModelManager;
import net.codjo.security.server.service.ads.AdsServiceWrapper;
import org.apache.log4j.Logger;
/**
 *
 */
class ServerModelManagerImpl implements ServerModelManager {
    private final Logger logger = Logger.getLogger(ServerModelManagerImpl.class);
    private final Lock lock = new ReentrantLock();
    private ModelManager modelFromFile;
    private ModelManager modelFromDatabase;
    private ModelManagerFileLoader fileLoader;
    private Timestamp modelTimestamp;
    private UserFactory userFactory;
    private AdsServiceWrapper adsServiceWrapper;


    public void init(ModelManagerFileLoader loader, UserFactory newUserFactory) {
        lock.lock();
        try {
            this.fileLoader = loader;
            this.userFactory = newUserFactory;
        }
        finally {
            lock.unlock();
        }
    }


    public void init(ModelManagerFileLoader loader,
                     UserFactory newUserFactory,
                     AdsServiceWrapper newAdsServiceWrapper) {
        lock.lock();
        try {
            this.fileLoader = loader;
            this.userFactory = newUserFactory;
            this.adsServiceWrapper = newAdsServiceWrapper;
        }
        finally {
            lock.unlock();
        }
    }


    private Storage getStorage(Storage storage) {
        if (adsServiceWrapper != null) {
            return new StorageWrapperForAds(storage);
        }
        return storage;
    }


    public void visit(Storage storage, UserId userId, RoleVisitor roleVisitor) throws Exception {
        lock.lock();
        try {
            loadModels(getStorage(storage));
            User user = new User(userId.getLogin());
            modelFromDatabase.visitUserRoles(user, roleVisitor);
            modelFromFile.visitUserRoles(user, roleVisitor);

            visitAds(userId, roleVisitor);
        }
        finally {
            lock.unlock();
        }
    }


    private void visitAds(UserId userId, RoleVisitor roleVisitor) {
        if (adsServiceWrapper != null) {
            try {
                for (String roleName : adsServiceWrapper.rolesFor(userId)) {
                    Role databaseRole = modelFromDatabase.findRole(roleName);
                    if (databaseRole != null) {
                        databaseRole.accept(roleVisitor);
                    }
                    Role fileRole = modelFromFile.findRole(roleName);
                    if (fileRole != null) {
                        fileRole.accept(roleVisitor);
                    }
                    if (fileRole == null && databaseRole == null) {
                        new Role(roleName).accept(roleVisitor);
                    }
                }
            }
            catch (AdsException e) {
                logger.warn("Récupération des rôles impossible.", e);
                throw new RuntimeException("Récupération des rôles impossible.");
            }
        }
    }


    public void updateUserLastLogin(Storage storage, String name) throws Exception {
        updateUserData(getStorage(storage), name, new UpdateUserDataAction() {
            public void updateUser(User user) {
                user.setLastLogin(new Date());
            }
        });
    }


    public void updateUserLastLogout(Storage storage, String name) throws Exception {
        updateUserData(getStorage(storage), name, new UpdateUserDataAction() {
            public void updateUser(User user) {
                user.setLastLogout(new Date());
            }
        });
    }


    public UserData getUserData(Storage storage, String name) throws Exception {
        lock.lock();
        try {
            loadModels(getStorage(storage));
            UserData user = modelFromDatabase.findUser(name);
            if (user == null) {
                return modelFromFile.findUser(name);
            }
            return user;
        }
        finally {
            lock.unlock();
        }
    }


    private void updateUserData(Storage storage, String name, UpdateUserDataAction action) throws Exception {
        lock.lock();
        try {
            loadModels(storage);
            User user = modelFromDatabase.findUser(name);
            if (user == null) {
                User newUser = new User(name);
                action.updateUser(newUser);
                modelFromDatabase.addUser(newUser);
            }
            else {
                action.updateUser(user);
            }

            logger.info(String.format("Sauvegarde du model de sécurité : connection %s avec la stratégie : %s",
                                      name,
                                      storage.getClass().getSimpleName()));
            modelTimestamp = storage.saveModel(modelFromDatabase);
        }
        finally {
            lock.unlock();
        }
    }


    public void save(Storage storage, ModelManager modelFromGui) throws Exception {
        lock.lock();
        try {
            loadModels(getStorage(storage));
            for (User user : modelFromDatabase.getUsers()) {
                User userFromGui = modelFromGui.findUser(user.getName());
                if (userFromGui != null) {
                    userFromGui.setLastLogin(user.getLastLogin());
                    userFromGui.setLastLogout(user.getLastLogout());
                }
            }
            modelTimestamp = getStorage(storage).saveModel(modelFromGui);
            modelFromDatabase = modelFromGui;
        }
        finally {
            lock.unlock();
        }
    }


    public void reloadRoles(Storage storage) throws Exception {
        lock.lock();
        try {
            loadModels(getStorage(storage));
            cleanDeprecatedRoles(modelFromDatabase, getRolesFromUserFactory());
        }
        finally {
            lock.unlock();
        }
    }


    private void loadModels(Storage storage) throws Exception {
        if (modelFromDatabase == null) {
            logger.info("Chargement initial du model de sécurité");
            loadDatabaseModel(storage, storage.getModelTimestamp());
        }
        else {
            Timestamp current = storage.getModelTimestamp();
            if (current.compareTo(modelTimestamp) > 0) {
                logger.info("Re-chargement du model de sécurité (model BD plus récent)");
                loadDatabaseModel(storage, current);
            }
        }

        if (modelFromFile == null) {
            modelFromFile = fileLoader.load();
        }
    }


    private void loadDatabaseModel(Storage storage, Timestamp timestamp) throws Exception {
        modelFromDatabase = storage.loadModel();
        cleanDeprecatedRoles(modelFromDatabase, getRolesFromUserFactory());
        modelTimestamp = timestamp;
    }


    private void cleanDeprecatedRoles(ModelManager modelManager, List<Role> roles) {
        List<Role> deprecatedRoles = new ArrayList<Role>(modelManager.getRoles());
        deprecatedRoles.removeAll(roles);
        for (Role role : deprecatedRoles) {
            if (!(role instanceof RoleComposite)) {
                logger.info("le role '" + role.getName() + "' est devenu obsolète");
                modelManager.removeRole(role);
            }
        }

        List<Role> newRoles = new ArrayList<Role>(roles);
        newRoles.removeAll(modelManager.getRoles());
        for (Role role : newRoles) {
            modelManager.addRole(role);
        }
    }


    List<Role> getRolesFromUserFactory() {
        List<String> names = userFactory.getRoleNames();
        List<Role> roles = new ArrayList<Role>(names.size());
        for (String name : names) {
            roles.add(new Role(name));
        }
        return roles;
    }


    ModelManager getDatabaseModel() {
        return modelFromDatabase;
    }


    private interface UpdateUserDataAction {
        public void updateUser(User user);
    }

    private static class StorageWrapperForAds implements Storage {
        private Storage storage;


        private StorageWrapperForAds(Storage storage) {
            this.storage = storage;
        }


        public ModelManager loadModel() throws Exception {
            ModelManager modelManager = storage.loadModel();
            doCleanUpRoles(modelManager);
            return modelManager;
        }


        public Timestamp saveModel(ModelManager manager) throws Exception {
            doCleanUpRoles(manager);
            return storage.saveModel(manager);
        }


        public Timestamp getModelTimestamp() throws Exception {
            return storage.getModelTimestamp();
        }


        private void doCleanUpRoles(ModelManager modelManager) {
            for (User user : modelManager.getUsers()) {
                List<Role> roleList = new ArrayList<Role>(modelManager.getUserRoles(user));
                for (Role role : roleList) {
                    modelManager.removeRoleToUser(role, user);
                }
            }
        }
    }
}
