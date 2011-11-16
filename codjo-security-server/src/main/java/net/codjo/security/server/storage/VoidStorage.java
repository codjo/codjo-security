package net.codjo.security.server.storage;
import java.sql.Timestamp;
import net.codjo.security.common.message.DefaultModelManager;
import net.codjo.security.common.message.ModelManager;
/**
 *
 */
class VoidStorage extends AbstractStorage {
    private static final Timestamp PAST_DATE = new Timestamp(0);


    public ModelManager loadModel() throws Exception {
        return new DefaultModelManager();
    }


    public Timestamp saveModel(ModelManager manager) throws Exception {
        return PAST_DATE;
    }


    public Timestamp getModelTimestamp() throws Exception {
        return PAST_DATE;
    }
}
