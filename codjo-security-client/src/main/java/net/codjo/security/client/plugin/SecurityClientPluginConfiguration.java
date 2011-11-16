package net.codjo.security.client.plugin;
/**
 *
 */
public class SecurityClientPluginConfiguration {
    public static final String LOGIN_PARAMETER = "login";
    public static final String PASSWORD_PARAMETER = "password";
    public static final String LDAP_PARAMETER = "ldap";

    private Class mainClass;


    public void setMainClass(Class mainClass) {
        this.mainClass = mainClass;
    }


    public Class getMainClass() {
        return mainClass;
    }
}
