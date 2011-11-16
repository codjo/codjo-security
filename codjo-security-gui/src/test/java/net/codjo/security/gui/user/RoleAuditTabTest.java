package net.codjo.security.gui.user;
import net.codjo.security.common.message.RoleComposite;
import net.codjo.security.gui.model.GuiModelManager;
import org.uispec4j.ListBox;
import org.uispec4j.Panel;
import org.uispec4j.UISpecTestCase;
/**
 *
 */
public class RoleAuditTabTest extends UISpecTestCase {
    private RoleAuditTab roleAuditTab;


    public void test_noDefinedRoles() throws Exception {
        roleAuditTab = create(model());

        assertTrue(getList().contentEquals(new String[]{}));
    }


    public void test_nominalCase() throws Exception {
        roleAuditTab = create(model()
                                    .addRole("zorro")
                                    .addRole("superman"));

        assertTrue(getList().contentEquals(new String[]{"superman", "zorro"}));
    }


    public void test_copy() throws Exception {
        roleAuditTab = create(model()
                                    .addRole("batman")
                                    .addRole("superman")
                                    .addRole("zorro"));

        getList().selectIndices(new int[]{0, 1});

        String result = roleAuditTab.selectionToString();
        assertEquals("batman\nsuperman", result);
    }


    public void test_modelChange() throws Exception {
        roleAuditTab = new RoleAuditTab();
        GuiModelManager manager = new GuiModelManager(model().addRole("zorro").get());
        roleAuditTab.initialize(manager);

        assertTrue(getList().contentEquals(new String[]{"zorro"}));

        manager.addRole(new RoleComposite("superman"));

        assertTrue(getList().contentEquals(new String[]{"superman", "zorro"}));
    }


    private static RoleAuditTab create(ModelBuilder modelBuilder) {
        RoleAuditTab tab = new RoleAuditTab();
        tab.initialize(new GuiModelManager(modelBuilder.get()));
        return tab;
    }


    private ListBox getList() {
        return new Panel(roleAuditTab.getMainPanel()).getListBox();
    }


    private static ModelBuilder model() {
        return new ModelBuilder();
    }
}
