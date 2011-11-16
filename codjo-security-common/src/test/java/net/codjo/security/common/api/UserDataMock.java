package net.codjo.security.common.api;
import java.util.Date;
/**
 *
 */
public class UserDataMock implements UserData {
    private String name;
    private Date lastLogin;
    private Date lastLogout;


    public UserDataMock(String name) {
        this.name = name;
    }


    public UserDataMock(String name, Date lastLogin) {
        this.name = name;
        this.lastLogin = lastLogin;
    }


    public UserDataMock(String name, Date lastLogin, Date lastLogout) {
        this.name = name;
        this.lastLogin = lastLogin;
        this.lastLogout = lastLogout;
    }


    public String getName() {
        return name;
    }


    public Date getLastLogin() {
        return lastLogin;
    }


    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }


    public Date getLastLogout() {
        return lastLogout;
    }


    public void setLastLogout(Date lastLogout) {
        this.lastLogout = lastLogout;
    }
}
