package net.codjo.security.gui.api;
import net.codjo.security.common.message.ModelManager;
/**
 *
 */
public interface ConfigurationSession {

    void open(OpenHandler openHandler) throws SecurityException;


    ModelManager getModelManager();


    void save(SaveHandler saveHandler) throws SecurityException;


    void close();
}
