package net.codjo.security.server.storage;
import java.sql.Timestamp;
import net.codjo.security.common.message.DefaultModelManager;
import net.codjo.security.common.message.ModelManager;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
/**
 *
 */
public class VoidStorageTest {
    private VoidStorage voidStorage = new VoidStorage();


    @Test
    public void test_loadModel() throws Exception {
        ModelManager modelManager = voidStorage.loadModel();

        assertNotNull(modelManager);
    }


    @Test
    public void test_saveModel() throws Exception {
        Timestamp timestamp = voidStorage.saveModel(new DefaultModelManager());

        assertEquals(0, timestamp.getTime());
    }


    @Test
    public void test_getModelTimestamp() throws Exception {
        Timestamp timestamp = voidStorage.getModelTimestamp();

        assertEquals(0, timestamp.getTime());
    }
}
