/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.security.gui.plugin;
import net.codjo.agent.AgentContainer;
import net.codjo.agent.ContainerConfiguration;
import net.codjo.agent.UserId;
import net.codjo.plugin.common.ApplicationCore;
import net.codjo.plugin.gui.GuiConfiguration;
import net.codjo.plugin.gui.GuiPlugin;
import net.codjo.security.common.api.User;
import net.codjo.security.gui.api.ConfigurationSession;
import net.codjo.security.gui.api.ConfigurationSessionFactory;
import net.codjo.security.gui.communication.DefaultConfigurationSession;
import net.codjo.security.gui.user.UserFormAction;
/**
 *
 */
public class SecurityGuiPlugin implements GuiPlugin {
    private static final String USER_FORM_ACTION_ID = "UserForm";
    static final String USER_NOT_DEFINED_ERROR = "La couche sécurité n'est pas initialisé."
                                                 + " Le plugin MadConnectionPlugin est-il activé ?";
    private final SecurityGuiPluginOperationsImpl operations;
    private final SecurityGuiConfiguration configuration = new DefaultSecurityGuiConfiguration();
    private AgentContainer agentContainer;


    public SecurityGuiPlugin(ApplicationCore applicationCore) {
        applicationCore.setMainBehaviour(new SecurityMainBehaviour(getConfiguration(),
                                                                   applicationCore,
                                                                   new LoginConfigLoader(getConfiguration())));
        applicationCore.addGlobalComponent(ConfigurationSessionFactory.class, new MySessionFactory());
        operations = new SecurityGuiPluginOperationsImpl(applicationCore);
    }


    public SecurityGuiConfiguration getConfiguration() {
        return configuration;
    }


    public SecurityGuiPluginOperations getOperations() {
        return operations;
    }


    public void initContainer(ContainerConfiguration containerConfiguration) throws Exception {
    }


    public void start(AgentContainer container) {
        this.agentContainer = container;
    }


    public void stop() throws Exception {
    }


    public void initGui(GuiConfiguration guiConfiguration) throws Exception {
        guiConfiguration.registerAction(this, USER_FORM_ACTION_ID, UserFormAction.class);
    }


    private class SecurityGuiPluginOperationsImpl implements SecurityGuiPluginOperations {
        private final ApplicationCore applicationCore;


        private SecurityGuiPluginOperationsImpl(ApplicationCore applicationCore) {
            this.applicationCore = applicationCore;
        }


        public User getUser() {
            User user = applicationCore.getGlobalComponent(User.class);
            if (user == null) {
                throw new ConfigurationException(USER_NOT_DEFINED_ERROR);
            }
            return user;
        }


        public ConfigurationSession createConfigurationSession() {
            return new DefaultConfigurationSession(getUser(),
                                                   applicationCore.getGlobalComponent(UserId.class),
                                                   agentContainer);
        }
    }

    private class MySessionFactory implements ConfigurationSessionFactory {
        public ConfigurationSession createConfigurationSession() {
            return operations.createConfigurationSession();
        }
    }
}
