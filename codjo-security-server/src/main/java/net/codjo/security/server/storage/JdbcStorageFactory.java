package net.codjo.security.server.storage;
import net.codjo.agent.ContainerConfiguration;
import net.codjo.agent.UserId;
import net.codjo.plugin.common.session.SessionManager;
import net.codjo.security.server.api.Storage;
import net.codjo.sql.server.JdbcManager;
/**
 *
 */
public class JdbcStorageFactory extends AbstractStorageFactory {
    public static final String CONFIG_JDBC_LOGIN = "LdapSecurityService.jdbc.login";
    public static final String CONFIG_JDBC_PASSWORD = "LdapSecurityService.jdbc.password";

    public static final String BAD_JDBC_CONFIGURATION_MESSAGE =
          "La configuration nécessaire pour la connexion BD applicative est absente : "
          + "Vérifier la présence des properties '" + CONFIG_JDBC_LOGIN
          + "' et '" + CONFIG_JDBC_PASSWORD + "'";

    private final ContainerConfiguration configuration;
    private final JdbcManager jdbcManager;


    public JdbcStorageFactory(JdbcManager jdbcManager,
                              SessionManager sessionManager,
                              ContainerConfiguration containerConfiguration) {
        this.jdbcManager = jdbcManager;
        configuration = containerConfiguration;

        assertConfigurationIsValid(configuration);

        initConnectionPoolLifeCycle(sessionManager);
    }


    public Storage create(UserId userId) throws Exception {
        return new JdbcStorage(jdbcManager.getPool(userId));
    }


    public void release() {

    }


    private void initConnectionPoolLifeCycle(SessionManager sessionManager) {
        JdbcConfiguration jdbcConfiguration = createJdbcConfiguration(configuration);
        sessionManager.addListener(new ConnectionPoolLifecycle(jdbcManager, jdbcConfiguration));
    }


    static JdbcConfiguration createJdbcConfiguration(ContainerConfiguration configuration) {
        return new JdbcConfiguration(get(configuration, CONFIG_JDBC_LOGIN, null),
                                     get(configuration, CONFIG_JDBC_PASSWORD, null));
    }


    private static String get(ContainerConfiguration configuration, String configKey, String defaultValue) {
        String value = configuration.getParameter(configKey);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }


    static void assertConfigurationIsValid(ContainerConfiguration configuration) {
        if (!(configuration.getParameter(CONFIG_JDBC_LOGIN) != null
              && configuration.getParameter(CONFIG_JDBC_PASSWORD) != null)) {
            throw new IllegalArgumentException(BAD_JDBC_CONFIGURATION_MESSAGE);
        }
    }
}
