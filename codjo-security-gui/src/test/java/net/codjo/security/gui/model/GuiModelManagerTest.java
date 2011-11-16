package net.codjo.security.gui.model;
import javax.swing.Action;
import junit.framework.TestCase;
import net.codjo.security.common.message.DefaultModelManager;
import net.codjo.security.common.message.Role;
import net.codjo.security.common.message.RoleComposite;
import net.codjo.security.common.message.User;
import net.codjo.test.common.LogString;
/**
 *
 */
public class GuiModelManagerTest extends TestCase {
    private LogString log = new LogString();
    private final GuiModelManager guiManager = new GuiModelManager(new DefaultModelManager());


    public void test_userRolesEvent() throws Exception {

        guiManager.addUserContentListener(new GuiListenerMock<ObjectContentEvent<User>>());

        guiManager.addRoleToUser(new Role("guest"), new User("smith"));
        guiManager.removeRoleToUser(new Role("guest"), new User("smith"));

        log.assertContent("eventTriggered(ObjectContentEvent(ROLE_ADDED,User(smith),Role(guest))), "
                          + "eventTriggered(ObjectContentEvent(ROLE_REMOVED,User(smith),Role(guest)))");
    }


    public void test_userRolesEvent_removeLisener() throws Exception {
        GuiListenerMock<ObjectContentEvent<User>> listener = createListener();
        guiManager.addUserContentListener(listener);
        guiManager.removeUserContentListener(listener);

        guiManager.addRoleToUser(new Role("guest"), new User("smith"));

        log.assertContent("");
    }


    public void test_userEvent() throws Exception {
        GuiListenerMock<ObjectEvent<User>> listener = createListener();
        guiManager.addUserListener(listener);

        guiManager.removeUser(new User("smith"));

        log.assertContent("eventTriggered(ObjectEvent(REMOVED,User(smith)))");
        log.clear();

        guiManager.removeUserListener(listener);

        guiManager.removeUser(new User("smith"));
        log.assertContent("");
    }


    public void test_userEvent_add() throws Exception {
        GuiListenerMock<ObjectEvent<User>> listener = createListener();
        guiManager.addUserListener(listener);

        guiManager.addUser(new User("nicolas"));

        log.assertContent("eventTriggered(ObjectEvent(ADDED,User(nicolas)))");
    }


    public void test_addUser() throws Exception {
        guiManager.addUser(new User("neo"));
        try {
            guiManager.addUser(new User("neo"));
            fail();
        }
        catch (ObjectAlreadyExistsException ex) {
            assertEquals("L'utilisateur 'neo' existe déjà.", ex.getMessage());
        }
    }


    public void test_addRole() throws Exception {
        guiManager.addRole(new Role("neo"));
        try {
            guiManager.addRole(new Role("neo"));
            fail();
        }
        catch (ObjectAlreadyExistsException ex) {
            assertEquals("Le rôle 'neo' existe déjà.", ex.getMessage());
        }
        try {
            guiManager.addRole(new RoleComposite("neo"));
            fail();
        }
        catch (ObjectAlreadyExistsException ex) {
            assertEquals("Le rôle 'neo' existe déjà.", ex.getMessage());
        }
    }


    public void test_addRole_notCaseSensitive() throws Exception {
        guiManager.addRole(new Role("neo"));
        try {
            guiManager.addRole(new Role("Neo"));
            fail();
        }
        catch (ObjectAlreadyExistsException ex) {
            assertEquals("Le rôle 'Neo' existe déjà.", ex.getMessage());
        }
        try {
            guiManager.addRole(new RoleComposite("nEo"));
            fail();
        }
        catch (ObjectAlreadyExistsException ex) {
            assertEquals("Le rôle 'nEo' existe déjà.", ex.getMessage());
        }
    }


    public void test_addRoleToComposite() throws Exception {
        guiManager.addRoleToComposite(new RoleComposite("father"), new RoleComposite("grandfather"));
        guiManager.addRoleToComposite(new RoleComposite("son"), new RoleComposite("father"));

        try {
            guiManager.addRoleToComposite(new RoleComposite("father"), new RoleComposite("son"));
            fail();
        }
        catch (CycleException ex) {
            assertEquals("Cycle détecté. Le rôle 'father' est un rôle père de 'son'", ex.getMessage());
        }
        try {
            guiManager.addRoleToComposite(new RoleComposite("grandfather"), new RoleComposite("son"));
            fail();
        }
        catch (CycleException ex) {
            assertEquals("Cycle détecté. Le rôle 'grandfather' est un rôle père de 'son'", ex.getMessage());
        }
    }


    public void test_hasGrant() throws Exception {
        User neo = new User("neo");

        guiManager.addUser(neo);
        assertFalse(guiManager.hasGrants(neo));

        guiManager.addRoleToUser(new Role("gues"), neo);
        assertTrue(guiManager.hasGrants(neo));
    }


    public void test_undo_actions() throws Exception {
        Action undo = guiManager.getUndoAction();
        assertEquals("undo", undo.getValue(Action.NAME));
        assertFalse(undo.isEnabled());

        Action redo = guiManager.getRedoAction();
        assertEquals("redo", redo.getValue(Action.NAME));
        assertFalse(redo.isEnabled());
    }


    public void test_undo_addUser() throws Exception {
        guiManager.addUserListener(new GuiListenerMock<ObjectEvent<User>>());

        Action undo = guiManager.getUndoAction();
        Action redo = guiManager.getRedoAction();

        assertFalse(undo.isEnabled());
        assertFalse(redo.isEnabled());

        guiManager.addUser(new User("bush"));

        log.clear();
        assertTrue(undo.isEnabled());
        assertFalse(redo.isEnabled());

        undo.actionPerformed(null);

        log.assertContent("eventTriggered(ObjectEvent(REMOVED,User(bush)))");
        log.clear();
        assertEquals("[]", guiManager.getUsers().toString());
        assertFalse(undo.isEnabled());
        assertTrue(redo.isEnabled());

        redo.actionPerformed(null);

        log.assertContent("eventTriggered(ObjectEvent(ADDED,User(bush)))");
        log.clear();
        assertEquals("[User(bush)]", guiManager.getUsers().toString());
        assertTrue(undo.isEnabled());
        assertFalse(redo.isEnabled());
    }


    public void test_undo_removeUser() throws Exception {
        guiManager.addUserListener(new GuiListenerMock<ObjectEvent<User>>());

        guiManager.addUser(new User("smith"));
        guiManager.removeUser(new User("smith"));
        log.clear();

        guiManager.getUndoAction().actionPerformed(null);

        log.assertContent("eventTriggered(ObjectEvent(ADDED,User(smith)))");
        assertEquals("[User(smith)]", guiManager.getUsers().toString());
    }


    public void test_undo_addRole() throws Exception {
        guiManager.addUserContentListener(new GuiListenerMock<ObjectContentEvent<User>>());
        guiManager.addRoleToUser(new Role("guest"), new User("smith"));
        log.clear();

        guiManager.getUndoAction().actionPerformed(null);

        log.assertContent("eventTriggered(ObjectContentEvent(ROLE_REMOVED,User(smith),Role(guest)))");
        assertEquals("[]", guiManager.getUserRoles(new User("smith")).toString());

        guiManager.getRedoAction().actionPerformed(null);

        assertEquals("[Role(guest)]", guiManager.getUserRoles(new User("smith")).toString());
    }


    public void test_undo_removeRole() throws Exception {
        guiManager.addUserContentListener(new GuiListenerMock<ObjectContentEvent<User>>());
        guiManager.addRoleToUser(new Role("guest"), new User("smith"));
        guiManager.removeRoleToUser(new Role("guest"), new User("smith"));
        log.clear();

        guiManager.getUndoAction().actionPerformed(null);

        log.assertContent("eventTriggered(ObjectContentEvent(ROLE_ADDED,User(smith),Role(guest)))");
        assertEquals("[Role(guest)]", guiManager.getUserRoles(new User("smith")).toString());

        guiManager.getRedoAction().actionPerformed(null);

        assertEquals("[]", guiManager.getUserRoles(new User("smith")).toString());
    }


    public void test_compositeEvent_removeComposite() throws Exception {
        GuiListenerMock<ObjectEvent<RoleComposite>> listener = createListener();

        guiManager.addRoleToComposite(new Role("simple"), new RoleComposite("composite"));

        guiManager.addCompositeListener(listener);

        guiManager.removeRole(new RoleComposite("composite"));
        log.assertContent("eventTriggered(ObjectEvent(REMOVED,RoleComposite(composite)))");
        assertEquals("[Role(simple)]", guiManager.getAllRoles().toString());
    }


    public void test_compositeContentEvent_add() throws Exception {
        GuiListenerMock<ObjectContentEvent<RoleComposite>> listener = createListener();
        guiManager.addCompositeContentListener(listener);

        guiManager.addRoleToComposite(new Role("simple"), new RoleComposite("composite"));
        log.assertContent("eventTriggered(ObjectContentEvent(ROLE_ADDED"
                          + ",RoleComposite(composite)"
                          + ",Role(simple)))");

        guiManager.removeCompositeContentListener(listener);
        log.clear();

        guiManager.addRoleToComposite(new Role("simple2"), new RoleComposite("composite2"));
        log.assertContent("");
    }


    public void test_compositeContentEvent_remove() throws Exception {
        RoleComposite composite = new RoleComposite("composite");
        guiManager.addRoleToComposite(new Role("simple"), composite);

        GuiListener<ObjectContentEvent<RoleComposite>> listener = createListener();
        guiManager.addCompositeContentListener(listener);

        guiManager.removeRoleToComposite(new Role("simple"), new RoleComposite("composite"));

        log.assertContent("eventTriggered(ObjectContentEvent(ROLE_REMOVED"
                          + ",RoleComposite(composite)"
                          + ",Role(simple)))");
        assertEquals("[]", composite.getRoles().toString());
    }


    public void test_findUsages_inComposite() throws Exception {
        RoleComposite adminVl = new RoleComposite("admin_vl");
        RoleComposite admin = new RoleComposite("admin");
        guiManager.addRole(adminVl);
        guiManager.addRole(admin);

        assertEquals("[]", guiManager.findUsages(adminVl).toString());

        guiManager.addRoleToComposite(adminVl, admin);

        assertEquals("[RoleComposite(admin)]", guiManager.findUsages(adminVl).toString());
    }


    public void test_findUsages_inUser() throws Exception {
        assertFindUser(new Role("admin"));
    }


    public void test_findUsages_inUser_composite() throws Exception {
        assertFindUser(new RoleComposite("admin"));
    }


    private void assertFindUser(Role admin) {
        guiManager.addRole(admin);

        guiManager.addUser(new User("god"));
        guiManager.addUser(new User("smith"));

        assertEquals("[]", guiManager.findUserUsages(admin).toString());

        guiManager.addRoleToUser(admin, new User("god"));

        assertEquals("[User(god)]", guiManager.findUserUsages(admin).toString());
    }


    private <T> GuiListenerMock<T> createListener() {
        //noinspection unchecked
        return new GuiListenerMock();
    }


    private class GuiListenerMock<T> implements GuiListener<T> {
        public void eventTriggered(T event) {
            log.call("eventTriggered", event);
        }
    }
}
