package net.codjo.security.server.api;
import java.sql.Timestamp;
import net.codjo.security.common.message.ModelManager;
/**
 *
 */
public interface Storage {
    Timestamp NO_TIMESTAMP = Timestamp.valueOf("1970-01-02 00:00:00.0");


    ModelManager loadModel() throws Exception;


    Timestamp saveModel(ModelManager manager) throws Exception;


    Timestamp getModelTimestamp() throws Exception;
}
