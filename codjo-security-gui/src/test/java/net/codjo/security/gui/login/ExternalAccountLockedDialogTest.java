package net.codjo.security.gui.login;
import org.uispec4j.Trigger;
import org.uispec4j.UISpecTestCase;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;
/**
 *
 */
public class ExternalAccountLockedDialogTest extends UISpecTestCase {
    private ExternalAccountLockedDialog externalAccountLockedDialog;


    public void test_show() throws Exception {
        externalAccountLockedDialog = new ExternalAccountLockedDialog("http://some_url");

        WindowInterceptor.init(new Trigger() {
            public void run() throws Exception {
                externalAccountLockedDialog.show(null);
            }
        }).process(new WindowHandler() {
            @Override
            public Trigger process(Window window) throws Exception {
                assertTrue(window.containsLabel("<html><u>http://some_url</u></html>"));
                return window.getButton("OK").triggerClick();
            }
        }).run();
    }
}
