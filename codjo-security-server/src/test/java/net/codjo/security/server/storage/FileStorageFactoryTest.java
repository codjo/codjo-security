package net.codjo.security.server.storage;
import net.codjo.agent.UserId;
import net.codjo.test.common.LogString;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
/**
 *
 */
public class FileStorageFactoryTest {
    private FileStorageFactory fileStorageFactory;
    private LogString log = new LogString();
    private UserId userId = UserId.createId("myLogin", "myPassword");


    @Before
    public void setUp() throws Exception {
        log.clear();
        fileStorageFactory = new FileStorageFactory("");
    }


    @Test
    public void test_configuration() throws Exception {
        fileStorageFactory = new FileStorageFactory("C:\\temp.xml", 2);
        assertEquals(2, fileStorageFactory.getTimeout());
        assertEquals("C:\\temp.xml", fileStorageFactory.getFile().getAbsolutePath());
    }


    @Test
    public void test_create() throws Exception {
        assertNotNull(fileStorageFactory.create(userId));
    }


    @Test
    public void test_notReleased() throws Exception {
        assertNotNull(fileStorageFactory.create(userId));
        try {
            fileStorageFactory.create(userId);
            fail();
        }
        catch (Exception e) {
            assertEquals(
                  "Impossible de créer la stratégie de stockage, le fichier est deja en cours d'utilisation",
                  e.getMessage());
        }
    }


    @Test
    public void test_release() throws Exception {
        assertNotNull(fileStorageFactory.create(userId));
        fileStorageFactory.release();

        assertNotNull(fileStorageFactory.create(userId));
    }
}
