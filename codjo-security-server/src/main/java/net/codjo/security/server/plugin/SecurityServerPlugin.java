package net.codjo.security.server.plugin;
import java.io.File;
import java.security.Security;
import net.codjo.ads.AdsException;
import net.codjo.agent.AgentContainer;
import net.codjo.agent.ContainerConfiguration;
import net.codjo.agent.ContainerFailureException;
import net.codjo.plugin.common.ApplicationCore;
import net.codjo.plugin.common.session.SessionManager;
import net.codjo.plugin.server.ServerPlugin;
import net.codjo.security.common.login.LoginProtocol;
import net.codjo.security.common.message.SecurityEngineConfiguration;
import net.codjo.security.server.api.DefaultUserFactory;
import net.codjo.security.server.api.SecurityContextFactory;
import net.codjo.security.server.api.StorageFactory;
import net.codjo.security.server.api.UserFactory;
import net.codjo.security.server.login.ServerLoginAgent;
import net.codjo.security.server.model.ServerModelManager;
import net.codjo.security.server.service.ldap.LdapSecurityService;
import net.codjo.security.server.storage.FileStorageFactory;
import net.codjo.security.server.storage.JdbcStorageFactory;
import net.codjo.security.server.storage.VoidStorageFactory;
import net.codjo.sql.server.JdbcManager;
import net.codjo.util.system.ClassUtil;
import org.apache.log4j.Logger;
/**
 * Plugin permettant d'initialiser la couche securité / gestion des droits.
 */
public class SecurityServerPlugin implements ServerPlugin {
    public static final String SECURITY_FILE_PARAMETER = "security-file";
    private static final Logger LOG = Logger.getLogger(SecurityServerPlugin.class);
    private static final String ENGINE_CONFIGURATION_MESSAGE
          = "Utilisation de la configuration %s pour les authentifications/autorisations";
    static final String STORAGE_FILE_PATH_PARAMETER = "storage.file";

    private final ApplicationCore applicationCore;
    private final SessionManager sessionManager;
    private final ServerModelManagerImpl serverModelManager = new ServerModelManagerImpl();
    private final SecurityServerPluginConfiguration configuration = new SecurityServerPluginConfiguration();
    private final SecurityServerPluginOperations operations;
    private SecurityEngineConfiguration securityEngineConfiguration;
    private AdsSecurityManager adsSecurityManager;


    public SecurityServerPlugin(ApplicationCore applicationCore, SessionManager sessionManager) {
        this.applicationCore = applicationCore;
        this.sessionManager = sessionManager;

        configuration.setSecuritServiceClass(LdapSecurityService.class);
        configuration.setSecurityContextFactory(new DefaultSecurityContextFactory(serverModelManager));
        operations = new SecurityServerPluginOperations(serverModelManager);

        disableDnsCaching();
    }


    public void initContainer(ContainerConfiguration containerConfiguration) throws Exception {
        ModelManagerFileLoader modelManagerFileLoader =
              new ModelManagerFileLoader(extractSecurityFile(containerConfiguration));
        UserFactory newUserFactory = configuration.getUserFactory();
        if (newUserFactory == null) {
            newUserFactory = DefaultUserFactory.createDefaultUserFactory();
            configuration.setUserFactory(newUserFactory);
        }

        SecurityServiceConfig securityServiceConfig = new SecurityServiceConfig(containerConfiguration);

        StorageFactory storageFactory;
        if (securityServiceConfig.storageIs("file")) {
            String filePath = securityServiceConfig.get(STORAGE_FILE_PATH_PARAMETER);
            if (filePath == null) {
                throw new IllegalArgumentException(
                      "La stratégie de sécurité via fichier nécessite le paramètre 'storage.file'.");
            }
            storageFactory = new FileStorageFactory(filePath);
        }
        else if (securityServiceConfig.storageIs("void")) {
            storageFactory = new VoidStorageFactory();
        }
        else {
            storageFactory = new JdbcStorageFactory(applicationCore.getGlobalComponent(JdbcManager.class),
                                                    sessionManager,
                                                    containerConfiguration);
        }
        applicationCore.addGlobalComponent(StorageFactory.class, storageFactory);

        if (securityServiceConfig.engineIs("ads:")) {
            securityEngineConfiguration =
                  initializeAdsEngine(securityServiceConfig, modelManagerFileLoader, newUserFactory);
        }
        else {
            securityEngineConfiguration =
                  initializeDefaultEngine(containerConfiguration, modelManagerFileLoader, newUserFactory);
        }

        applicationCore
              .addGlobalComponent(SecurityContextFactory.class, configuration.getSecurityContextFactory());
        applicationCore.addGlobalComponent(UserFactory.class, configuration.getUserFactory());
        applicationCore.addGlobalComponent(ServerModelManager.class, serverModelManager);
        containerConfiguration.addService(configuration.getSecuritServiceClass().getName());

        operations.setStorageFactory(applicationCore.getGlobalComponent(StorageFactory.class));
    }


    private SecurityEngineConfiguration initializeDefaultEngine(ContainerConfiguration containerConfiguration,
                                                                ModelManagerFileLoader modelManagerFileLoader,
                                                                UserFactory newUserFactory) {
        LOG.info(String.format(ENGINE_CONFIGURATION_MESSAGE, "LDAP"));
        if (configuration.getSecuritServiceClass() == LdapSecurityService.class) {
            LdapSecurityService.assertConfigurationIsValid(containerConfiguration);
        }

        serverModelManager.init(modelManagerFileLoader, newUserFactory);
        return SecurityEngineConfiguration.defaultConfiguration();
    }


    private SecurityEngineConfiguration initializeAdsEngine(SecurityServiceConfig securityServiceConfig,
                                                            ModelManagerFileLoader modelManagerFileLoader,
                                                            UserFactory newUserFactory) throws AdsException {
        LOG.info(String.format(ENGINE_CONFIGURATION_MESSAGE, "ADS"));
        adsSecurityManager = new AdsSecurityManager(applicationCore,
                                                    sessionManager,
                                                    configuration);
        adsSecurityManager.init(securityServiceConfig);

        serverModelManager.init(modelManagerFileLoader,
                                newUserFactory,
                                adsSecurityManager.getAdsServiceWrapper());
        return SecurityEngineConfiguration.adsConfiguration(adsSecurityManager.getAdsBvJnlpUrl());
    }


    private File extractSecurityFile(ContainerConfiguration containerConfiguration) {
        String value = containerConfiguration.getParameter(SECURITY_FILE_PARAMETER);
        return value == null ? new File("./target/config/security.xml") : new File(value);
    }


    public void start(AgentContainer agentContainer) throws ContainerFailureException {
        SecurityManagerAgent agent
              = new SecurityManagerAgent(serverModelManager,
                                         applicationCore.getGlobalComponent(StorageFactory.class),
                                         securityEngineConfiguration);
        agentContainer.acceptNewAgent("security-manager-agent", agent).start();

        String version = ClassUtil.getMainClassVersion();
        ServerLoginAgent serverLoginAgent = new ServerLoginAgent(sessionManager, version);
        agentContainer.acceptNewAgent(LoginProtocol.SERVER_LOGIN_AGENT, serverLoginAgent).start();
    }


    public void stop() throws Exception {
        applicationCore.removeGlobalComponent(SecurityContextFactory.class);
        applicationCore.removeGlobalComponent(UserFactory.class);
        applicationCore.removeGlobalComponent(ServerModelManager.class);
        applicationCore.removeGlobalComponent(StorageFactory.class);
        if (adsSecurityManager != null) {
            adsSecurityManager.stop();
            adsSecurityManager = null;
        }
    }


    public SecurityServerPluginConfiguration getConfiguration() {
        return configuration;
    }


    public SecurityServerPluginOperations getOperations() {
        return operations;
    }


    ServerModelManagerImpl getServerModelManager() {
        return serverModelManager;
    }


    SecurityEngineConfiguration getSecurityEngineConfiguration() {
        return securityEngineConfiguration;
    }


    private void disableDnsCaching() {
        Security.setProperty("networkaddress.cache.ttl", "0");
    }
}
