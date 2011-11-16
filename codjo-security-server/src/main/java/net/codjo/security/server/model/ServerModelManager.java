package net.codjo.security.server.model;
import net.codjo.security.common.api.UserData;
import net.codjo.security.server.api.Storage;
/**
 * TODO a supprimer des que le SessionListener apparait.
 */
public interface ServerModelManager {
    void updateUserLastLogin(Storage storage, String name) throws Exception;


    void updateUserLastLogout(Storage storage, String name) throws Exception;


    UserData getUserData(Storage storage, String name) throws Exception;
}
