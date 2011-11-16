package net.codjo.security.gui.user;
import net.codjo.security.common.message.Role;
import net.codjo.security.common.message.RoleComposite;
import net.codjo.security.gui.model.GuiModelManager;
import org.uispec4j.ListBox;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.UISpecTestCase;
import org.uispec4j.interception.BasicHandler;
import org.uispec4j.interception.WindowInterceptor;
/**
 *
 */
public class RoleTabTest extends UISpecTestCase {
    private RoleTab roleTab;


    public void test_roleList() throws Exception {
        roleTab = create(new ModelBuilder()
                               .addRole("simple")
                               .addRoleComposite("composite_B")
                               .addRoleComposite("composite_A"));

        assertTrue(getCompositeList().contentEquals(new String[]{"composite_A", "composite_B"}));
    }


    public void test_roleList_empty() throws Exception {
        initNoRole();

        assertTrue(getCompositeList().contentEquals(new String[0]));
    }


    public void test_selectOneUser() throws Exception {
        roleTab = create(new ModelBuilder()
                               .addRole("simple")
                               .addRoleComposite("composite_B")
                               .addRoleComposite("composite_A"));

        assertFalse(getMainPanel().getButton("removeCompositeButton").isEnabled());

        getCompositeList().select("composite_A");

        assertTrue(getMainPanel().getTextBox("compositeNameField").textEquals("composite_A"));
        assertTrue(getMainPanel().getButton("removeCompositeButton").isEnabled());

        getCompositeList().clearSelection();

        assertTrue(getMainPanel().getTextBox("compositeNameField").textIsEmpty());
        assertFalse(getMainPanel().getButton("removeCompositeButton").isEnabled());
    }


    public void test_selectOneUser_assignedRoles() throws Exception {
        roleTab = create(new ModelBuilder()
                               .addRoleComposite("composite_B", "role_B1", "role_B2")
                               .addRoleComposite("composite_A"));

        getCompositeList().select("composite_A");

        assertTrue(getAssignedList().isEmpty());

        getCompositeList().select("composite_B");

        assertTrue(getUnassignedList().contentEquals(new String[]{"composite_A"}));
        assertTrue(getAssignedList().contentEquals(new String[]{"role_B1", "role_B2"}));
    }


    public void test_addComposite() throws Exception {
        initNoRole();

        WindowInterceptor
              .init(getMainPanel().getButton("addCompositeButton").triggerClick())
              .process(BasicHandler.init()
                             .assertContainsText("Nom du rôle composite:")
                             .setText("  composite_A  ")
                             .triggerButtonClick("OK"))
              .run();

        assertTrue(getCompositeList().contentEquals(new String[]{"composite_A"}));
    }


    public void test_addComposite_fromModel() throws Exception {
        initNoRole();

        roleTab.getGuiManager().addRole(new RoleComposite("new-composite"));

        assertTrue(getCompositeList().contentEquals(new String[]{"new-composite"}));
    }


    public void test_addRoleToComposite_automaticSelection() throws Exception {
        initNoRole();

        roleTab.getGuiManager().addRole(new RoleComposite("new-composite"));
        getCompositeList().clearSelection();

        roleTab.getGuiManager().addRoleToComposite(new Role("guest"), new RoleComposite("new-composite"));

        assertTrue(getCompositeList().selectionEquals("new-composite"));
    }


    public void test_removeComposite() throws Exception {
        roleTab = create(new ModelBuilder()
                               .addRole("simple")
                               .addRoleComposite("composite_B")
                               .addRoleComposite("composite_A"));

        getCompositeList().select("composite_B");
        getMainPanel().getButton("removeCompositeButton").click();
        assertTrue(getCompositeList().contentEquals(new String[]{"composite_A"}));
        assertTrue(getCompositeList().selectionIsEmpty());
    }


    public void test_removeComposite_usedByUser() throws Exception {
        roleTab = create(new ModelBuilder()
                               .addRole("simple")
                               .addRoleComposite("composite_B")
                               .addRoleComposite("composite_A")
                               .addRoleToUser("composite_B", "user-1")
                               .addRoleToUser("composite_B", "user-2")
                               .addRoleToUser("composite_B", "user-3"));

        getCompositeList().select("composite_B");

        WindowInterceptor
              .init(getMainPanel().getButton("removeCompositeButton").triggerClick())
              .process(BasicHandler.init()
                             .assertContainsText("Ce rôle est affecté à:")
                             .assertContainsText("  * 3 utilisateur(s) : user-1, user-2, etc...")
                             .assertContainsText("Voulez-vous vraiment le supprimer ?")
                             .triggerButtonClick("OK"))
              .run();

        assertTrue(getCompositeList().contentEquals(new String[]{"composite_A"}));
    }


    public void test_removeComposite_usedInComposite() throws Exception {

        roleTab = create(new ModelBuilder()
                               .addRoleComposite("composite_B")
                               .addRoleComposite("composite_A", "composite_B"));

        getCompositeList().select("composite_B");

        WindowInterceptor
              .init(getMainPanel().getButton("removeCompositeButton").triggerClick())
              .process(BasicHandler.init()
                             .assertContainsText("Ce rôle est affecté à:")
                             .assertContainsText("  * 1 rôle(s) : composite_A")
                             .assertContainsText("Voulez-vous vraiment le supprimer ?")
                             .triggerButtonClick("OK"))
              .run();

        assertTrue(getCompositeList().contentEquals(new String[]{"composite_A"}));
    }


    public void test_searchCompositeRole() throws Exception {
        roleTab = create(new ModelBuilder()
                               .addRole("role1")
                               .addRole("role2")
                               .addRoleComposite("composite_B")
                               .addRoleComposite("composite_A", "composite_B"));

        assertTrue(getCompositeList().contentEquals(new String[]{"composite_A", "composite_B"}));

        getCompositeSearchField().setText("composite_A");
        assertTrue(getCompositeList().contentEquals(new String[]{"composite_A"}));

        getCompositeSearchField().setText("composite_B");
        assertTrue(getCompositeList().contentEquals(new String[]{"composite_A", "composite_B"}));

        roleTab.getGuiManager().addRoleToComposite(new Role("role1"), new RoleComposite("composite_A"));

        getCompositeSearchField().setText("role1");
        assertTrue(getCompositeList().contentEquals(new String[]{"composite_A"}));
    }


    public void test_searchCompositeRole_notCaseSensitive() throws Exception {
        roleTab = create(new ModelBuilder()
                               .addRoleComposite("COMPOSITE_A")
                               .addRoleComposite("composite_B")
                               .addRoleComposite("dummy"));

        assertTrue(getCompositeList().contentEquals(new String[]{"COMPOSITE_A", "composite_B", "dummy"}));

        getCompositeSearchField().setText("composite");
        assertTrue(getCompositeList().contentEquals(new String[]{"COMPOSITE_A", "composite_B"}));
    }


    public void test_search_notCanceledWhenModifiedRole() throws Exception {
        roleTab = create(new ModelBuilder()
                               .addRole("role1")
                               .addRole("role2")
                               .addRoleComposite("composite_B")
                               .addRoleComposite("composite_A", "composite_B"));

        getCompositeSearchField().setText("role1");
        assertTrue(getCompositeList().contentEquals(new String[]{}));

        roleTab.getGuiManager().addRoleToComposite(new Role("role1"), new RoleComposite("composite_A"));

        assertTrue(getCompositeList().contentEquals(new String[]{"composite_A"}));
        assertEquals("role1", getCompositeSearchField().getText());
    }


    public void test_searchAssignableRole() throws Exception {
        roleTab = create(new ModelBuilder()
                               .addRole("role1")
                               .addRole("role2")
                               .addRoleComposite("composite_B")
                               .addRoleComposite("composite_A", "composite_B"));

        getCompositeList().select("composite_B");
        assertTrue(getUnassignedList().contentEquals(new String[]{"composite_A", "role1", "role2"}));

        getAssignableSearchField().setText("role1");
        assertTrue(getUnassignedList().contentEquals(new String[]{"role1"}));
    }


    public void test_searchAssignableRole_notCaseSensitive() throws Exception {
        roleTab = create(new ModelBuilder()
                               .addRole("ROLE1")
                               .addRole("role2")
                               .addRole("dummy")
                               .addRoleComposite("composite_A"));

        getCompositeList().select("composite_A");
        assertTrue(getUnassignedList().contentEquals(new String[]{"ROLE1", "dummy", "role2"}));

        getAssignableSearchField().setText("role");
        assertTrue(getUnassignedList().contentEquals(new String[]{"ROLE1", "role2"}));
    }


    public void test_searchAssignableRoleUpdate() {
        roleTab = create(new ModelBuilder()
                               .addRole("role1")
                               .addRole("role2")
                               .addRoleComposite("composite_B")
                               .addRoleComposite("composite_A", "composite_B"));

        getCompositeList().select("composite_A");
        assertTrue(getUnassignedList().contentEquals(new String[]{"role1", "role2"}));

        getAssignableSearchField().setText("role1");
        assertTrue(getUnassignedList().contentEquals(new String[]{"role1"}));

        roleTab.getGuiManager()
              .removeRoleToComposite(new Role("composite_B"), new RoleComposite("composite_A"));
        assertTrue(getUnassignedList().contentEquals(new String[]{"role1"}));

        getCompositeList().select("composite_B");

        assertEquals("role1", getAssignableSearchField().getText());
        assertTrue(getUnassignedList().contentEquals(new String[]{"role1"}));

        getAssignableSearchField().setText("");
        assertTrue(getUnassignedList().contentEquals(new String[]{"composite_A", "role1", "role2"}));
    }


    private TextBox getCompositeSearchField() {
        return getMainPanel().getTextBox("compositeRoleSearchField");
    }


    private TextBox getAssignableSearchField() {
        return getMainPanel().getTextBox("assignableRoleSearchField");
    }


    private static RoleTab create(ModelBuilder modelBuilder) {
        RoleTab tab = new RoleTab();
        tab.initialize(new GuiModelManager(modelBuilder.get()));
        return tab;
    }


    private void initNoRole() {
        roleTab = create(new ModelBuilder());
    }


    private ListBox getCompositeList() {
        return getMainPanel().getListBox("compositeList");
    }


    private ListBox getUnassignedList() {
        return getMainPanel().getListBox("unassignedRoles");
    }


    private ListBox getAssignedList() {
        return getMainPanel().getListBox("assignedRoles");
    }


    private Panel getMainPanel() {
        return new Panel(roleTab.getMainPanel());
    }
}

