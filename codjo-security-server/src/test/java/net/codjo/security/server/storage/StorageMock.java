package net.codjo.security.server.storage;
import java.sql.Timestamp;
import net.codjo.security.common.message.DefaultModelManager;
import net.codjo.security.common.message.ModelManager;
import net.codjo.security.common.message.XmlCodec;
import net.codjo.security.server.api.Storage;
import net.codjo.test.common.LogString;
/**
 *
 */
public class StorageMock implements Storage {
    private final LogString log;
    private ModelManager model = new DefaultModelManager();
    private Timestamp savedTimestamp;
    private Timestamp timestamp = NO_TIMESTAMP;
    private Exception saveModelFailure;


    public StorageMock() {
        this(new LogString());
    }


    public StorageMock(LogString log) {
        this.log = log;
    }


    public ModelManager loadModel() throws Exception {
        log.call("loadModel");
        return model;
    }


    public Timestamp saveModel(ModelManager manager) throws Exception {
        if (saveModelFailure != null) {
            throw saveModelFailure;
        }

        log.call("saveModel", XmlCodec.toXml(manager));
        return savedTimestamp;
    }


    public Timestamp getModelTimestamp() throws Exception {
        log.call("getModelTimestamp");
        return timestamp;
    }


    public StorageMock mockModel(ModelManager modelManager) {
        model = modelManager;
        return this;
    }


    public StorageMock mockGetModelTimestamp(Timestamp time) {
        timestamp = time;
        return this;
    }


    public StorageMock mockSaveModelTimestamp(Timestamp time) {
        savedTimestamp = time;
        return this;
    }


    public StorageMock mockSaveModelFailure(Exception exception) {
        saveModelFailure = exception;
        return this;
    }
}
