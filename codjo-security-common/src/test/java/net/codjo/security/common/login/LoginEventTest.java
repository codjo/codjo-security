package net.codjo.security.common.login;
import net.codjo.agent.UserId;
import net.codjo.security.common.api.User;
import net.codjo.security.common.api.UserMock;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
/**
 *
 */
public class LoginEventTest {

    @Test
    public void test_constructor_loginOK() throws Exception {
        User user = new UserMock();
        UserId userId = UserId.createId("l", "p");

        LoginEvent event = new LoginEvent(user, userId);

        assertSame(user, event.getUser());
        assertSame(userId, event.getUserId());
        assertEquals(String.format("LoginEvent[userId='%s']", userId.encode()), event.toString());
    }


    @Test
    public void test_constructor_loginFailed() throws Exception {
        Throwable throwable = new Throwable("error message");

        LoginEvent event = new LoginEvent(throwable);

        assertTrue(event.hasFailed());
        assertSame(throwable, event.getLoginFailureException());
        assertEquals("LoginEvent[loginFailureException='error message']", event.toString());
    }
}
