package net.codjo.security.server.model;
import java.sql.SQLException;
import net.codjo.security.common.api.UserData;
import net.codjo.security.common.message.User;
import net.codjo.security.server.api.Storage;
import net.codjo.test.common.LogString;
/**
 *
 */
public class ServerModelManagerMock implements ServerModelManager {
    private LogString log;
    private UserData userData = new User("dummy");


    public ServerModelManagerMock() {
        this(new LogString());
    }


    public ServerModelManagerMock(LogString log) {
        this.log = log;
    }


    public void setLog(LogString log) {
        this.log = log;
    }


    public void updateUserLastLogin(Storage storage, String name) throws SQLException {
        log.call("updateUserLastLogin", storage.getClass().getSimpleName(), name);
    }


    public void updateUserLastLogout(Storage storage, String name) throws SQLException {
        log.call("updateUserLastLogout", storage.getClass().getSimpleName(), name);
    }


    public UserData getUserData(Storage storage, String name) throws SQLException {
        log.call("getUserData", storage.getClass().getSimpleName(), name);
        return userData;
    }


    public void mockGetUserData(UserData mock) {
        userData = mock;
    }
}
