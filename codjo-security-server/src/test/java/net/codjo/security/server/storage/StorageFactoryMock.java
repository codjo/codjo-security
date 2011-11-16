package net.codjo.security.server.storage;
import net.codjo.agent.UserId;
import net.codjo.security.server.api.Storage;
import net.codjo.security.server.api.StorageFactory;
import net.codjo.test.common.LogString;
/**
 *
 */
public class StorageFactoryMock implements StorageFactory {
    private final LogString log;
    private Storage storage;


    public StorageFactoryMock() {
        this(new LogString());
    }


    public StorageFactoryMock(LogString log) {
        this.log = log;
    }


    public Storage create(UserId userId) throws Exception {
        log.call("create", userId.getLogin(), userId.getPassword());
        return storage;
    }


    public void release() throws Exception {
        log.call("release");
    }


    public void mockStorage(Storage aStorage) {
        storage = aStorage;
    }
}
