package net.codjo.security.common.login;
import java.io.Serializable;
import net.codjo.agent.UserId;
import net.codjo.security.common.api.User;
/**
 *
 */
public class LoginEvent implements Serializable {
    private final User user;
    private final UserId userId;
    private final Throwable loginFailureException;


    public LoginEvent(Throwable loginFailureException) {
        this.user = null;
        this.userId = null;
        this.loginFailureException = loginFailureException;
    }


    public LoginEvent(User user, UserId userId) {
        this.user = user;
        this.userId = userId;
        this.loginFailureException = null;
    }


    public User getUser() {
        return user;
    }


    public UserId getUserId() {
        return userId;
    }


    public Throwable getLoginFailureException() {
        return loginFailureException;
    }


    public boolean hasFailed() {
        return loginFailureException != null;
    }


    @Override
    public String toString() {
        if (hasFailed()) {
            return String.format("LoginEvent[loginFailureException='%s']",
                                 loginFailureException.getMessage());
        }
        return String.format("LoginEvent[userId='%s']", userId.encode());
    }
}
