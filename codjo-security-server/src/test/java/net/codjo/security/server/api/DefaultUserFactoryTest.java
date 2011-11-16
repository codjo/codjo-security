package net.codjo.security.server.api;
import net.codjo.security.common.api.User;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
/**
 *
 */
public class DefaultUserFactoryTest {

    @Test
    public void test_badConfiguration() throws Exception {
        // UGLY : C'est bizarre mais c'est le comportement ancienne plateforme
        //        A voir pour ajouter un echec rapide.
        DefaultUserFactory factory = new DefaultUserFactory("/unknown/path");
        User administrator = factory.getUser(null, null);
        assertNotNull(administrator);
        assertTrue(administrator.isAllowedTo("function"));
        assertEquals("[]", factory.getRoleNames().toString());
    }


    @Test
    public void test_defaultBuilder() throws Exception {
        try {
            DefaultUserFactory.createDefaultUserFactory();
            fail();
        }
        catch (IllegalArgumentException e) {
            assertEquals(
                  "Aucune configuration de securité par défaut n'a été trouvée : le fichier '/conf/role.xml' n'existe pas.",
                  e.getMessage());
        }
    }
}
