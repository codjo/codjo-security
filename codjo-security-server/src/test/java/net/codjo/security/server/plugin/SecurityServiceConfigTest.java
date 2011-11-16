package net.codjo.security.server.plugin;
import net.codjo.agent.ContainerConfiguration;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
/**
 *
 */
public class SecurityServiceConfigTest {
    private ContainerConfiguration containerConfiguration = new ContainerConfiguration();
    private SecurityServiceConfig securityServiceConfig;


    @Test
    public void test_load() {
        containerConfiguration.setParameter(SecurityServiceConfig.SECURITY_SERVICE_CONFIG,
                                            "ads:param1=value1|param2=value2");

        securityServiceConfig = new SecurityServiceConfig(containerConfiguration);

        assertEquals("value1", securityServiceConfig.get("param1"));
        assertEquals("value2", securityServiceConfig.get("param2"));
    }


    @Test
    public void test_emptyConfiguration() throws Exception {
        securityServiceConfig = new SecurityServiceConfig(containerConfiguration);

        assertFalse(securityServiceConfig.entrySet().iterator().hasNext());
    }


    @Test
    public void test_configurationIsAVariable() throws Exception {
        containerConfiguration.setParameter(SecurityServiceConfig.SECURITY_SERVICE_CONFIG,
                                            "@securityServiceConfig@");
        securityServiceConfig = new SecurityServiceConfig(containerConfiguration);

        assertFalse(securityServiceConfig.entrySet().iterator().hasNext());
    }


    @Test
    public void test_engineIs_ads() throws Exception {
        containerConfiguration.setParameter(SecurityServiceConfig.SECURITY_SERVICE_CONFIG,
                                            "ads:param1=value1|param2=value2");
        securityServiceConfig = new SecurityServiceConfig(containerConfiguration);

        assertTrue(securityServiceConfig.engineIs("ads"));
    }


    @Test
    public void test_storageIs_file() throws Exception {
        containerConfiguration.setParameter(SecurityServiceConfig.SECURITY_SERVICE_CONFIG,
                                            "storage.type=file");
        securityServiceConfig = new SecurityServiceConfig(containerConfiguration);

        assertTrue(securityServiceConfig.storageIs("file"));
    }
}
