package net.codjo.security.server.plugin;
import java.io.File;
import junit.framework.TestCase;
import net.codjo.security.common.message.ModelManager;
import net.codjo.security.common.message.User;
import net.codjo.test.common.PathUtil;
/**
 *
 */
public class ModelManagerFileLoaderTest extends TestCase {

    public void test_load() throws Exception {
        ModelManagerFileLoader loader = new ModelManagerFileLoader(toFile("ModelManagerFileLoaderTest.xml"));

        ModelManager model = loader.load();

        assertNotNull(model);
        assertEquals(1, model.getRoles().size());
        assertEquals(1, model.getUsers().size());
        assertEquals("guest", model.getUserRoles(new User("smith")).get(0).getName());
    }


    public void test_load_fileDoesNotExist() throws Exception {
        ModelManagerFileLoader loader = new ModelManagerFileLoader(new File("/do/not/exist/security.xml"));

        ModelManager model = loader.load();

        assertNotNull(model);
        assertEquals(0, model.getRoles().size());
        assertEquals(0, model.getUsers().size());
    }


    public void test_load_fileIsNull() throws Exception {
        ModelManagerFileLoader loader = new ModelManagerFileLoader(null);

        ModelManager model = loader.load();

        assertNotNull(model);
        assertEquals(0, model.getRoles().size());
        assertEquals(0, model.getUsers().size());
    }


    private File toFile(String fileName) {
        return new File(PathUtil.findResourcesFileDirectory(ModelManagerFileLoaderTest.class), fileName);
    }
}
