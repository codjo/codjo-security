package net.codjo.security.gui.user;
import java.util.Date;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import net.codjo.security.common.message.Role;
import net.codjo.security.common.message.RoleComposite;
import net.codjo.security.common.message.User;
import net.codjo.security.gui.model.GuiModelManager;
import org.uispec4j.ListBox;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.UISpecTestCase;

public class UserTabForInternalManagementTest extends UISpecTestCase {
    private UserTabForInternalManagement userTab;


    public void test_userList() throws Exception {
        initWithTwoUsers("smith", "dupont");

        assertTrue(getUserList().contentEquals(new String[]{"dupont", "smith"}));
    }


    public void test_userList_empty() throws Exception {
        initWithNoUser();

        assertTrue(getUserList().contentEquals(new String[0]));
    }


    public void test_selectOneUser() throws Exception {
        initWithTwoUsers("smith", "dupont");

        assertFalse(getMainPanel().getButton("removeUserButton").isEnabled());
        assertUserFields("", "", "");

        getUserList().select("smith");

        assertTrue(getMainPanel().getButton("removeUserButton").isEnabled());
        assertUserFields("smith", "jamais", "jamais");

        getUserList().clearSelection();

        assertFalse(getMainPanel().getButton("removeUserButton").isEnabled());
        assertUserFields("", "", "");
    }


    public void test_selectOneUser_withLoginAndLogout() throws Exception {
        initWithTwoUsers("smith", "dupont");
        user("smith").setLastLogin(java.sql.Timestamp.valueOf("1973-03-18 16:25:00"));
        user("smith").setLastLogout(java.sql.Timestamp.valueOf("2008-02-01 12:20:00"));

        getUserList().select("smith");

        assertUserFields("smith", "18/03/73 16:25", "01/02/08 12:20");
    }


    public void test_addUser() throws Exception {
        initWithNoUser();

        userTab.getGuiManager().addUser(new User("smith"));

        assertTrue(getUserList().contentEquals(new String[]{"smith"}));
    }


    public void test_addRoleToUser() throws Exception {
        initWithNoUser();

        userTab.getGuiManager().addUser(new User("smith"));
        getUserList().clearSelection();

        userTab.getGuiManager().addRoleToUser(new Role("guest"), new User("smith"));

        assertTrue(getUserList().selectionEquals("smith"));
    }


    public void test_removeUser() throws Exception {
        initWithTwoUsers("smith", "dupont");

        getUserList().select("smith");
        getMainPanel().getButton("removeUserButton").click();
        assertTrue(getUserList().contentEquals(new String[]{"dupont"}));
        assertTrue(getUserList().selectionIsEmpty());
    }


    public void test_addRoleComposite() throws Exception {
        initWithTwoUsers("smith", "dupont");

        getUserList().select("smith");
        assertTrue(getMainPanel().getListBox("UnassignedRoles").isEmpty());

        userTab.getGuiManager().addRole(new RoleComposite("Admin"));
        assertTrue(getMainPanel().getListBox("UnassignedRoles").contentEquals(new String[]{"Admin"}));
    }


    public void test_search_byName() throws Exception {
        initWithTwoUsers("smith", "dupont");

        assertTrue(getUserList().contentEquals(new String[]{"dupont", "smith"}));

        getSearchField().setText("dup");
        assertTrue(getUserList().contentEquals(new String[]{"dupont"}));

        getSearchField().setText("DUP");
        assertTrue(getUserList().contentEquals(new String[]{"dupont"}));

        getSearchField().setText("");
        assertTrue(getUserList().contentEquals(new String[]{"dupont", "smith"}));
    }


    public void test_search_byRole() throws Exception {
        initWithTwoUsers("smith", "dupont");
        userTab.getGuiManager().addRoleToUser(new Role("admin"), new User("dupont"));

        getSearchField().setText("admin");

        assertTrue(getUserList().contentEquals(new String[]{"dupont"}));
    }


    public void test_search_byKeywordNew() throws Exception {
        initWithTwoUsers("smith", "dupont");
        user("smith").setLastLogin(new Date());

        getSearchField().setText("new");

        assertTrue(getUserList().contentEquals(new String[]{"dupont"}));
    }


    public void test_search_byKeywordEmpty() throws Exception {
        userTab = create(new ModelBuilder()
                               .addRoleToUser("smith", "admin")
                               .addUser("dupont"));

        getSearchField().setText("empty");

        assertTrue(getUserList().contentEquals(new String[]{"dupont"}));
    }


    public void test_search_byKeywordOld() throws Exception {
        initWithTwoUsers("smith", "dupont");

        user("dupont").setLastLogout(new Date());
        user("smith").setLastLogout(java.sql.Date.valueOf("1998-07-12"));

        getSearchField().setText("old");

        assertTrue(getUserList().contentEquals(new String[]{"smith"}));
    }


    public void test_search_keepSelection() throws Exception {
        initWithTwoUsers("smith", "dupont");

        getUserList().select("smith");
        assertTrue(getMainPanel().getTextBox("nameField").textEquals("smith"));

        getSearchField().setText("smith");

        assertTrue(getUserList().selectionEquals("smith"));
        assertTrue(getMainPanel().getTextBox("nameField").textEquals("smith"));
    }


    public void test_search_keystroke() throws Exception {
        initWithTwoUsers("smith", "dupont");

        getSearchField().setText("bobo");

        //  getUserList().pressKey(Key.control(Key.F));
        pressKey(KeyStroke.getKeyStroke("control F"));

        assertEquals("bobo", ((JTextField)getSearchField().getAwtComponent()).getSelectedText());
    }


    public void test_search_canceledWhenNewUserAdded() throws Exception {
        initWithTwoUsers("smith", "dupont");

        getSearchField().setText("bobo");

        userTab.getGuiManager().addUser(new User("Boris"));

        assertTrue(getSearchField().textEquals(""));
    }


    public void test_search_notCanceledWhenUndoRole() throws Exception {
        initWithTwoUsers("smith", "dupont");
        userTab.getGuiManager().addRoleToUser(new Role("admin"), new User("smith"));

        getSearchField().setText("dupont");

        userTab.getGuiManager().getUndoAction().actionPerformed(null);

        assertTrue(getSearchField().textEquals("dupont"));
    }


    public void test_search_notCanceledWhenModifiedRole() throws Exception {
        initWithTwoUsers("smith", "dupont");
        Role admin = new Role("admin");
        userTab.getGuiManager().addRoleToUser(admin, new User("smith"));

        getSearchField().setText("dupont");

        userTab.getGuiManager().addRoleToUser(admin, new User("dupont"));

        assertTrue(getSearchField().textEquals("dupont"));
        assertEquals(1, userTab.getGuiManager().getUserRoles(new User("dupont")).size());

        userTab.getGuiManager().removeRoleToUser(admin, new User("dupont"));

        assertTrue(getSearchField().textEquals("dupont"));
        assertEquals(0, userTab.getGuiManager().getUserRoles(new User("dupont")).size());
    }


    private void initWithNoUser() {
        userTab = create(new ModelBuilder());
    }


    private void initWithTwoUsers(String user1, String user2) {
        userTab = create(new ModelBuilder()
                               .addUser(user1)
                               .addUser(user2));
    }


    private static UserTabForInternalManagement create(ModelBuilder modelBuilder) {
        UserTabForInternalManagement tab = new UserTabForInternalManagement();
        tab.initialize(new GuiModelManager(modelBuilder.get()), null);
        return tab;
    }


    private User user(String user) {
        return userTab.getGuiManager().getModel().findUser(user);
    }


    private void pressKey(KeyStroke keyStroke) {
        JPanel mainPanel = (JPanel)getMainPanel().getAwtComponent();
        Object actionId = mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(keyStroke);
        mainPanel.getActionMap().get(actionId).actionPerformed(null);
    }


    private Panel getMainPanel() {
        return new Panel(userTab.getMainPanel());
    }


    private ListBox getUserList() {
        return getMainPanel().getListBox("userList");
    }


    private TextBox getSearchField() {
        return getMainPanel().getTextBox("searchField");
    }


    private void assertUserFields(String name, String login, String logout) {
        assertTrue(getMainPanel().getTextBox("nameField").textEquals(name));
        assertTrue(getMainPanel().getTextBox("lastLoginField").textEquals(login));
        assertTrue(getMainPanel().getTextBox("lastLogoutField").textEquals(logout));
    }
}