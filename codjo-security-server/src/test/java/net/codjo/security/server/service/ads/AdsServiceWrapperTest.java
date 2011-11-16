package net.codjo.security.server.service.ads;
import net.codjo.ads.AdsException;
import net.codjo.ads.AdsServiceMock;
import net.codjo.agent.UserId;
import net.codjo.security.common.SecurityLevel;
import net.codjo.test.common.LogString;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
/**
 *
 */
public class AdsServiceWrapperTest {
    private LogString log = new LogString();
    private AdsServiceWrapper adsServiceWrapper;
    private AdsServiceMock adsServiceMock = new AdsServiceMock(log);


    @Before
    public void setUp() {
        adsServiceWrapper = new AdsServiceWrapper(adsServiceMock);
        adsServiceMock.mockRoles("myLogin", "role1", "role2");
    }


    @Test
    public void test_login() throws Exception {
        adsServiceWrapper.login("myLogin", "myPassword", SecurityLevel.USER);

        log.assertContent("login(myLogin, myPassword, 1)");
    }


    @Test(expected = AdsException.class)
    public void test_login_failure() throws Exception {
        adsServiceMock.mockLoginFailureException("Login failure !!!");

        adsServiceWrapper.login("myLogin", "myPassword", SecurityLevel.USER);
    }


    @Test
    public void test_disconnectAll() throws Exception {
        adsServiceWrapper.disconnectAll();

        log.assertContent("disconnectAll()");
    }


    @Test
    public void test_rolesFor() throws Exception {
        UserId userId = adsServiceWrapper.login("myLogin", "myPassword", SecurityLevel.USER);

        String[] roles = adsServiceWrapper.rolesFor(userId);

        log.assertContent("login(myLogin, myPassword, 1)", "rolesFor(adsUserId.myLogin)");
        assertArrayEquals(new String[]{"role1", "role2"}, roles);
    }


    @Test
    public void test_rolesFor_notLogged() throws Exception {
        UserId userId = UserId.createId("myLogin", "myPassword");
        try {
            adsServiceWrapper.rolesFor(userId);
        }
        catch (Exception e) {
            assertEquals(String.format("Unknown userId: %s", userId.getLogin()), e.getMessage());
        }
    }


    @Test
    public void test_rolesFor_sessionClosed() throws Exception {
        UserId userId = adsServiceWrapper.login("myLogin", "myPassword", SecurityLevel.USER);
        adsServiceWrapper.handleSessionStop(userId);

        try {
            adsServiceWrapper.rolesFor(userId);
        }
        catch (Exception e) {
            assertEquals(String.format("Unknown userId: %s", userId.getLogin()), e.getMessage());
        }
    }
}
