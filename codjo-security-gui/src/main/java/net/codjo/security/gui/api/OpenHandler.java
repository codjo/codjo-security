package net.codjo.security.gui.api;
import net.codjo.security.common.message.ModelManager;
import net.codjo.security.common.message.SecurityEngineConfiguration;
/**
 *
 */
public interface OpenHandler extends CommunicationHandler {

    void handleReceiveSecurityModel(ModelManager modelManager,
                                    SecurityEngineConfiguration engineConfiguration);
}
