package net.codjo.security.server.plugin;
import java.util.Map;
import net.codjo.ads.AdsException;
import net.codjo.ads.AdsService;
import net.codjo.ads.ProjectEnvironment;
import net.codjo.ads.ProjectEnvironmentBuilder;
import net.codjo.crypto.common.StringEncrypter;
import net.codjo.plugin.common.ApplicationCore;
import net.codjo.plugin.common.session.SessionManager;
import net.codjo.security.common.Constants;
import net.codjo.security.server.service.ads.AdsSecurityService;
import net.codjo.security.server.service.ads.AdsServiceWrapper;
/**
 *
 */
public class AdsSecurityManager {
    protected static final String APPLICATION_USER = "application.user";
    protected static final String APPLICATION_PWD = "application.pwd";
    protected static final String PROJECT_NAME = "project.name";
    protected static final String LDAP_URL = "ldap.url";
    protected static final String SSL_URL = "ssl.url";
    protected static final String PROJECT_BASE = "project.base";
    protected static final String ROLE_BASE = "role.base";
    protected static final String PERMISSION_BASE = "permission.base";
    protected static final String USER_BASE = "user.base";
    protected static final String ADSBV_JNLP_URL = "adsbv.jnlp.url";
    static final StringEncrypter stringEncrypter
          = new StringEncrypter(Constants.CLIENT_SERVER_ENCRYPTION_KEY);
    private static final String INVALID_CONFIGURATION_MESSAGE = String.format(
          "La configuration ads est invalide.\nVeuillez vérifier la valeur de la propriété %s.",
          SecurityServiceConfig.SECURITY_SERVICE_CONFIG);
    private final ApplicationCore applicationCore;
    private final SessionManager sessionManager;
    private final SecurityServerPluginConfiguration securityServerPluginConfiguration;
    private AdsServiceWrapper adsServiceWrapper;
    private String adsBvJnlpUrl;
    private static final String[] mandatoryConfigParameters = {APPLICATION_USER, APPLICATION_PWD,
                                                               PROJECT_NAME, LDAP_URL, SSL_URL, PROJECT_BASE,
                                                               ROLE_BASE, PERMISSION_BASE, USER_BASE,
                                                               ADSBV_JNLP_URL};


    AdsSecurityManager(ApplicationCore applicationCore,
                       SessionManager sessionManager,
                       SecurityServerPluginConfiguration securityServerPluginConfiguration) {
        this.applicationCore = applicationCore;
        this.sessionManager = sessionManager;
        this.securityServerPluginConfiguration = securityServerPluginConfiguration;
    }


    public AdsServiceWrapper getAdsServiceWrapper() {
        return adsServiceWrapper;
    }


    public String getAdsBvJnlpUrl() {
        return adsBvJnlpUrl;
    }


    public void init(SecurityServiceConfig securityServiceConfig) throws AdsException {
        assertConfigurationIsValid(securityServiceConfig);

        securityServerPluginConfiguration.setSecuritServiceClass(AdsSecurityService.class);

        adsServiceWrapper = new AdsServiceWrapper(buildAdsService(securityServiceConfig));
        adsBvJnlpUrl = securityServiceConfig.get(ADSBV_JNLP_URL);
        sessionManager.addListener(adsServiceWrapper);
        applicationCore.addGlobalComponent(adsServiceWrapper);
    }


    public void stop() throws AdsException {
        adsServiceWrapper.disconnectAll();
        sessionManager.removeListener(adsServiceWrapper);
        applicationCore.removeGlobalComponent(adsServiceWrapper.getClass());
    }


    @Deprecated
    public static AdsService buildAdsService(Map<String, String> parametersMap) throws AdsException {
        ProjectEnvironment projectEnvironment = new ProjectEnvironmentBuilder()
              .setSecurityPrincipal(stringEncrypter.decrypt(parametersMap.get(APPLICATION_USER)))
              .setSecurityCredentials(stringEncrypter.decrypt(parametersMap.get(APPLICATION_PWD)))
              .setProjectName(parametersMap.get(PROJECT_NAME))
              .setProjectBase(parametersMap.get(PROJECT_BASE))
              .setRoleBase(parametersMap.get(ROLE_BASE))
              .setPermissionBase(parametersMap.get(PERMISSION_BASE))
              .setUserBase(parametersMap.get(USER_BASE))
              .get();
        return new AdsService(parametersMap.get(LDAP_URL),
                              parametersMap.get(SSL_URL),
                              projectEnvironment);
    }


    AdsService buildAdsService(SecurityServiceConfig securityServiceConfig) throws AdsException {
        ProjectEnvironment projectEnvironment = new ProjectEnvironmentBuilder()
              .setSecurityPrincipal(stringEncrypter.decrypt(securityServiceConfig.get(APPLICATION_USER)))
              .setSecurityCredentials(stringEncrypter.decrypt(securityServiceConfig.get(APPLICATION_PWD)))
              .setProjectName(securityServiceConfig.get(PROJECT_NAME))
              .setProjectBase(securityServiceConfig.get(PROJECT_BASE))
              .setRoleBase(securityServiceConfig.get(ROLE_BASE))
              .setPermissionBase(securityServiceConfig.get(PERMISSION_BASE))
              .setUserBase(securityServiceConfig.get(USER_BASE))
              .get();
        return new AdsService(securityServiceConfig.get(LDAP_URL),
                              securityServiceConfig.get(SSL_URL),
                              projectEnvironment);
    }


    public static void assertConfigurationIsValid(SecurityServiceConfig securityServiceConfig) {
        for (String mandatoryParameter : mandatoryConfigParameters) {
            if (!securityServiceConfig.containsKey(mandatoryParameter)) {
                throw new IllegalArgumentException(INVALID_CONFIGURATION_MESSAGE);
            }
        }
    }
}
