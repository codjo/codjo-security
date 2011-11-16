/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.security.common.message;
import java.util.Arrays;
import java.util.List;
import junit.framework.TestCase;
import net.codjo.test.common.LogString;

import static net.codjo.security.common.message.AssertUtil.assertSetEquals;
/**
 *
 */
public class DefaultModelManagerTest extends TestCase {
    private LogString log = new LogString();
    private DefaultModelManager manager;


    public void test_users() throws Exception {
        assertNotNull(manager.getUsers());
        assertEquals(0, manager.getUsers().size());

        manager.addUser(new User("smith"));
        manager.addUser(new User("boissie"));

        assertSetEquals("[User(boissie), User(smith)]", manager.getUsers());

        manager.addRoleToUser(new Role("admin"), new User("smith"));
        manager.removeUser(new User("smith"));

        assertSetEquals("[User(boissie)]", manager.getUsers());
        assertEquals(0, manager.getUserRoles(new User("smith")).size());
    }


    public void test_user_unmodifiable() throws Exception {
        manager.addUser(new User("smith"));

        try {
            manager.getUsers().clear();
            fail();
        }
        catch (UnsupportedOperationException ex) {
            ; // Ok
        }
        assertSetEquals("[User(smith)]", manager.getUsers());
    }


    public void test_user_add() throws Exception {
        assertNotNull(manager.getUsers());
        assertEquals(0, manager.getUsers().size());

        manager.addUser(new User("nicolas"));

        assertSetEquals("[User(nicolas)]", manager.getUsers());
    }


    public void test_roles() throws Exception {
        assertNotNull(manager.getRoles());
        assertEquals(0, manager.getRoles().size());

        manager.setRoles(Arrays.asList(new Role("guest"), new Role("admin")));

        assertEquals("[Role(guest), Role(admin)]", manager.getRoles().toString());
    }


    public void test_addRole() throws Exception {
        manager.addRole(new Role("guest"));

        assertEquals("[Role(guest)]", manager.getRoles().toString());
    }


    public void test_addRole_failure() throws Exception {
        manager.addRole(new Role("guest"));

        try {
            manager.addRole(new Role("guest"));
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals("Le rôle 'guest' existe déjà.", ex.getLocalizedMessage());
        }

        assertEquals("[Role(guest)]", manager.getRoles().toString());
    }


    public void test_setUserRoles() throws Exception {
        assertNotNull(manager.getUserRoles(new User("smith")));
        assertEquals(0, manager.getUserRoles(new User("smith")).size());

        manager.addRoleToUser(new Role("guest"), new User("smith"));
        manager.addRoleToUser(new Role("admin"), new User("smith"));

        List<Role> userRoles = manager.getUserRoles(new User("smith"));
        assertSame(userRoles, manager.getUserRoles(new User("smith")));

        assertEquals("[Role(guest), Role(admin)]", userRoles.toString());
    }


    public void test_addUserRoles() throws Exception {
        manager.addRoleToUser(new Role("agent-role"), new User("smith"));
        assertEquals("agent-role", manager.getUserRoles(new User("smith")).get(0).getName());

        manager.addRoleToUser(new Role("agent-role"), new User("smith"));

        List<Role> userRoles = manager.getUserRoles(new User("smith"));
        assertEquals(1, userRoles.size());
    }


    public void test_addUserRoles_useSameInstance() throws Exception {
        manager.addRoleToUser(new Role("bad-boy"), new User("smith"));
        assertEquals("bad-boy", manager.getRoles().get(0).getName());

        manager.addRoleToUser(new Role("bad-boy"), new User("sylar"));
        assertEquals(1, manager.getRoles().size());

        assertSame(manager.getUserRoles(new User("smith")).get(0),
                   manager.getUserRoles(new User("sylar")).get(0));
    }


    public void test_removeUserRoles() throws Exception {
        User smith = new User("smith");

        manager.removeRoleToUser(new Role("guest"), smith);

        manager.addRoleToUser(new Role("guest"), smith);
        manager.addRoleToUser(new Role("admin"), smith);

        manager.removeRoleToUser(new Role("guest"), smith);
        assertEquals(1, manager.getUserRoles(smith).size());

        manager.removeRoleToUser(new Role("admin"), smith);
        assertEquals(0, manager.getUserRoles(smith).size());

        manager.removeRoleToUser(new Role("unknown"), smith);
    }


    public void test_removeRole() throws Exception {
        manager.addRoleToUser(new Role("guest"), new User("smith"));
        manager.addRoleToUser(new Role("admin"), new User("smith"));

        manager.removeRole(new Role("guest"));

        assertEquals("[Role(admin)]", manager.getUserRoles(new User("smith")).toString());
        assertEquals("[Role(admin)]", manager.getRoles().toString());
    }


    public void test_removeRole_composite() throws Exception {
        Role guest = new Role("guest");

        RoleComposite admin = new RoleComposite("admin");
        admin.addRole(guest);

        manager.addRole(guest);
        manager.addRole(admin);

        assertEquals("[Role(guest), RoleComposite(admin)]", manager.getRoles().toString());

        manager.removeRole(new Role("guest"));

        assertEquals("[RoleComposite(admin)]", manager.getRoles().toString());
        assertEquals("[]", admin.getRoles().toString());
    }


    public void test_hasGrant() throws Exception {
        User neo = new User("neo");

        assertFalse(manager.hasGrants(neo));

        manager.addUser(neo);
        assertFalse(manager.hasGrants(neo));

        manager.addRoleToUser(new Role("gues"), neo);
        assertTrue(manager.hasGrants(neo));
    }


    public void test_findUser() throws Exception {
        User neo = new User("neo");

        assertNull(manager.findUser("neo"));

        manager.addUser(neo);

        assertSame(neo, manager.findUser("neo"));
    }


    public void test_visit() throws Exception {
        manager.addRoleToUser(new Role("agent"), new User("smith"));
        manager.visitUserRoles(new User("smith"), new RoleVisitorMock(log));
        log.assertContent("visit(agent)");
    }


    public void test_addRoleToComposite() throws Exception {
        manager.addRoleToComposite(new Role("guest"), new RoleComposite("compositeA"));
        manager.addRoleToComposite(new Role("admin"), new RoleComposite("compositeA"));

        assertEquals("[RoleComposite(compositeA), Role(guest), Role(admin)]", manager.getRoles().toString());
        assertCompositeRoles("[Role(guest), Role(admin)]", "compositeA");
    }


    public void test_removeRoleToComposite() throws Exception {
        manager.addRoleToComposite(new Role("guest"), new RoleComposite("compositeA"));
        assertCompositeRoles("[Role(guest)]", "compositeA");

        manager.removeRoleToComposite(new Role("admin"), new RoleComposite("compositeA"));
        assertCompositeRoles("[Role(guest)]", "compositeA");

        manager.removeRoleToComposite(new Role("guest"), new RoleComposite("compositeA"));
        assertCompositeRoles("[]", "compositeA");
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        manager = new DefaultModelManager();
    }


    private void assertCompositeRoles(String expected, String compositeName) {
        assertEquals(expected, ((RoleComposite)manager.findRole(compositeName)).getRoles().toString());
    }
}
