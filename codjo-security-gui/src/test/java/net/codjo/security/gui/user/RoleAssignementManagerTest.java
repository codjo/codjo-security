package net.codjo.security.gui.user;
import java.util.Arrays;
import net.codjo.security.common.message.DefaultModelManager;
import net.codjo.security.common.message.Role;
import net.codjo.security.common.message.User;
import net.codjo.security.gui.model.GuiModelManager;
import net.codjo.security.gui.user.RoleAssignementToUserUtil.UserRoleContainer;
import net.codjo.security.gui.user.RoleAssignementToUserUtil.UserRoleContainerEventManager;
import org.uispec4j.Key;
import org.uispec4j.ListBox;
import org.uispec4j.Panel;
import org.uispec4j.UISpecTestCase;
import org.uispec4j.interception.BasicHandler;
import org.uispec4j.interception.WindowInterceptor;
/**
 *
 */
public class RoleAssignementManagerTest extends UISpecTestCase {
    private RoleAssignementManager<User> roleAssignementManager;
    private Panel mainPanel;
    private GuiModelManager manager;


    public void test_default() throws Exception {
        assertTrue(getUnassignedList().contentEquals(new String[0]));
        assertTrue(getAssignedList().contentEquals(new String[0]));
    }


    public void test_userWithoutRoles() throws Exception {
        initializeManager();
        roleAssignementManager.setRoleContainer(createContainer("smith"));

        assertTrue(getUnassignedList().contentEquals(new String[]{"admin", "dev", "guest"}));
        assertTrue(getUnassignedList().isEnabled());
        assertTrue(getAssignedList().isEnabled());
    }


    public void test_noUser() throws Exception {
        initializeManager();
        roleAssignementManager.setRoleContainer(createContainer("smith"));
        roleAssignementManager.setRoleContainer(null);

        assertTrue(getUnassignedList().contentEquals(new String[0]));
        assertFalse(getUnassignedList().isEnabled());
        assertFalse(getAssignedList().isEnabled());
    }


    public void test_userWithRoles() throws Exception {
        initializeManager();

        manager.addRoleToUser(new Role("guest"), new User("smith"));

        roleAssignementManager.setRoleContainer(createContainer("smith"));

        assertTrue(getUnassignedList().contentEquals(new String[]{"admin", "dev"}));
        assertTrue(getAssignedList().contentEquals(new String[]{"guest"}));
    }


    public void test_assignRoles() throws Exception {
        initializeManager();

        roleAssignementManager.setRoleContainer(createContainer("smith"));

        getUnassignedList().select("guest");
        mainPanel.getButton("assign").click();

        assertTrue(getUnassignedList().contentEquals(new String[]{"admin", "dev"}));
        assertTrue(getAssignedList().contentEquals(new String[]{"guest"}));

        assertEquals("[Role(guest)]", manager.getUserRoles(new User("smith")).toString());
    }


    public void test_assignRoles_error() throws Exception {
        initializeManager();

        roleAssignementManager.setRoleContainer(new UserRoleContainer(manager, new User("smith")) {
            @Override
            public void addRole(Role role) {
                throw new RuntimeException("niet");
            }
        });

        getUnassignedList().select("guest");

        WindowInterceptor
              .init(mainPanel.getButton("assign").triggerClick())
              .process(BasicHandler.init()
                             .assertContainsText("niet")
                             .triggerButtonClick("OK"))
              .run();

        assertTrue(getUnassignedList().contentEquals(new String[]{"admin", "dev", "guest"}));
        assertTrue(getAssignedList().contentEquals(new String[0]));
    }


    public void test_unassignRoles() throws Exception {
        initializeManager();

        manager.addRoleToUser(new Role("guest"), new User("smith"));
        roleAssignementManager.setRoleContainer(createContainer("smith"));

        getAssignedList().select("guest");
        mainPanel.getButton("unassign").click();

        assertTrue(getUnassignedList().contentEquals(new String[]{"admin", "dev", "guest"}));
        assertTrue(getAssignedList().contentEquals(new String[0]));
    }


    public void test_usingKeyBoard() throws Exception {
        initializeManager();
        roleAssignementManager.setRoleContainer(createContainer("smith"));

        getUnassignedList().select("guest");
        getUnassignedList().pressKey(Key.ENTER);
        assertTrue(getAssignedList().contentEquals(new String[]{"guest"}));

        getAssignedList().select("guest");
        getAssignedList().pressKey(Key.ENTER);

        assertEquals("[]", manager.getUserRoles(new User("smith")).toString());
    }


    public void test_buttonState() throws Exception {
        initializeManager();
        manager.addRoleToUser(new Role("guest"), new User("smith"));
        roleAssignementManager.setRoleContainer(createContainer("smith"));

        assertFalse(mainPanel.getButton("unassign").isEnabled());
        assertFalse(mainPanel.getButton("assign").isEnabled());

        getUnassignedList().selectIndex(0);

        assertFalse(mainPanel.getButton("unassign").isEnabled());
        assertTrue(mainPanel.getButton("assign").isEnabled());

        getAssignedList().selectIndex(0);

        assertTrue(mainPanel.getButton("unassign").isEnabled());
        assertTrue(mainPanel.getButton("assign").isEnabled());
    }


    private RoleAssignementManager.RoleContainer createContainer(String name) {
        return new UserRoleContainer(manager, new User(name));
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        UserTabForInternalManagement gui = new UserTabForInternalManagement();
        roleAssignementManager = new RoleAssignementManager<User>(gui);
        mainPanel = new Panel(gui.getMainPanel());

        DefaultModelManager sub = new DefaultModelManager();
        sub.setRoles(Arrays.asList(new Role("guest"), new Role("admin"), new Role("dev")));
        manager = new GuiModelManager(sub);
    }


    private void initializeManager() {
        roleAssignementManager.listenToThis(new UserRoleContainerEventManager(manager));
    }


    private ListBox getAssignedList() {
        return mainPanel.getListBox("assignedRoles");
    }


    private ListBox getUnassignedList() {
        return mainPanel.getListBox("unassignedRoles");
    }
}