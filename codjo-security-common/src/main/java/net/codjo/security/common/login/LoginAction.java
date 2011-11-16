package net.codjo.security.common.login;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import net.codjo.security.common.SecurityLevel;
/**
 *
 */
public class LoginAction implements Serializable {
    private final String login;
    private final String password;
    private final String ldap;
    private final String version;
    private final String ip;
    private final SecurityLevel securityLevel;
    private final String hostname;


    public LoginAction(String login,
                       String password,
                       String optionalLdap,
                       String version,
                       String ip,
                       SecurityLevel securityLevel) {
        this.login = login;
        this.password = password;
        this.version = version;
        this.ldap = optionalLdap;
        this.ip = ip;
        if (securityLevel != null) {
            this.securityLevel = securityLevel;
        }
        else {
            throw new IllegalArgumentException("Security level can't be null");
        }

        try {
            this.hostname = InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    public String getLogin() {
        return login;
    }


    public String getPassword() {
        return password;
    }


    public String getLdap() {
        return ldap;
    }


    public String getVersion() {
        return version;
    }


    public String getIp() {
        return ip;
    }


    public String getHostname() {
        return hostname;
    }


    public SecurityLevel getSecurityLevel() {
        return securityLevel;
    }


    @Override
    public String toString() {

        return new StringBuilder()
              .append(getLogin())
              .append("/").append(getPassword())
              .append(ldap != null ? "/" + getLdap() : "")
              .append("/").append(getVersion())
              .append("/").append(getIp())
              .append("/").append(getHostname())
              .append("/").append(getSecurityLevel()
              ).toString();
    }
}
