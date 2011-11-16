package net.codjo.security.common.message;
/**
 *
 */
public interface RoleVisitor {
    void visit(Role role);


    void visitComposite(RoleComposite role);
}
