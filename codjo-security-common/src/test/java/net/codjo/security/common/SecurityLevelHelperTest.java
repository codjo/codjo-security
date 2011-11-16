package net.codjo.security.common;
import net.codjo.agent.ContainerConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
/**
 *
 */
public class SecurityLevelHelperTest {
    private ContainerConfiguration containerConfiguration;


    @Before
    public void setUp() {
        containerConfiguration = new ContainerConfiguration();
    }


    @Test
    public void test_getSecurityLevel() throws Exception {
        SecurityLevelHelper.setSecurityLevel(containerConfiguration, SecurityLevel.USER);
        Assert.assertEquals(SecurityLevel.USER, SecurityLevelHelper.getSecurityLevel(containerConfiguration));
    }


    @Test
    public void test_setSecurityLevel() throws Exception {
        Assert.assertNull("", SecurityLevelHelper.getSecurityLevel(containerConfiguration));
        SecurityLevelHelper.setSecurityLevel(containerConfiguration, SecurityLevel.USER);
        Assert.assertEquals(SecurityLevel.USER, SecurityLevelHelper.getSecurityLevel(containerConfiguration));
    }
}
