package net.codjo.security.server.plugin;
import java.util.SortedSet;
import java.util.TreeSet;
import net.codjo.agent.UserId;
import net.codjo.security.common.api.DefaultSecurityContext;
import net.codjo.security.common.api.SecurityContext;
import net.codjo.security.common.message.Role;
import net.codjo.security.common.message.RoleComposite;
import net.codjo.security.common.message.RoleVisitor;
import net.codjo.security.server.api.SecurityContextFactory;
import net.codjo.security.server.api.Storage;
/**
 *
 */
class DefaultSecurityContextFactory implements SecurityContextFactory {
    private final ServerModelManagerImpl manager;


    DefaultSecurityContextFactory(ServerModelManagerImpl manager) {
        this.manager = manager;
    }


    public SecurityContext createSecurityContext(UserId id, Storage storage) throws Exception {
        final SortedSet<String> set = new TreeSet<String>();
        manager.visit(storage, id, new RoleVisitor() {
            public void visit(Role role) {
                set.add(role.getName());
            }


            public void visitComposite(RoleComposite role) {
                set.add(role.getName());
                role.acceptForSubRoles(this);
            }
        });
        return new DefaultSecurityContext(set);
    }
}
