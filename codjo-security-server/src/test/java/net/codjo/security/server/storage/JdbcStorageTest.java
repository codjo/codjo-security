package net.codjo.security.server.storage;
import java.io.File;
import java.sql.SQLException;
import java.sql.Timestamp;
import net.codjo.database.common.api.JdbcFixture;
import net.codjo.database.common.api.structure.SqlTable;
import net.codjo.datagen.DatagenFixture;
import net.codjo.security.common.message.DefaultModelManager;
import net.codjo.security.common.message.ModelManager;
import net.codjo.security.common.message.RoleComposite;
import net.codjo.security.common.message.User;
import net.codjo.security.server.storage.AbstractStorage.BadConfigurationException;
import net.codjo.sql.server.ConnectionPoolMock;
import net.codjo.test.common.LogString;
import net.codjo.test.common.fixture.CompositeFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
/**
 *
 */
public class JdbcStorageTest {
    private JdbcFixture jdbc = JdbcFixture.newFixture();
    private DatagenFixture datagen = new DatagenFixture(JdbcStorageTest.class);
    private CompositeFixture fixture = new CompositeFixture(jdbc, datagen);
    private JdbcStorage jdbcStorage;
    private LogString log;
    private ConnectionPoolMock connectionPoolMock;


    @Before
    public void setUp() throws Exception {
        fixture.doSetUp();
        try {
            jdbc.advanced().dropAllObjects();
            datagen.generate();
            jdbc.advanced().executeCreateTableScriptFile(new File(datagen.getSqlPath(), "PM_SEC_MODEL.tab"));
            log = new LogString();
            connectionPoolMock = new ConnectionPoolMock(log);
            connectionPoolMock.mockGetConnection(jdbc.getConnection());
            connectionPoolMock.doLogClassName();
            jdbcStorage = new JdbcStorage(connectionPoolMock);
        }
        catch (Exception e) {
            fixture.doTearDown();
            fail(e.getLocalizedMessage());
        }
    }


    @After
    public void tearDown() throws Exception {
        fixture.doTearDown();
    }


    @Test
    public void test_load() throws Exception {
        jdbc.executeUpdate("insert into PM_SEC_MODEL (VERSION, MODEL) "
                           + "values (0, '<model>"
                           + "<grants>"
                           + "<entry><user name=\"kasparov\"/><list/></entry>"
                           + "</grants>"
                           + "</model>') ");
        ModelManager manager = jdbcStorage.loadModel();

        assertNotNull(manager);
        assertEquals(0, manager.getRoles().size());
        assertEquals(1, manager.getUsers().size());
        assertEquals("[User(kasparov)]", manager.getUsers().toString());
        log.assertContent("getConnection()",
                          String.format("releaseConnection(%s)",
                                        jdbc.getConnection().getClass().getSimpleName()));
    }


    @Test
    public void test_getModelTimestamp() throws Exception {
        insertEmptyModel();

        String date = "2007-05-28 08:12:50";
        jdbc.executeUpdate("update PM_SEC_MODEL set LAST_UPDATE='" + date + "'");

        assertEquals(Timestamp.valueOf(date), jdbcStorage.getModelTimestamp());
        log.assertContent("getConnection()",
                          String.format("releaseConnection(%s)",
                                        jdbc.getConnection().getClass().getSimpleName()));
    }


    @Test
    public void test_getModelTimestamp_noDatabaseRow() throws Exception {
        assertEquals(JdbcStorage.NO_TIMESTAMP, jdbcStorage.getModelTimestamp());
    }


    @Test
    public void test_load_noRow() throws Exception {
        jdbcStorage = new JdbcStorage(connectionPoolMock);

        ModelManager manager = jdbcStorage.loadModel();

        assertNotNull(manager);
        assertTrue(manager.getRoles().isEmpty());
        assertTrue(manager.getUsers().isEmpty());
    }


    @Test
    public void test_load_emptyModel() throws Exception {
        insertEmptyModel();
        ModelManager manager = jdbcStorage.loadModel();

        assertNotNull(manager);
        assertEquals(0, manager.getRoles().size());
        assertEquals(0, manager.getUsers().size());
    }


    @Test
    public void test_load_badXmlVersion() throws Exception {
        insertEmptyModel(-5);
        try {
            jdbcStorage.loadModel();
            fail();
        }
        catch (BadConfigurationException ex) {
            assertEquals("Paramétrage de la couche sécurité incohérente."
                         + " La version du modèle '-5' n'est pas gérée.",
                         ex.getLocalizedMessage());
        }
    }


    @Test
    public void test_load_2rows() throws Exception {
        insertEmptyModel();
        insertEmptyModel();
        try {
            jdbcStorage.loadModel();
            fail();
        }
        catch (BadConfigurationException ex) {
            assertEquals(JdbcStorage.MULTIPLE_LINES_ERROR_MESSAGE, ex.getLocalizedMessage());
        }
    }


    @Test
    public void test_load_addNewRoles() throws Exception {
        insertEmptyModel();

        jdbcStorage = new JdbcStorage(connectionPoolMock);
        ModelManager manager = jdbcStorage.loadModel();

        assertTrue(manager.getRoles().isEmpty());
    }


    @Test
    public void test_save_noRow() throws Exception {
        DefaultModelManager manager = new DefaultModelManager();
        manager.addUser(new User("kasparov"));

        Timestamp updateTime = jdbcStorage.saveModel(manager);

        jdbc.assertContent(SqlTable.table("PM_SEC_MODEL"), new String[][]{
              {"0", updateTime.toString(), "<model>\n"
                                           + "  <grants>\n"
                                           + "    <entry>\n"
                                           + "      <user name=\"kasparov\"/>\n"
                                           + "      <list/>\n"
                                           + "    </entry>\n"
                                           + "  </grants>\n"
                                           + "</model>"}
        });
        log.assertAndClear("getConnection()",
                           String.format("releaseConnection(%s)",
                                         jdbc.getConnection().getClass().getSimpleName()));

        assertEquals(updateTime, jdbcStorage.getModelTimestamp());
        log.assertAndClear("getConnection()",
                           String.format("releaseConnection(%s)",
                                         jdbc.getConnection().getClass().getSimpleName()));
    }


    @Test
    public void test_save_rowAlreadyExists() throws Exception {
        insertEmptyModel();

        DefaultModelManager manager = new DefaultModelManager();
        manager.addUser(new User("kasparov"));
        Timestamp updateTime = jdbcStorage.saveModel(manager);

        jdbc.assertContent(SqlTable.table("PM_SEC_MODEL"), new String[][]{
              {"0", updateTime.toString(), "<model>\n"
                                           + "  <grants>\n"
                                           + "    <entry>\n"
                                           + "      <user name=\"kasparov\"/>\n"
                                           + "      <list/>\n"
                                           + "    </entry>\n"
                                           + "  </grants>\n"
                                           + "</model>"}
        });
    }


    @Test
    public void test_bug_sybase() throws Exception {
        int limit = 500;
        String userPostfix = "user-";

        DefaultModelManager savedModel = new DefaultModelManager();
        for (int i = 0; i < limit; i++) {
            savedModel.addRoleToUser(new RoleComposite("my-role-" + i), new User(userPostfix + i));
        }
        jdbcStorage.saveModel(savedModel);

        ModelManager loadedModel = jdbcStorage.loadModel();
        assertEquals(limit, loadedModel.getRoles().size());
        assertEquals(limit, loadedModel.getUsers().size());
        for (User user : loadedModel.getUsers()) {
            assertEquals(1, loadedModel.getUserRoles(user).size());
            assertTrue(user.getName(), user.getName().startsWith(userPostfix));
        }
    }


    private void insertEmptyModel(int xlmVersion) throws SQLException {
        jdbc.executeUpdate(String.format("insert into PM_SEC_MODEL (VERSION, MODEL) "
                                         + "values (%d, '<model><grants/></model>') ",
                                         xlmVersion));
    }


    private void insertEmptyModel() throws SQLException {
        insertEmptyModel(0);
    }
}
