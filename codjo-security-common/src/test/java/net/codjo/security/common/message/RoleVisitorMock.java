package net.codjo.security.common.message;
import net.codjo.test.common.LogString;
/**
 *
 */
public class RoleVisitorMock implements RoleVisitor {
    private LogString log;


    public RoleVisitorMock(LogString log) {
        this.log = log;
    }


    public void visit(Role role) {
        log.call("visit", role.getName());
    }


    public void visitComposite(RoleComposite role) {
        log.call("visitComposite", role.getName());
        role.acceptForSubRoles(this);
    }
}