package net.codjo.security.server.storage;
import java.io.File;
import java.util.ConcurrentModificationException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import net.codjo.agent.UserId;
import net.codjo.security.server.api.Storage;
/**
 *
 */
public class FileStorageFactory extends AbstractStorageFactory {
    private static final int DEFAULT_TIMEOUT = 500;
    private int timeout;
    Semaphore lock = new Semaphore(1);
    private File file;


    public FileStorageFactory(String storagePath) {
        this(storagePath, DEFAULT_TIMEOUT);
    }


    public FileStorageFactory(String storagePath, int timeout) {
        file = new File(storagePath);
        this.timeout = timeout;
    }


    public Storage create(UserId userId) throws Exception {
        if (lock.tryAcquire(timeout, TimeUnit.MILLISECONDS)) {
            return new FileStorage(file);
        }
        throw new ConcurrentModificationException(
              "Impossible de créer la stratégie de stockage, le fichier est deja en cours d'utilisation");
    }


    public void release() throws Exception {
        lock.release();
    }


    int getTimeout() {
        return timeout;
    }


    public File getFile() {
        return file;
    }
}
