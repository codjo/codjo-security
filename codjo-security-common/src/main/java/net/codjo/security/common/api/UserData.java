package net.codjo.security.common.api;
import java.util.Date;
/**
 *
 */
public interface UserData {
    public String getName();


    public Date getLastLogin();


    public Date getLastLogout();
}
