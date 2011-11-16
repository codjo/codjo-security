package net.codjo.security.common.message;
import java.util.Date;
import junit.framework.TestCase;
/**
 *
 */
public class UserTest extends TestCase {

    public void test_equalsHashcode() throws Exception {
        User smith = new User("smith");
        User dupont = new User("dupont");

        assertFalse(smith.equals(new Object()));
        assertFalse(smith.equals(dupont));

        assertTrue(smith.equals(smith));
        assertTrue(smith.equals(new User("smith")));
    }


    public void test_ignoreCase() throws Exception {
        User smith = new User("smith");
        User strangeSmith = new User("SmiTh");

        assertTrue(smith.equals(strangeSmith));
        assertTrue(strangeSmith.equals(strangeSmith));
        assertEquals(smith.hashCode(), strangeSmith.hashCode());
    }


    public void test_comparable() throws Exception {
        assertTrue(new User("smith").compareTo(new User("bossie")) > 0);
    }


    public void test_lastLogin() throws Exception {
        assertEquals(null, new User("smith").getLastLogin());

        User smith = new User("smith", new Date(0));
        assertEquals(0, smith.getLastLogin().getTime());

        smith.setLastLogin(new Date(10));
        assertEquals(10, smith.getLastLogin().getTime());
    }


    public void test_lastLogout() throws Exception {
        assertEquals(null, new User("smith").getLastLogout());

        User smith = new User("smith");
        smith.setLastLogout(new Date(10));
        assertEquals(10, smith.getLastLogout().getTime());
    }


    public void test_name() throws Exception {
        assertEquals("smith", new User("smith").getName());
    }
}
