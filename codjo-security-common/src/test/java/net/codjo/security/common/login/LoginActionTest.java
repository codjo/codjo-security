package net.codjo.security.common.login;
import java.net.Inet4Address;
import java.net.InetAddress;
import net.codjo.security.common.SecurityLevel;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
/**
 *
 */
public class LoginActionTest {

    @Test
    public void test_constructor() throws Exception {
        LoginAction action = new LoginAction("login",
                                             "pwd",
                                             "myDomain",
                                             "version",
                                             "ip", SecurityLevel.USER);
        assertEquals("login", action.getLogin());
        assertEquals("pwd", action.getPassword());
        assertEquals("version", action.getVersion());
        assertEquals("myDomain", action.getLdap());
        assertEquals("ip", action.getIp());
        assertEquals(SecurityLevel.USER, action.getSecurityLevel());

        InetAddress localhostAddress = Inet4Address.getLocalHost();
        String hostname = localhostAddress.getHostName();
        assertEquals(hostname, action.getHostname());

        assertEquals("login/pwd/myDomain/version/ip/" + hostname + "/USER", action.toString());
    }


    @Test
    public void test_constructorDomainNull() throws Exception {
        LoginAction action = new LoginAction("login",
                                             "pwd",
                                             null,
                                             "version",
                                             "ip", SecurityLevel.SERVICE);
        assertEquals("login", action.getLogin());
        assertEquals("pwd", action.getPassword());
        assertEquals("version", action.getVersion());
        assertEquals("ip", action.getIp());
        assertNull(action.getLdap());

        InetAddress localhostAddress = Inet4Address.getLocalHost();
        String hostname = localhostAddress.getHostName();
        assertEquals(hostname, action.getHostname());

        assertEquals("login/pwd/version/ip/" + hostname + "/SERVICE", action.toString());
    }
}
