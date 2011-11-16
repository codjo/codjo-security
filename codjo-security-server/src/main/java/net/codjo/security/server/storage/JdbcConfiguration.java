package net.codjo.security.server.storage;
/**
 *
 */
public class JdbcConfiguration {
    private String applicationLogin;
    private String applicationPassword;


    JdbcConfiguration(String login, String password) {
        this.applicationLogin = login;
        this.applicationPassword = password;
    }


    public String getApplicationLogin() {
        return applicationLogin;
    }


    public String getApplicationPassword() {
        return applicationPassword;
    }
}
