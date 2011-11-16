package net.codjo.security.server.storage;
import net.codjo.agent.ContainerConfiguration;
import net.codjo.agent.UserId;
import net.codjo.security.server.api.SessionManagerMock;
import net.codjo.sql.server.JdbcManagerMock;
import net.codjo.test.common.LogString;
import org.junit.Before;
import org.junit.Test;

import static net.codjo.security.server.storage.JdbcStorageFactory.assertConfigurationIsValid;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
/**
 *
 */
public class JdbcStorageFactoryTest {
    private JdbcStorageFactory jdbcStorageFactory;
    private LogString log = new LogString();
    private JdbcManagerMock jdbcManagerMock = new JdbcManagerMock(log);
    private UserId userId = UserId.createId("myLogin", "myPassword");
    private ContainerConfiguration containerConfiguration;


    @Before
    public void setUp() throws Exception {
        jdbcManagerMock.createPool(userId, "myLogin", "myPassword");
        log.clear();
        containerConfiguration = new ContainerConfiguration();
        containerConfiguration.setParameter(JdbcStorageFactory.CONFIG_JDBC_LOGIN, "capri");
        containerConfiguration.setParameter(JdbcStorageFactory.CONFIG_JDBC_PASSWORD, "capri_pwd");

        jdbcStorageFactory = new JdbcStorageFactory(jdbcManagerMock,
                                                    new SessionManagerMock(log),
                                                    containerConfiguration
        );
    }


    @Test
    public void test_configuration() throws Exception {
        JdbcConfiguration jdbcConfig = JdbcStorageFactory.createJdbcConfiguration(containerConfiguration);
        assertEquals("capri", jdbcConfig.getApplicationLogin());
        assertEquals("capri_pwd", jdbcConfig.getApplicationPassword());
    }


    @Test
    public void test_assertConfigurationIsValid() throws Exception {
        containerConfiguration = new ContainerConfiguration();
        assertBadConfiguration(containerConfiguration);

        containerConfiguration.setParameter(JdbcStorageFactory.CONFIG_JDBC_LOGIN, "capri");

        assertBadConfiguration(containerConfiguration);

        containerConfiguration.setParameter(JdbcStorageFactory.CONFIG_JDBC_PASSWORD, "capri_pwd");

        assertConfigurationIsValid(containerConfiguration);
    }


    @Test
    public void test_create() throws Exception {
        assertNotNull(jdbcStorageFactory.create(userId));
        log.assertContent("addListener(ConnectionPoolLifecycle), getPool(myLogin)");
    }


    private void assertBadConfiguration(ContainerConfiguration configuration) {
        try {
            assertConfigurationIsValid(configuration);
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals(JdbcStorageFactory.BAD_JDBC_CONFIGURATION_MESSAGE, ex.getMessage());
        }
    }
}
