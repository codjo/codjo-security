package net.codjo.security.common.message;
import java.util.Set;
import java.util.TreeSet;
import junit.framework.Assert;
/**
 *
 */
public class AssertUtil {
    private AssertUtil() {
    }


    public static <T> void assertSetEquals(String expected, Set<T> actual) {
        Assert.assertEquals(expected, new TreeSet<T>(actual).toString());
    }
}
