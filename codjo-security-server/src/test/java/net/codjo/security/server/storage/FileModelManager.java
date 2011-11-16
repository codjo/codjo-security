package net.codjo.security.server.storage;
import net.codjo.security.common.message.ModelManager;
/**
 *
 */
public class FileModelManager {
    ModelManager model;
    String version;


    public ModelManager getModel() {
        return model;
    }


    public void setModel(ModelManager model) {
        this.model = model;
    }


    public String getVersion() {
        return version;
    }


    public void setVersion(String version) {
        this.version = version;
    }
}
