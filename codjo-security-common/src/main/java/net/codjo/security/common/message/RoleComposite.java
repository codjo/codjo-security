package net.codjo.security.common.message;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 *
 */
public class RoleComposite extends Role {
    private List<Role> roles = new ArrayList<Role>();


    public RoleComposite() {
    }


    public RoleComposite(String name) {
        super(name);
    }


    void addRole(Role role) {
        roles.add(role);
    }


    void removeRole(Role role) {
        roles.remove(role);
    }


    public List<Role> getRoles() {
        return Collections.unmodifiableList(roles);
    }


    @Override
    public void accept(RoleVisitor visitor) {
        visitor.visitComposite(this);
    }


    public void acceptForSubRoles(RoleVisitor roleVisitor) {
        for (Role role : roles) {
            role.accept(roleVisitor);
        }
    }


    @Override
    public String toString() {
        return "RoleComposite(" + getName() + ")";
    }
}
