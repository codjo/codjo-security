package net.codjo.security.common.message;
import java.util.List;
/**
 *
 */
public abstract class RoleVisitorAdapter implements RoleVisitor {
    public void visit(Role role) {
    }


    public void visitComposite(RoleComposite role) {
    }


    public static void visitAll(List<Role> roles, RoleVisitorAdapter visitor) {
        for (Role current : roles) {
            current.accept(visitor);
        }
    }
}
