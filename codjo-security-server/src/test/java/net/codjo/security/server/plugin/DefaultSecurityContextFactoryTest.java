package net.codjo.security.server.plugin;
import junit.framework.TestCase;
import net.codjo.agent.UserId;
import net.codjo.security.common.api.SecurityContext;
import net.codjo.security.common.message.DefaultModelManager;
import net.codjo.security.common.message.ModelManager;
import net.codjo.security.common.message.Role;
import net.codjo.security.common.message.RoleComposite;
import net.codjo.security.common.message.User;
import net.codjo.security.server.storage.StorageMock;
/**
 *
 */
public class DefaultSecurityContextFactoryTest extends TestCase {
    public void test_noComposite() throws Exception {
        ModelManager manager = new DefaultModelManager();
        manager.addUser(new User("smith"));
        manager.addRoleToUser(new Role("guest"), new User("smith"));

        DefaultSecurityContextFactory contextFactory =
              new DefaultSecurityContextFactory(new ServerModelManagerImplMock(manager));

        SecurityContext securityContext =
              contextFactory.createSecurityContext(UserId.createId("smith", "secret"), new StorageMock());

        assertTrue(securityContext.isCallerInRole("guest"));
        assertFalse(securityContext.isCallerInRole("noMyRole"));
    }


    public void test_withComposite() throws Exception {
        ModelManager manager = new DefaultModelManager();
        manager.addUser(new User("smith"));
        manager.addRoleToComposite(new Role("guest"), new RoleComposite("composed"));
        manager.addRoleToUser(new RoleComposite("composed"), new User("smith"));

        DefaultSecurityContextFactory contextFactory =
              new DefaultSecurityContextFactory(new ServerModelManagerImplMock(manager));

        SecurityContext securityContext =
              contextFactory.createSecurityContext(UserId.createId("smith", "secret"), new StorageMock());

        assertTrue(securityContext.isCallerInRole("guest"));
        assertTrue(securityContext.isCallerInRole("composed"));
        assertFalse(securityContext.isCallerInRole("noMyRole"));
    }
}
