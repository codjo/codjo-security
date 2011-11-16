package net.codjo.security.common;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
/**
 *
 */
public class SecurityLevelTest extends TestCase {

    @Test
    public void test_values() throws Exception {
        Assert.assertEquals(1, SecurityLevel.USER.getValue());
        Assert.assertEquals(0, SecurityLevel.SERVICE.getValue());
    }


    @Test
    public void test_toEnum() throws Exception {
        Assert.assertEquals(SecurityLevel.USER, SecurityLevel.toEnum(1));
        Assert.assertEquals(SecurityLevel.SERVICE, SecurityLevel.toEnum(0));
    }


    @Test
    public void test_toEnumString() throws Exception {
        Assert.assertEquals(SecurityLevel.USER, SecurityLevel.toEnum("1"));
        Assert.assertEquals(SecurityLevel.SERVICE, SecurityLevel.toEnum("0"));
    }


    @Test
    public void test_toEnumValueNotFound() throws Exception {
        try {
            SecurityLevel.toEnum(2);
        }
        catch (Exception e) {
            Assert.assertEquals("Pas de correspondance pour le security level: '2'", e.getLocalizedMessage());
        }
    }


    @Test
    public void test_toEnumStrValueNotFound() throws Exception {
        try {
            Assert.assertEquals(SecurityLevel.USER, SecurityLevel.toEnum("adsd"));
            fail();
        }
        catch (Exception e) {
            Assert.assertEquals("Pas de correspondance pour le security level: 'adsd'",
                                e.getLocalizedMessage());
        }
    }


    @Test
    public void test_toEnumStrNullValue() throws Exception {
        try {
            Assert.assertEquals(SecurityLevel.USER, SecurityLevel.toEnum(null));
            fail();
        }
        catch (Exception e) {
            Assert.assertEquals("Pas de correspondance pour le security level: 'null'",
                                e.getLocalizedMessage());
        }
    }
}
