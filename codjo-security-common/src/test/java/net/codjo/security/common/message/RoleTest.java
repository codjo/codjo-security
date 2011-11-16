package net.codjo.security.common.message;
import junit.framework.TestCase;
/**
 *
 */
public class RoleTest extends TestCase {
    public void test_constructor() throws Exception {
        assertEquals("smith", new Role("smith").getName());
    }


    public void test_name() throws Exception {
        Role role = new Role();
        role.setName("smith");
        assertEquals("smith", role.getName());
    }


    public void test_comparable() throws Exception {
        assertTrue(new Role("smith").compareTo(new Role("bossie")) > 0);
    }


    public void test_equalsHashcode() throws Exception {
        Role first = new Role("smith");
        Role equals = new Role("dupont");

        assertFalse(first.equals(new Object()));
        assertFalse(first.equals(equals));

        assertTrue(first.equals(first));
        assertTrue(first.equals(new Role("smith")));
    }
}
