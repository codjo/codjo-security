package net.codjo.security.server.api;
import net.codjo.agent.AgentMock;
import net.codjo.sql.server.ConnectionPoolMock;
import net.codjo.sql.server.JdbcServiceHelperMock;
import net.codjo.test.common.LogString;
import net.codjo.test.common.mock.ConnectionMock;
import org.junit.After;
import org.junit.Before;
/**
 *
 */
public abstract class SecurityServiceHelperTestCase {
    protected static final LogString log = new LogString();
    protected SecurityServiceHelper securityServiceHelper;


    @Before
    public void setUp() throws Exception {
        securityServiceHelper = createSecurityServiceHelper();
    }


    @After
    public void tearDown() throws Exception {
        log.clear();
    }


    protected abstract SecurityServiceHelper createSecurityServiceHelper();


    protected LogString logger(String prefix) {
        return new LogString(prefix, log);
    }
}
