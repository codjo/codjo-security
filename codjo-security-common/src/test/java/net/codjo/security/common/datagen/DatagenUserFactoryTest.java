package net.codjo.security.common.datagen;
import java.util.List;
import junit.framework.TestCase;
import net.codjo.agent.UserId;
import net.codjo.security.common.api.SecurityContextMock;
import net.codjo.security.common.api.User;
/**
 *
 */
public class DatagenUserFactoryTest extends TestCase {
    private static final String ROLE_XML = "RoleReaderTest.xml";
    private UserId userId = UserId.createId("gonnot", "secret");


    public void test_read() throws Exception {
        assertConsultationRole(new DatagenUserFactory(ROLE_XML));
    }


    public void test_read_fromStream() throws Exception {
        DatagenUserFactory userFactory = new DatagenUserFactory(getClass().getResourceAsStream(ROLE_XML));

        assertConsultationRole(userFactory);
    }


    public void test_emptyConstructor() throws Exception {
        User userSive =
              new DatagenUserFactory().getUser(userId, new SecurityContextMock("administration_vl"));
        assertTrue(userSive.isAllowedTo("selectFundPrice"));
        assertTrue(userSive.isAllowedTo("PortFolioCodificationCoucou"));
        assertTrue(userSive.isAllowedTo("updateTruc"));
        assertTrue(userSive.isAllowedTo("selectAllFundPrice"));
    }


    public void test_read_adminVl() throws Exception {
        User user =
              new DatagenUserFactory(ROLE_XML).getUser(userId, new SecurityContextMock("administration_vl"));

        assertTrue(user.isAllowedTo("selectFundPrice"));
        assertTrue(user.isAllowedTo("PortFolioCodificationCoucou"));
        assertFalse(user.isAllowedTo("updateTruc"));
        assertFalse(user.isAllowedTo("selectAllFundPrice"));
    }


    public void test_roles() throws Exception {
        DatagenUserFactory userHome = new DatagenUserFactory(ROLE_XML);

        List rolesName = userHome.getAllRoleNames();

        assertTrue("contient administration_vl", rolesName.contains("administration_vl"));
        assertTrue("contient consultation", rolesName.contains("consultation"));
        assertEquals(2, rolesName.size());
    }


    public void test_noSecurityProfile() throws Exception {
        DatagenUserFactory userHome = new DatagenUserFactory("/conf/noRole.xml");
        User user = userHome.getUser(userId, new SecurityContextMock("consultation"));

        assertNotNull(user);
        assertTrue("Toutes les fonctions sont autorisées", user.isAllowedTo("truc"));
        assertTrue(user.isInRole("bidule"));
        assertSame(userId, user.getId());
        assertEquals("0 role defini", 0, userHome.getAllRoleNames().size());
    }


    private void assertConsultationRole(DatagenUserFactory userFactory) {
        User user = userFactory.getUser(userId, new SecurityContextMock("consultation"));

        assertTrue(user.isAllowedTo("selectTruc"));
        assertFalse(user.isAllowedTo("updateTruc"));
        assertFalse(user.isAllowedTo("PortFolioCodificationDoStuff"));
    }
}
