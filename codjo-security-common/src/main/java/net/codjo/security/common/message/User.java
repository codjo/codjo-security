package net.codjo.security.common.message;
import java.util.Date;
import net.codjo.security.common.api.UserData;
/**
 *
 */
public class User implements Comparable<User>, UserData {
    private String name;
    private Date lastLogin;
    private Date lastLogout;


    public User(String name) {
        this.name = name;
    }


    public User(String name, Date lastLogin) {
        this.name = name;
        this.lastLogin = lastLogin;
    }


    public User(String name, Date lastLogin, Date lastLogout) {
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


    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        User user = (User)object;
        return !(name != null ? !name.equalsIgnoreCase(user.name) : user.name != null);
    }


    @Override
    public int hashCode() {
        return (name != null ? name.toLowerCase().hashCode() : 0);
    }


    @Override
    public String toString() {
        return "User(" + name + ")";
    }


    public int compareTo(User user) {
        return name.compareTo(user.name);
    }
}
