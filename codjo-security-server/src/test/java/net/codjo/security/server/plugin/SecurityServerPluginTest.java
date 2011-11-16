package net.codjo.security.server.plugin;
import java.io.File;
import java.io.IOException;
import java.security.Security;
import net.codjo.ads.AdsTest;
import net.codjo.agent.Agent;
import net.codjo.agent.AgentContainerMock;
import net.codjo.agent.ContainerConfiguration;
import net.codjo.agent.ContainerConfigurationMock;
import net.codjo.agent.ServiceHelper;
import net.codjo.plugin.common.session.SessionManager;
import net.codjo.plugin.server.ServerCoreMock;
import net.codjo.security.common.api.SecurityContextMock;
import net.codjo.security.common.api.User;
import net.codjo.security.server.api.SecurityContextFactory;
import net.codjo.security.server.api.SecurityContextFactoryMock;
import net.codjo.security.server.api.SecurityService;
import net.codjo.security.server.api.StorageFactory;
import net.codjo.security.server.api.UserFactory;
import net.codjo.security.server.api.UserFactoryMock;
import net.codjo.security.server.model.ServerModelManager;
import net.codjo.security.server.service.ldap.LdapSecurityService;
import net.codjo.security.server.storage.FileStorageFactory;
import net.codjo.security.server.storage.JdbcStorageFactory;
import net.codjo.test.common.LogString;
import net.codjo.test.common.PathUtil;
import net.codjo.util.file.FileUtil;
import org.junit.Before;
import org.junit.Test;

import static net.codjo.security.common.message.SecurityEngineConfiguration.adsConfiguration;
import static net.codjo.security.common.message.SecurityEngineConfiguration.defaultConfiguration;
import static net.codjo.security.server.plugin.AdsSecurityManagerTest.createDefaultMasterConfigWithAdsValues;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
/**
 *
 */
public class SecurityServerPluginTest {
    private LogString log = new LogString();
    private SecurityServerPlugin plugin;
    private static final String ROLE_CONFIG = "<roles>"
                                              + "    <role id='consultation'>"
                                              + "        <include>select*</include>"
                                              + "    </role>"
                                              + "</roles>";
    private ServerCoreMock serverCoreMock;


    @Before
    public void setUp() throws Exception {
        serverCoreMock = new ServerCoreMock(log, null);
        plugin = new SecurityServerPlugin(serverCoreMock, new SessionManager());
    }


    @Test
    public void test_checkProperties() throws Exception {
        File roleFile = saveRoleConfigFile("/conf/role.xml", ROLE_CONFIG);
        try {
            plugin.initContainer(new ContainerConfiguration());
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals(JdbcStorageFactory.BAD_JDBC_CONFIGURATION_MESSAGE, ex.getMessage());
        }
        finally {
            roleFile.delete();
        }
    }


    @Test
    public void test_noDefaultRole() throws Exception {
        try {
            ContainerConfiguration configuration = new ContainerConfiguration();
            initValidConfigurationForJdbc(configuration);
            plugin.initContainer(configuration);
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals("Aucune configuration de securité par défaut n'a été trouvée : "
                         + "le fichier '/conf/role.xml' n'existe pas.", ex.getMessage());
        }
    }


    @Test
    public void test_getConfiguration() throws Exception {
        assertNotNull(plugin.getConfiguration());
    }


    @Test
    public void test_init_ldap_jdbcStorage_default() throws Exception {
        initPluginConfiguration();

        ContainerConfiguration configuration = new ContainerConfigurationMock(log);
        initValidConfigurationForJdbc(configuration);

        plugin.initContainer(configuration);

        assertEquals(defaultConfiguration(), plugin.getSecurityEngineConfiguration());

        log.assertContent("addGlobalComponent(StorageFactory, JdbcStorageFactory)",
                          "addGlobalComponent(SecurityContextFactory, SecurityContextFactoryMock)",
                          "addGlobalComponent(UserFactory, UserFactoryMock)",
                          "addGlobalComponent(ServerModelManager, ServerModelManagerImpl)",
                          String.format("addService(%s)", SecurityServiceMock.class.getName()));
    }


    @Test
    public void test_init_ldap_fileStorage() throws Exception {
        initPluginConfiguration();

        ContainerConfiguration configuration = new ContainerConfigurationMock(log);
        initValidConfigurationForFileStorage(configuration);

        plugin.initContainer(configuration);

        StorageFactory storageFactory = serverCoreMock.getGlobalComponent(StorageFactory.class);
        assertTrue(storageFactory instanceof FileStorageFactory);
        assertEquals("C:\\Dev\\tmp\\test.xml",
                     ((FileStorageFactory)storageFactory).getFile().getAbsolutePath());
        assertEquals(defaultConfiguration(), plugin.getSecurityEngineConfiguration());

        log.assertContent("addGlobalComponent(StorageFactory, FileStorageFactory)",
                          "addGlobalComponent(SecurityContextFactory, SecurityContextFactoryMock)",
                          "addGlobalComponent(UserFactory, UserFactoryMock)",
                          "addGlobalComponent(ServerModelManager, ServerModelManagerImpl)",
                          String.format("addService(%s)", SecurityServiceMock.class.getName()));
    }


    @Test
    public void test_init_ads_jdbcStorage() throws Exception {
        initPluginConfiguration();

        ContainerConfiguration configuration = createDefaultMasterConfigWithAdsValues();
        initValidConfigurationForJdbc(configuration);

        plugin.initContainer(configuration);

        assertEquals(adsConfiguration(AdsTest.ADSBV_JNLP_URL), plugin.getSecurityEngineConfiguration());

        log.assertContent("addGlobalComponent(StorageFactory, JdbcStorageFactory)",
                          "addGlobalComponent(AdsServiceWrapper)",
                          "addGlobalComponent(SecurityContextFactory, SecurityContextFactoryMock)",
                          "addGlobalComponent(UserFactory, UserFactoryMock)",
                          "addGlobalComponent(ServerModelManager, ServerModelManagerImpl)");
    }


    @Test
    public void test_init_ads_fileStorage() throws Exception {
        initPluginConfiguration();

        ContainerConfiguration configuration = createDefaultMasterConfigWithAdsValues();
        initValidConfigurationForFileStorage(configuration);

        plugin.initContainer(configuration);

        assertEquals(adsConfiguration(AdsTest.ADSBV_JNLP_URL), plugin.getSecurityEngineConfiguration());

        log.assertContent("addGlobalComponent(StorageFactory, FileStorageFactory)",
                          "addGlobalComponent(AdsServiceWrapper)",
                          "addGlobalComponent(SecurityContextFactory, SecurityContextFactoryMock)",
                          "addGlobalComponent(UserFactory, UserFactoryMock)",
                          "addGlobalComponent(ServerModelManager, ServerModelManagerImpl)");
    }


    @Test
    public void test_init_ldap_default() throws Exception {
        SecurityServerPluginConfiguration conf = plugin.getConfiguration();

        assertEquals(LdapSecurityService.class, conf.getSecuritServiceClass());
        assertEquals(DefaultSecurityContextFactory.class, conf.getSecurityContextFactory().getClass());
        assertNull(conf.getUserFactory());

        File roleFile = saveRoleConfigFile("/conf/role.xml", ROLE_CONFIG);
        ContainerConfiguration configuration = new ContainerConfiguration();
        initValidConfigurationForJdbc(configuration);
        plugin.initContainer(configuration);
        roleFile.delete();

        UserFactory userFactory = conf.getUserFactory();
        assertNotNull(userFactory);
        User user = userFactory.getUser(null, new SecurityContextMock("consultation"));
        assertTrue(user.isAllowedTo("selectAllKetru"));
        assertFalse(user.isAllowedTo("updateKetru"));

        assertEquals(plugin.getSecurityEngineConfiguration(), defaultConfiguration());
    }


    @Test
    public void test_init_ads_notValid() throws Exception {
        ContainerConfiguration containerConfiguration = new ContainerConfiguration();
        containerConfiguration.setParameter(SecurityServiceConfig.SECURITY_SERVICE_CONFIG,
                                            "ads:nain=portekoa");
        initValidConfigurationForJdbc(containerConfiguration);

        File roleFile = saveRoleConfigFile("/conf/role.xml", ROLE_CONFIG);
        try {
            plugin.initContainer(containerConfiguration);
            fail();
        }
        catch (IllegalArgumentException e) {
            assertEquals(String.format(
                  "La configuration ads est invalide.\nVeuillez vérifier la valeur de la propriété %s.",
                  SecurityServiceConfig.SECURITY_SERVICE_CONFIG), e.getLocalizedMessage());
        }
        finally {
            roleFile.delete();
        }
    }


    @Test
    public void test_init_fileStorage_missingPath() throws Exception {
        UserFactoryMock userFactory = new UserFactoryMock();
        userFactory.mockGetRoleNames("guest", "admin");

        plugin.getConfiguration().setSecuritServiceClass(SecurityServiceMock.class);
        plugin.getConfiguration().setUserFactory(userFactory);
        plugin.getConfiguration().setSecurityContextFactory(new SecurityContextFactoryMock());

        ContainerConfiguration containerConfiguration = new ContainerConfiguration();
        containerConfiguration.setParameter(SecurityServiceConfig.SECURITY_SERVICE_CONFIG,
                                            "ldap:nimportQuoi=nimportQui|storage.type=file");

        try {
            plugin.initContainer(containerConfiguration);
            fail();
        }
        catch (Exception e) {
            assertEquals("La stratégie de sécurité via fichier nécessite le paramètre 'storage.file'.",
                         e.getMessage());
        }
    }


    @Test
    public void test_nullStorage() throws Exception {
        initPluginConfiguration();

        ContainerConfiguration configuration = new ContainerConfiguration();
        initValidConfigurationforNullStorage(configuration);

        plugin.initContainer(configuration);

        log.assertContent("addGlobalComponent(StorageFactory, VoidStorageFactory)",
                          "addGlobalComponent(SecurityContextFactory, SecurityContextFactoryMock)",
                          "addGlobalComponent(UserFactory, UserFactoryMock)",
                          "addGlobalComponent(ServerModelManager, ServerModelManagerImpl)");
    }


    @Test
    public void test_init_jdbc_notValid() throws Exception {
        initPluginConfiguration();

        try {
            plugin.initContainer(new ContainerConfigurationMock(log));
            fail();
        }
        catch (Exception e) {
            assertEquals(
                  "La configuration nécessaire pour la connexion BD applicative est absente : Vérifier la présence des properties 'LdapSecurityService.jdbc.login' et 'LdapSecurityService.jdbc.password'",
                  e.getMessage());
        }
    }


    @Test
    public void test_stopRemoveOldComponent() throws Exception {
        initPluginConfiguration();

        ContainerConfigurationMock containerConfiguration = new ContainerConfigurationMock(log);
        initValidConfigurationForJdbc(containerConfiguration);

        plugin.initContainer(containerConfiguration);

        assertNotNull(serverCoreMock.getGlobalComponent(SecurityContextFactory.class));
        assertNotNull(serverCoreMock.getGlobalComponent(UserFactory.class));
        assertNotNull(serverCoreMock.getGlobalComponent(ServerModelManager.class));
        assertNotNull(serverCoreMock.getGlobalComponent(StorageFactory.class));

        plugin.stop();

        assertNull(serverCoreMock.getGlobalComponent(SecurityContextFactory.class));
        assertNull(serverCoreMock.getGlobalComponent(UserFactory.class));
        assertNull(serverCoreMock.getGlobalComponent(ServerModelManager.class));
        assertNull(serverCoreMock.getGlobalComponent(StorageFactory.class));
    }


    @Test
    public void test_startAgents() throws Exception {
        plugin.start(new AgentContainerMock(log));
        plugin.stop();

        log.assertContent("acceptNewAgent(security-manager-agent, SecurityManagerAgent)"
                          + ", security-manager-agent.start()"
                          + ", acceptNewAgent(server-login-agent, ServerLoginAgent)"
                          + ", server-login-agent.start()"
                          + ", removeGlobalComponent(SecurityContextFactory)"
                          + ", removeGlobalComponent(UserFactory)"
                          + ", removeGlobalComponent(ServerModelManager)"
                          + ", removeGlobalComponent(StorageFactory)");
    }


    @Test
    public void test_getPluginOperations() throws Exception {
        plugin.getConfiguration().setSecuritServiceClass(SecurityServiceMock.class);
        plugin.getConfiguration().setUserFactory(new UserFactoryMock());
        ContainerConfigurationMock containerConfigurationMock = new ContainerConfigurationMock(log);
        initValidConfigurationForJdbc(containerConfigurationMock);
        plugin.initContainer(containerConfigurationMock);

        assertNotNull(plugin.getOperations());
    }


    @Test
    public void test_dnsCaching() throws Exception {
        assertEquals("0", Security.getProperty("networkaddress.cache.ttl"));
    }


    private void initPluginConfiguration() {
        plugin.getConfiguration().setSecuritServiceClass(SecurityServiceMock.class);
        plugin.getConfiguration().setUserFactory(new UserFactoryMock());
        plugin.getConfiguration().setSecurityContextFactory(new SecurityContextFactoryMock());
    }


    private File saveRoleConfigFile(String path, String roleXml) throws IOException {
        File roleFile = new File(PathUtil.findTargetDirectory(getClass()), "test-classes" + path);
        roleFile.getParentFile().mkdirs();
        FileUtil.saveContent(roleFile, roleXml);
        return roleFile;
    }


    public static void initValidConfigurationForJdbc(ContainerConfiguration configuration) {
        configuration.setParameter(JdbcStorageFactory.CONFIG_JDBC_LOGIN, "capri");
        configuration.setParameter(JdbcStorageFactory.CONFIG_JDBC_PASSWORD, "capri_pwd");
    }


    public static void initValidConfigurationForFileStorage(ContainerConfiguration configuration) {
        String oldSecurityConfig = configuration.getParameter(SecurityServiceConfig.SECURITY_SERVICE_CONFIG);
        StringBuilder securityServiceConfig = new StringBuilder();

        if (oldSecurityConfig != null) {
            securityServiceConfig.append(oldSecurityConfig).append("|");
        }
        securityServiceConfig.append(String.format("%s=%s|%s=%s",
                                                   SecurityServiceConfig.STORAGE_TYPE_PARAMETER,
                                                   "file",
                                                   SecurityServerPlugin.STORAGE_FILE_PATH_PARAMETER,
                                                   "C:\\Dev\\tmp\\test.xml"));

        configuration.setParameter(SecurityServiceConfig.SECURITY_SERVICE_CONFIG,
                                   securityServiceConfig.toString());
    }


    public static void initValidConfigurationforNullStorage(ContainerConfiguration configuration) {
        configuration.setParameter(SecurityServiceConfig.SECURITY_SERVICE_CONFIG,
                                   "storage.type=void");
    }


    public static class SecurityServiceMock extends SecurityService {

        @SuppressWarnings({"UnusedDeclaration"})
        public SecurityServiceMock(UserFactory userFactory, SecurityContextFactory securityContextFactory) {
        }


        public ServiceHelper getServiceHelper(Agent agent) {
            return null;
        }
    }
}
