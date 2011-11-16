package net.codjo.security.server.api;
import junit.framework.TestCase;
import net.codjo.agent.Agent;
import net.codjo.agent.ServiceHelper;
/**
 *
 */
public class SecurityServiceTest extends TestCase {
    public void test_getName() throws Exception {
        SecurityService service =
              new SecurityService() {
                  public ServiceHelper getServiceHelper(Agent agent) {
                      return null;
                  }
              };
        assertEquals(SecurityServiceHelper.NAME, service.getName());
    }
}
