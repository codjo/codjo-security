package net.codjo.security.gui.user;
import org.uispec4j.Panel;
import org.uispec4j.UISpecTestCase;

import static net.codjo.security.common.message.SecurityEngineConfiguration.adsConfiguration;
/**
 *
 */
public class UserTabForExternalManagementTest extends UISpecTestCase {
    private UserTabForExternalManagement tab = new UserTabForExternalManagement();


    public void test_buttonManagement() throws Exception {
        assertFalse(mainPanel().getButton().isEnabled());
        tab.initialize(null, adsConfiguration("http://my.jnlp"));
        assertTrue(mainPanel().getButton().isEnabled());
    }


    private Panel mainPanel() {
        return new Panel(tab.getMainPanel());
    }
}
