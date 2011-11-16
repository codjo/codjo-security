package net.codjo.security.server.plugin;
import net.codjo.ads.AdsTest;
import net.codjo.agent.ContainerConfiguration;
import net.codjo.crypto.common.StringEncrypterException;
import net.codjo.plugin.server.ServerCoreMock;
import net.codjo.security.server.api.SecurityContextFactory;
import net.codjo.security.server.api.SecurityService;
import net.codjo.security.server.api.SessionManagerMock;
import net.codjo.test.common.LogString;
import org.junit.Test;

import static net.codjo.security.server.plugin.AdsSecurityManager.stringEncrypter;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
/**
 *
 */
public class AdsSecurityManagerTest {
    private LogString log = new LogString();
    private AdsSecurityManager adsSecurityManager =
          new AdsSecurityManager(new ServerCoreMock(log),
                                 new SessionManagerMock(new LogString("SessionManager", log)),
                                 new MySecurityServerPluginConfiguration(log));


    @Test
    public void test_assertConfigurationIsValid() throws Exception {
        SecurityServiceConfig config = new SecurityServiceConfig(createDefaultMasterConfigWithAdsValues());
        AdsSecurityManager.assertConfigurationIsValid(config);
    }


    @Test
    public void test_assertConfigurationIsValid_notSet() throws Exception {
        SecurityServiceConfig config = new SecurityServiceConfig(new ContainerConfiguration());

        try {
            AdsSecurityManager.assertConfigurationIsValid(config);
            fail();
        }
        catch (Exception e) {
            assertEquals(
                  "La configuration ads est invalide.\n"
                  + "Veuillez vérifier la valeur de la propriété SecurityService.config.",
                  e.getLocalizedMessage());
        }
    }


    @Test
    public void test_assertConfigurationIsValid_emptyConfiguration() throws Exception {
        ContainerConfiguration configuration = new ContainerConfiguration();
        configuration.setParameter(SecurityServiceConfig.SECURITY_SERVICE_CONFIG, "");
        SecurityServiceConfig config = new SecurityServiceConfig(configuration);

        try {
            AdsSecurityManager.assertConfigurationIsValid(config);
            fail();
        }
        catch (Exception e) {
            assertEquals(
                  "La configuration ads est invalide.\n"
                  + "Veuillez vérifier la valeur de la propriété SecurityService.config.",
                  e.getLocalizedMessage());
        }
    }


    @Test
    public void test_assertConfigurationIsValid_syntaxError() throws Exception {
        ContainerConfiguration configuration = new ContainerConfiguration();
        configuration.setParameter(SecurityServiceConfig.SECURITY_SERVICE_CONFIG, "ads:");
        SecurityServiceConfig config = new SecurityServiceConfig(configuration);

        try {
            AdsSecurityManager.assertConfigurationIsValid(config);
            fail();
        }
        catch (IllegalArgumentException e) {
            assertEquals(
                  "La configuration ads est invalide.\n"
                  + "Veuillez vérifier la valeur de la propriété SecurityService.config.",
                  e.getLocalizedMessage());
        }
    }


    @Test
    public void test_initAds() throws Exception {
        adsSecurityManager.init(new SecurityServiceConfig(createDefaultMasterConfigWithAdsValues()));

        log.assertContent("setSecuritServiceClass(AdsSecurityService)",
                          "SessionManager.addListener(AdsServiceWrapper)",
                          "addGlobalComponent(AdsServiceWrapper)");

        assertEquals(AdsTest.ADSBV_JNLP_URL, adsSecurityManager.getAdsBvJnlpUrl());
    }


    @Test
    public void test_initAds_notEncrypted_shoudFail() throws Exception {
        try {
            adsSecurityManager.init(new SecurityServiceConfig(
                  createDefaultMasterConfigWithAdsValues("notAnEncryptedLogin", "notAnEncryptedPassword")));
            fail();
        }
        catch (StringEncrypterException e) {
            assertTrue(e.getLocalizedMessage().startsWith(
                  "Impossible de decrypter la chaine 'notAnEncryptedLogin'"));
        }
    }


    public static ContainerConfiguration createDefaultMasterConfigWithAdsValues() {
        return createConfigurationWithAdsSecurityConfig(createAdsSecurityServiceConfig(
              stringEncrypter.encrypt(AdsTest.APPLICATION_USER),
              stringEncrypter.encrypt(AdsTest.APPLICATION_PASSWORD),
              AdsTest.LDAP_URL,
              AdsTest.SSL_URL));
    }


    public static ContainerConfiguration createDefaultMasterConfigWithAdsValues(String login,
                                                                                String password) {
        return createConfigurationWithAdsSecurityConfig(createAdsSecurityServiceConfig(login,
                                                                                       password,
                                                                                       AdsTest.LDAP_URL,
                                                                                       AdsTest.SSL_URL));
    }


    private static ContainerConfiguration createConfigurationWithAdsSecurityConfig(String securityConfig) {
        ContainerConfiguration configuration = new ContainerConfiguration();
        configuration.setParameter(SecurityServiceConfig.SECURITY_SERVICE_CONFIG, securityConfig);
        return configuration;
    }


    private static String createAdsSecurityServiceConfig(String encryptedApplicationUser,
                                                         String encryptedApplicationPwd,
                                                         String ldapUrl,
                                                         String sslUrl) {
        return String.format(
              "ads:%s=%s|%s=%s|%s=%s|%s=%s|%s=%s|%s=%s|%s=%s|%s=%s|%s=%s|%s=%s",
              AdsSecurityManager.APPLICATION_USER, encryptedApplicationUser,
              AdsSecurityManager.APPLICATION_PWD, encryptedApplicationPwd,
              AdsSecurityManager.PROJECT_NAME, AdsTest.PROJECT_NAME,
              AdsSecurityManager.LDAP_URL, ldapUrl,
              AdsSecurityManager.SSL_URL, sslUrl,
              AdsSecurityManager.PROJECT_BASE, AdsTest.PROJECT_BASE,
              AdsSecurityManager.ROLE_BASE, AdsTest.ROLE_BASE,
              AdsSecurityManager.PERMISSION_BASE, AdsTest.PERMISSION_BASE,
              AdsSecurityManager.USER_BASE, AdsTest.USER_BASE,
              AdsSecurityManager.ADSBV_JNLP_URL, AdsTest.ADSBV_JNLP_URL);
    }


    private static class MySecurityServerPluginConfiguration extends SecurityServerPluginConfiguration {
        private final LogString log;


        private MySecurityServerPluginConfiguration(LogString log) {
            this.log = log;
        }


        @Override
        public void setSecuritServiceClass(Class<? extends SecurityService> securitServiceClass) {
            log.call("setSecuritServiceClass", securitServiceClass.getSimpleName());
        }


        @Override
        public void setSecurityContextFactory(SecurityContextFactory securityContextFactory) {
            log.call("setSecurityContextFactory", securityContextFactory.getClass().toString());
        }
    }
}
