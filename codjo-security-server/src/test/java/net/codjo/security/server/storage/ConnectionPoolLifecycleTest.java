package net.codjo.security.server.storage;
import junit.framework.TestCase;
import net.codjo.agent.UserId;
import net.codjo.sql.server.JdbcManagerMock;
import net.codjo.test.common.LogString;
/**
 *
 */
public class ConnectionPoolLifecycleTest extends TestCase {
    private LogString log = new LogString();
    private ConnectionPoolLifecycle poolLifeCycle;


    @Override
    protected void setUp() throws Exception {
        JdbcManagerMock managerMock = new JdbcManagerMock(new LogString("jdbc", log));
        JdbcConfiguration jdbc = new JdbcConfiguration("dbLogin", "dbPwd");

        poolLifeCycle = new ConnectionPoolLifecycle(managerMock, jdbc);
    }


    public void test_createPool() throws Exception {
        poolLifeCycle.handleSessionStart(UserId.createId("smith", "mysecret"));

        log.assertContent("jdbc.createPool(smith, dbLogin, dbPwd)");
    }


    public void test_destroyPool() throws Exception {
        poolLifeCycle.handleSessionStop(UserId.createId("smith", "mysecret"));

        log.assertContent("jdbc.destroyPool(smith)");
    }
}
