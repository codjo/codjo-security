package net.codjo.security.server.plugin;
import net.codjo.agent.UserId;
import net.codjo.security.common.message.ModelManager;
import net.codjo.security.common.message.RoleVisitor;
import net.codjo.security.common.message.User;
import net.codjo.security.common.message.XmlCodec;
import net.codjo.security.server.api.Storage;
import net.codjo.security.server.api.UserFactory;
import net.codjo.test.common.LogString;
/**
 *
 */
public class ServerModelManagerImplMock extends ServerModelManagerImpl {
    private final LogString log;
    private final ModelManager manager;
    private Exception saveFailure;


    public ServerModelManagerImplMock(ModelManager manager) {
        this.manager = manager;
        log = new LogString();
    }


    public ServerModelManagerImplMock(LogString log, ModelManager manager) {
        this.manager = manager;
        this.log = log;
    }


    @Override
    public void init(ModelManagerFileLoader loader, UserFactory newUserFactory) {
        log.call("init", loader);
    }


    @Override
    public void visit(Storage storage, UserId userId, RoleVisitor roleVisitor) {
        log.call("visit", storage.getClass().getSimpleName(), userId);
        manager.visitUserRoles(new User(userId.getLogin()), roleVisitor);
    }


    @Override
    public void save(Storage storage, ModelManager modelFromGui) throws Exception {
        if (saveFailure != null) {
            throw saveFailure;
        }
        log.call("save", storage.getClass().getSimpleName(), XmlCodec.toXml(modelFromGui));
    }


    @Override
    public void reloadRoles(Storage storage) {
        log.call("reloadRoles");
    }


    @Override
    ModelManager getDatabaseModel() {
        log.call("getDatabaseModel");
        return manager;
    }


    public ServerModelManagerImplMock mockSaveFailure(Exception exception) {
        saveFailure = exception;
        return this;
    }
}
