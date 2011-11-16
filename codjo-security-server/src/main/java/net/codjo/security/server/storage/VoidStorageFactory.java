package net.codjo.security.server.storage;
import net.codjo.agent.UserId;
import net.codjo.security.server.api.Storage;
/**
 *
 */
public class VoidStorageFactory extends AbstractStorageFactory {

    public Storage create(UserId userId) throws Exception {
        return new VoidStorage();
    }


    public void release() throws Exception {
    }
}
