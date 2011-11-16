package net.codjo.security.common.api;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import net.codjo.agent.UserId;
/**
 *
 */
public class DefaultUserTest extends TestCase {
    private DefaultUser xmlUser = null;


    public void test_isAllowedTo() throws Exception {
        assertEquals(true, xmlUser.isAllowedTo("selectFundPrice"));
        assertEquals(true, xmlUser.isAllowedTo("selectBench"));
        assertEquals(false, xmlUser.isAllowedTo("selectAllFundPrice"));
        assertEquals(false, xmlUser.isAllowedTo("updateFundPrice"));
    }


    public void test_isInRole() throws Exception {
        xmlUser = new DefaultUser(null, new ArrayList<Role>(), new SecurityContextMock("consultation"));

        assertTrue(xmlUser.isInRole("consultation"));
        assertFalse(xmlUser.isInRole("administration"));
    }


    public void test_userId() throws Exception {
        UserId userId = UserId.createId("john", "secret");
        xmlUser = new DefaultUser(userId, new ArrayList<Role>(), new SecurityContextMock());

        assertSame(userId, xmlUser.getId());
    }


    @Override
    protected void setUp() throws Exception {
        List<Role> roles = new ArrayList<Role>();

        List<Pattern> includes = new ArrayList<Pattern>();
        List<Pattern> excludes = new ArrayList<Pattern>();
        includes.add(new Pattern("select*"));
        excludes.add(new Pattern("selectAllFundPrice"));
        roles.add(new Role("consultation", includes, excludes));

        List<Pattern> includes2 = new ArrayList<Pattern>();
        List<Pattern> excludes2 = new ArrayList<Pattern>();
        includes2.add(new Pattern("*FundPrice*"));
        includes2.add(new Pattern("PortfolioCodification"));
        excludes2.add(new Pattern("selectAllFundPrice"));
        roles.add(new Role("administration_vl", includes2, excludes2));

        xmlUser = new DefaultUser(null, roles, new SecurityContextMock("consultation"));
    }
}

