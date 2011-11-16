package net.codjo.security.gui.user;
import java.sql.Timestamp;
import java.util.Date;
import net.codjo.security.common.message.User;
import net.codjo.security.gui.model.GuiModelManager;
import org.uispec4j.Panel;
import org.uispec4j.UISpecTestCase;
/**
 *
 */
public class UserAuditTabTest extends UISpecTestCase {
    private static final String[] HEADER = new String[]{"Nom", "Dernier login", "Dernier logout"};
    private UserAuditTab userAudit;
    private ModelBuilder modelBuilder = new ModelBuilder();
    private Panel panel;


    public void test_noUser() throws Exception {
        userAudit.initialize(new GuiModelManager(modelBuilder.get()));
        assertTrue(panel.getTable().contentEquals(new Object[][]{}));
    }


    public void test_nominalCase() throws Exception {
        userAudit.initialize(new GuiModelManager(modelBuilder
                                                       .addUser(new User("gonnot",
                                                                         date("2010-01-01 10:50:2.0"),
                                                                         date("2010-12-31 18:00:0.0")))
                                                       .addUser(new User("beck",
                                                                         date("1996-01-01 10:50:2.0"),
                                                                         date("1996-12-31 11:00:0.0")))
                                                       .get()));
        assertTrue(panel.getTable().contentEquals(HEADER,
                                                  new Object[][]{
                                                        {"beck", "01/01/96 10:50", "31/12/96 11:00"},
                                                        {"gonnot", "01/01/10 10:50", "31/12/10 18:00"}
                                                  }));
    }


    public void test_neverLogout() throws Exception {
        userAudit.initialize(new GuiModelManager(modelBuilder
                                                       .addUser(new User("gonnot", date("2010-01-01 10:50:2.0"), null))
                                                       .get()));
        assertTrue(panel.getTable().contentEquals(HEADER,
                                                  new Object[][]{
                                                        {"gonnot", "01/01/10 10:50", "jamais"}
                                                  }));
    }


    public void test_addUserCase() throws Exception {
        GuiModelManager manager = new GuiModelManager(modelBuilder.get());
        userAudit.initialize(manager);

        assertTrue(panel.getTable().contentEquals(new Object[][]{}));

        manager.addUser(new User("gonnot", null, null));

        assertTrue(panel.getTable().contentEquals(HEADER,
                                                  new Object[][]{{"gonnot", "jamais", "jamais"}}));
    }


    @Override
    protected void setUp() throws Exception {
        userAudit = new UserAuditTab();
        panel = new Panel(userAudit.getMainPanel());
        super.setUp();
    }


    private static Date date(String date) {
        return Timestamp.valueOf(date);
    }
}
