package net.codjo.security.server.plugin;
import de.dit.ads.auth.UserToken;
import de.dit.ads.auth.ldap.Authentication;
import de.dit.ads.auth.ldap.Authorization;
import de.dit.ads.common.AdsLoggingUtilities;
import de.dit.ads.common.AdsSystemException;
import de.dit.ads.common.ProjectEnvironment;
import de.dit.ads.prefs.AdsPreferencesCtx;
import de.dit.ads.prefs.AdsPreferencesFactory;
import java.util.Arrays;
import net.codjo.ads.AdsService;
import net.codjo.agent.ContainerConfiguration;
import net.codjo.crypto.common.StringEncrypter;
import net.codjo.plugin.common.session.SessionListener;
import net.codjo.plugin.common.session.SessionManager;
import net.codjo.plugin.server.ServerCoreMock;
import net.codjo.security.common.Constants;
import net.codjo.security.common.SecurityLevel;
import net.codjo.security.server.api.SecurityContextFactory;
import net.codjo.security.server.api.SecurityService;
import net.codjo.test.common.LogString;
/**
 *
 */
@SuppressWarnings({"UseOfSystemOutOrSystemErr"})
public class AdsConfiguratorHelper {
    // ------------------------------------------------------------------------------------------------
    // Application specific configuration
    //
    public static final String APPLICATION_USER = "uid=SMART_recetteAppl,ou=internal,ou=People,dc=dit,dc=de";
    public static final String APPLICATION_PASSWORD = "lightblue.68";

    // configuration of project environment:
    private static final String PROJECTNAME = "SMART_recette";
    private static final String PROJECTBASE = "dc=SMART_recette,ou=Projects,dc=dit,dc=de";
    private static final String ROLEBASE = "dc=userroles,dc=SMART_recette,ou=Projects,dc=dit,dc=de";
    private static final String PERMISSIONBASE = "dc=permissions,dc=SMART_recette,ou=Projects,dc=dit,dc=de";
    private static final String USERBASE = "ou=People,dc=dit,dc=de";

    public static final String LDAPURL = "ldap://a7sw303.am.agf.fr:40389";
    public static final String SSLURL = "ldap://a7sw303.am.agf.fr:40636";

    // ------------------------------------------------------------------------------------------------
    // Static configuration
    //
    public static final String LDAPURL_BACKUP = "ldap://adsprod.intradit.net:40389";
    public static final String SSLURL_BACKUP = "ldap://adsprod.intradit.net:40636";
    private static final String ADSBV_JNLP_URL = "http://ldapbv.intradit.net/ADSBV/adsbvgui.jnlp";
    public static final boolean USE_SSL = true;
    public static final String USERROLE_TRX = "TRXUser";
    private static final String WINDOWS_LOGIN = "frsimple1";
    private static final String PASSWORD = "clemensfritz*8";


    private AdsConfiguratorHelper() {
    }


    public static void main(String[] args) throws Exception {
        System.out.println("Utilisateur de test = " + WINDOWS_LOGIN + " / " + PASSWORD);
        testConfigIsValid();

        String value = createConfiguration();
        value = value
              .replaceAll("\n", "")
              .replaceAll("\r", "");
        System.out.println("securityServiceConfig=" + value);

        testWithSecurity(value);
        System.out.println("---------------------------------------------- WORK");
    }


    private static void testWithSecurity(String value) throws Exception {
        LogString log = new LogString();

        AdsSecurityManager manager = new AdsSecurityManager(new ServerCoreMock(log),
                                                            new MySessionManager(log),
                                                            new MySecurityServerPluginConfiguration(log));

        AdsService service = manager.buildAdsService(createConfigurationWithAdsSecurityConfig(value));

        net.codjo.ads.UserToken userToken = service.login(WINDOWS_LOGIN,
                                                          PASSWORD,
                                                          SecurityLevel.USER.getValue());
        System.out.println("userToken = " + userToken.getUserId());
        String[] roles = service.rolesFor(userToken);

        System.out.println("roles = " + Arrays.asList(roles));
        service.disconnectAll();
    }


    private static String createConfiguration() {
        return "ads:"
               + "application.user=" + cryptIt(APPLICATION_USER) + "|"
               + "application.pwd=" + cryptIt(APPLICATION_PASSWORD) + "|"
               + "project.name=" + PROJECTNAME + "|"
               + "ldap.url=" + LDAPURL + "|"
               + "ssl.url=" + SSLURL + "|"
               + "ldap.url.backup=" + LDAPURL_BACKUP + "|"
               + "ssl.url.backup=" + SSLURL_BACKUP + "|"
               + "project.base=" + PROJECTBASE + "|"
               + "role.base=" + ROLEBASE + "|"
               + "permission.base=" + PERMISSIONBASE + "|"
               + "user.base=" + USERBASE + "|"
               + "adsbv.jnlp.url=" + ADSBV_JNLP_URL;
    }


    private static String cryptIt(String value) {
        StringEncrypter stringEncrypter = new StringEncrypter(Constants.CLIENT_SERVER_ENCRYPTION_KEY);
        return stringEncrypter.encrypt(value);
    }


    private static void testConfigIsValid() {
        // Log4J configuration

//        PropertyConfigurator.configure("console.lcf");
        AdsLoggingUtilities.setLoggingConfigured(true);

        UserToken utoken = null;

        // authentication module:
        Authentication am = null;

        // authorization module:
        Authorization ui = null;

        // project parameter container:
        ProjectEnvironment projectEnv1 = new ProjectEnvironment(
              PROJECTNAME,
              APPLICATION_USER,
              APPLICATION_PASSWORD,
              PROJECTBASE,
              USERBASE,
              ROLEBASE,
              PERMISSIONBASE
        );

        // configuration of preferences interface:
        AdsPreferencesCtx.setPrefsBase("/de/dit/Projects/TRX/configuration");
        AdsPreferencesCtx.setUserHome("/user");
        AdsPreferencesCtx.setSystemHome("/system");
        AdsPreferencesCtx.setMetaHome("/meta");

        AdsPreferencesCtx.setBootParam(AdsPreferencesFactory.KEY_DS_SSL_HOST_URI, SSLURL);
        AdsPreferencesCtx.setBootParam(AdsPreferencesFactory.KEY_DS_TRUST_STORE_URI, LDAPURL);

        try {
            // create modules
            am = Authentication.createInstance(USE_SSL, LDAPURL, SSLURL);
            ui = Authorization.createInstance(USE_SSL, LDAPURL, SSLURL);
        }
        catch (AdsSystemException adsex) {
            // initialization error
            System.out.println("ADS System Exception : " + adsex);
            adsex.printStackTrace();
            // in this example: exit
            System.exit(-1);
        }

        // user identifier (<domain).<uid>) and password for example user":

        // ####### authentication ####### (extensive exception handling):
        try {

            // user identifier, password, security level (the user must have a security level >= 1):
            utoken = am.authenticate("internal." + WINDOWS_LOGIN, PASSWORD, SecurityLevel.USER.getValue());

            // user information is given in the user token after a successfull login:
            System.out.println("\nUser authenticated: " + utoken.getCommonName() + "\n");

            // exception handling
        }
        catch (Exception ex) {
            System.out.println("ADS error: " + ex);
            ex.printStackTrace();
            System.exit(-1);
        }

        // ######### authorization #############:
        try {

            String[] roles = ui.getRolesForUser(projectEnv1, utoken.getUserId());
            System.out.println();
            for (String role : roles) {
                System.out.println("Role: " + role);
            }
            System.out.println();

            ui.disconnectAll();
            am.disconnectAll();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    // ----------------------------------------------------------------------------------------------------
    private static SecurityServiceConfig createConfigurationWithAdsSecurityConfig(String securityConfig) {
        ContainerConfiguration configuration = new ContainerConfiguration();
        configuration.setParameter(SecurityServiceConfig.SECURITY_SERVICE_CONFIG, securityConfig);
        return new SecurityServiceConfig(configuration);
    }


    private static class MySessionManager extends SessionManager {
        private final LogString log;


        private MySessionManager(LogString log) {
            this.log = log;
        }


        @Override
        public void addListener(SessionListener sessionListener) {
            log.call("addListener", sessionListener.getClass().getSimpleName());
        }
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
