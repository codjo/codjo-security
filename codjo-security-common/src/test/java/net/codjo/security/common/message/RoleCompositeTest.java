package net.codjo.security.common.message;
import junit.framework.TestCase;
import net.codjo.test.common.LogString;
/**
 *
 */
public class RoleCompositeTest extends TestCase {
    private LogString log = new LogString();
    private final RoleComposite composite = new RoleComposite("composed-role");


    public void test_subRoles() throws Exception {
        composite.addRole(new Role("smith"));
        assertEquals("smith", composite.getRoles().get(0).getName());
    }


    public void test_removeRole() throws Exception {
        composite.addRole(new Role("smith"));
        composite.removeRole(new Role("smith"));
        assertEquals("[]", composite.getRoles().toString());
    }


    public void test_visitor() throws Exception {
        composite.addRole(new Role("admin"));
        composite.accept(new RoleVisitorMock(log));

        log.assertContent("visitComposite(composed-role), visit(admin)");
    }
}
