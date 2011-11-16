package net.codjo.security.server.api;
import net.codjo.agent.UserId;
/**
 *
 */
public interface StorageFactory {
    Storage create(UserId userId) throws Exception;


    void release() throws Exception;
}
