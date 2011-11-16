package net.codjo.security.gui.communication;
import net.codjo.agent.AgentContainer;
import net.codjo.agent.ContainerFailureException;
import net.codjo.agent.UserId;
import net.codjo.security.common.api.User;
import net.codjo.security.common.message.ModelManager;
import net.codjo.security.common.message.SecurityEngineConfiguration;
import net.codjo.security.gui.api.ConfigurationSession;
import net.codjo.security.gui.api.OpenHandler;
import net.codjo.security.gui.api.SaveHandler;
import net.codjo.security.gui.plugin.SecurityFunctions;
import org.apache.log4j.Logger;
/**
 *
 */
public class DefaultConfigurationSession implements ConfigurationSession {
    private final Logger logger = Logger.getLogger(DefaultConfigurationSession.class);
    private static final String CONFIGURATOR_NICKNAME = "security-configurator";
    private final User user;
    private final UserId userId;
    private final AgentContainer agentContainer;
    private ConfigurationSessionHandler sessionHandler = new ConfigurationSessionHandler();
    private OpenHandler openHandler;
    private ModelManager modelManager;
    private SaveHandler saveHandler;


    public DefaultConfigurationSession(User user, UserId userId, AgentContainer agentContainer) {
        this.user = user;
        this.userId = userId;
        this.agentContainer = agentContainer;
    }


    public void open(OpenHandler handler) {
        if (!user.isAllowedTo(SecurityFunctions.ADMINISTRATE_USER)) {
            throw new SecurityException("Vous n'avez pas les droits de configurer la couche sécurité.");
        }
        openHandler = handler;

        try {
            agentContainer.acceptNewAgent(CONFIGURATOR_NICKNAME,
                                          new SecurityConfiguratorAgent(sessionHandler, userId)).start();
        }
        catch (ContainerFailureException e) {
            throw new SecurityException("Une session de configuration est deja ouverte.", e);
        }
    }


    public ModelManager getModelManager() {
        return modelManager;
    }


    public void save(SaveHandler handler) {
        saveHandler = handler;

        try {
            agentContainer
                  .getAgent(CONFIGURATOR_NICKNAME)
                  .putO2AObject(getModelManager());
        }
        catch (ContainerFailureException e) {
            throw new SecurityException("Erreur technique lors de l'enregistrement.", e);
        }
    }


    public void close() {
        try {
            agentContainer.getAgent(CONFIGURATOR_NICKNAME).kill();
        }
        catch (Throwable e) {
            logger.warn("Impossible de tuer l'agent de configuration");
        }
    }


    private class ConfigurationSessionHandler implements SecurityConfiguratorAgent.Handler {

        public void handleReceiveSecurityModel(ModelManager model,
                                               SecurityEngineConfiguration engineConfiguration) {
            modelManager = model;
            openHandler.handleReceiveSecurityModel(modelManager, engineConfiguration);
            openHandler = null;
        }


        public void handleSaveSucceed() {
            saveHandler.handleSaveSucceed();
            saveHandler = null;
        }


        public void handleCommunicationError(String badThingsHappen) {
            if (openHandler != null) {
                openHandler.handleCommunicationError(badThingsHappen);
            }
            else {
                saveHandler.handleCommunicationError(badThingsHappen);
            }
        }
    }
}
