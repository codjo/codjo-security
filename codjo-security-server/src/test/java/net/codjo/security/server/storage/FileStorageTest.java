package net.codjo.security.server.storage;
import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import net.codjo.security.common.message.DefaultModelManager;
import net.codjo.security.common.message.ModelManager;
import net.codjo.security.common.message.Role;
import net.codjo.security.common.message.RoleComposite;
import net.codjo.security.common.message.User;
import net.codjo.security.server.api.Storage;
import net.codjo.security.server.storage.AbstractStorage.BadConfigurationException;
import net.codjo.test.common.Directory.NotDeletedException;
import net.codjo.test.common.XmlUtil;
import net.codjo.test.common.fixture.DirectoryFixture;
import net.codjo.util.file.FileUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
/**
 *
 */
public class FileStorageTest {
    private DirectoryFixture fixture = DirectoryFixture.newTemporaryDirectoryFixture();
    private File modelFile;
    private FileStorage fileStorage;


    @Before
    public void setUp() throws Exception {
        fixture.doSetUp();

        modelFile = new File(fixture, "test_model.xml");
        fileStorage = new FileStorage(modelFile);
    }


    @After
    public void tearDown() throws NotDeletedException {
        fixture.doTearDown();
    }


    @Test
    public void test_loadModel() throws Exception {
        String model = "<xml version=\"0\">"
                       + "  <model>"
                       + "    <grants>"
                       + "      <entry><user name=\"kasparov\"/><list/></entry>"
                       + "    </grants>"
                       + "  </model>"
                       + "</xml>";
        FileUtil.saveContent(modelFile, model);

        ModelManager modelManager = fileStorage.loadModel();
        Assert.assertArrayEquals(new Role[]{}, modelManager.getUserRoles(new User("kasparov")).toArray());
    }


    @Test
    public void test_loadModel_noFile() throws Exception {
        ModelManager manager = fileStorage.loadModel();

        assertNotNull(manager);
        assertTrue(manager.getRoles().isEmpty());
        assertTrue(manager.getUsers().isEmpty());
    }


    @Test
    public void test_loadModel_emptyModel() throws Exception {
        modelFile.createNewFile();

        ModelManager manager = fileStorage.loadModel();

        assertNotNull(manager);
        assertTrue(manager.getRoles().isEmpty());
        assertTrue(manager.getUsers().isEmpty());
    }


    @Test
    public void test_loadModel_badXmlVersion() throws Exception {
        FileUtil.saveContent(modelFile, "<xml version=\"4\">"
                                        + "  <oldModel></oldModel>"
                                        + "</xml>");
        try {
            fileStorage.loadModel();
            fail();
        }
        catch (BadConfigurationException ex) {
            assertEquals("Paramétrage de la couche sécurité incohérente."
                         + " La version du modèle '4' n'est pas gérée.",
                         ex.getLocalizedMessage());
        }
    }


    @Test
    public void test_saveModel() throws Exception {
        DefaultModelManager modelManager = new DefaultModelManager();
        modelManager.addRoleToUser(new RoleComposite("roleComposite1"), new User("myLogin"));
        modelManager.addRoleToComposite(new Role("role1"), new RoleComposite("roleComposite1"));

        fileStorage.saveModel(modelManager);

        XmlUtil.assertEquivalent("<xml version=\"0\">"
                                 + "  <model>"
                                 + "    <roles>"
                                 + "      <role-composite name=\"roleComposite1\">"
                                 + "        <roles>"
                                 + "          <role name=\"role1\"/>"
                                 + "        </roles>"
                                 + "      </role-composite>"
                                 + "      <role reference=\"/model/roles/role-composite/roles/role\"/>"
                                 + "    </roles>"
                                 + "    <grants>"
                                 + "      <entry>"
                                 + "        <user name=\"myLogin\"/>"
                                 + "        <list>"
                                 + "          <role-composite reference=\"/model/roles/role-composite\"/>"
                                 + "        </list>"
                                 + "      </entry>"
                                 + "    </grants>"
                                 + "  </model>"
                                 + "</xml>",
                                 FileUtil.loadContent(modelFile));
    }


    @Test
    public void test_saveAndLoad() throws Exception {
        fileStorage.saveModel(new DefaultModelManager());
        fileStorage.loadModel();
    }


    @Test
    public void test_getModelTimestamp() throws Exception {
        long actualLastModified = new SimpleDateFormat("yyyy/MM/dd hh:mm")
              .parse("2000/11/02 12:12")
              .getTime();

        modelFile.createNewFile();
        modelFile.setLastModified(actualLastModified);

        assertEquals(new Timestamp(actualLastModified), fileStorage.getModelTimestamp());
    }


    @Test
    public void test_getModelTimeStamp_noFile() throws Exception {
        assertEquals(Storage.NO_TIMESTAMP, fileStorage.getModelTimestamp());
    }
}

