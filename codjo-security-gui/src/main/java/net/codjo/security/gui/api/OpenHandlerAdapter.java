package net.codjo.security.gui.api;
import net.codjo.security.common.message.ModelManager;
import net.codjo.security.common.message.SecurityEngineConfiguration;
/**
 *
 */
public abstract class OpenHandlerAdapter implements OpenHandler {
    public void handleReceiveSecurityModel(ModelManager modelManager,
                                           SecurityEngineConfiguration engineConfiguration) {
    }


    public void handleCommunicationError(String badThingsHappen) {
    }
}
