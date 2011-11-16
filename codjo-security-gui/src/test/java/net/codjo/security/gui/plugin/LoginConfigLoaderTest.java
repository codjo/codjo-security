package net.codjo.security.gui.plugin;
import java.util.List;
import java.util.Properties;
import net.codjo.security.gui.login.LoginConfig;
import net.codjo.security.gui.login.LoginConfig.Ldap;
import net.codjo.security.gui.login.LoginConfig.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
/**
 *
 */
public class LoginConfigLoaderTest {
    private LoginConfigLoader loginConfigLoader
          = new LoginConfigLoader(new DefaultSecurityGuiConfiguration());
    private LoginConfig loginConfig;
    private Properties properties;
    private Properties systemProperties;


    @Before
    public void setUp() throws Exception {
        systemProperties = new Properties();
        systemProperties.putAll(System.getProperties());
        properties = new Properties();

        properties.put("login.default.name", "pims");
        properties.put("login.default.pwd", "pims");

        properties.put("application.name", "PIMS");
        properties.put("application.version", "1.00.00.00");
        properties.put("application.icon", "/images/logo.jpg");

        properties.put("server.url.production", "Production, t3://a7we019:7001");
        properties.put("server.url.recette", "Recette, t3://a7we019:7001");
        properties.put("server.url.integration", "Intégration, t3://a7we019:7001");
        properties.put("server.url.developpement", "Développement, t3://localhost:7001");

        properties.put("server.default.url", "server.url.production");
        properties.put("server.initialContext.factory", "weblogic.jndi.WLInitialContextFactory");
    }


    @After
    public void tearDown() {
        System.setProperties(systemProperties);
    }


    @Test
    public void test_getVersion() throws Exception {
        properties.put("server.default.url", "NONE");
        loginConfig = loginConfigLoader.load(properties);
        assertEquals("1.00.00.00", loginConfig.getApplicationVersion());

        properties.put("server.default.url", "server.url.recette");
        loginConfig = loginConfigLoader.load(properties);
        assertEquals("1.00.00.00", loginConfig.getApplicationVersion());

        properties.put("server.default.url", "server.url.production");
        loginConfig = loginConfigLoader.load(properties);
        assertEquals("1.00.00.00", loginConfig.getApplicationVersion());
    }


    @Test
    public void test_getIconReturnsNullIfNotDefined() throws Exception {
        loginConfig = loginConfigLoader.load(new Properties());
        assertNull(loginConfig.getApplicationIcon());
    }


    @Test
    public void test_getLDaps() throws Exception {
        properties.put("server.ldap.default", "AM");
        properties.put("server.ldap.gdo2", "gdoLdap");
        loginConfig = loginConfigLoader.load(properties);

        List<Ldap> ldapList = loginConfig.getLdapList();
        assertEquals(2, ldapList.size());
        assertEquals("AM", ldapList.get(0).getLabel());
        assertEquals("gdoLdap", ldapList.get(1).getLabel());
    }


    /**
     * Test que getServers renvoie tous les serveurs definie lorsqu'aucun serveur par defaut n'est configuré.
     */
    @Test
    public void test_getServers_NoDefault() throws Exception {
        properties.put("server.default.url", "NONE");
        loginConfig = loginConfigLoader.load(properties);

        List<Server> serverList = loginConfig.getServerList();
        assertEquals(4, serverList.size());
        assertEquals("Développement", serverList.get(0).getName());
        assertEquals("Intégration", serverList.get(1).getName());
        assertEquals("Production", serverList.get(2).getName());
        assertEquals("Recette", serverList.get(3).getName());
    }


    /**
     * Test que getServers renvoie que le serveur definie par defaut lorsque la property "server.default.url" est
     * definie.
     */
    @Test
    public void test_getServers_OneDefault() throws Exception {
        properties.put("server.default.url", "server.url.production");
        properties.put("server.url.production", "Production, t3://a7we019:7001");
        loginConfig = loginConfigLoader.load(properties);

        List<Server> serverList = loginConfig.getServerList();
        assertEquals(1, serverList.size());
        assertEquals("Production", serverList.get(0).getName());
        assertEquals("t3://a7we019:7001", serverList.get(0).getUrl());
    }


    /**
     * Verifie que les property definie dans Systeme outrepassent les réglages du fichier de configuration.
     */
    @Test
    public void test_getServers_OneDefault_FromSystemProperty()
          throws Exception {
        properties.put("server.default.url", "NONE");
        properties.put("server.url.production", "Production, t3://a7we019:7001");
        System.setProperty("server.default.url", "server.url.production");
        loginConfig = loginConfigLoader.load(properties);

        List<Server> serverList = loginConfig.getServerList();
        assertEquals(1, serverList.size());
        assertEquals("Production", serverList.get(0).getName());
        assertEquals("t3://a7we019:7001", serverList.get(0).getUrl());
    }
}
