package net.codjo.security.gui.login;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 *
 */
public class LoginConfig {
    private String applicationName;
    private String applicationVersion;
    private String applicationIcon;
    private String applicationSplashImage;
    private String defaultLogin;
    private String defaultPassword;
    private final List<Server> serverList = new ArrayList<Server>();
    private final List<Ldap> ldapList = new ArrayList<Ldap>();


    public String getApplicationName() {
        return applicationName;
    }


    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }


    public String getApplicationVersion() {
        return applicationVersion;
    }


    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }


    public String getApplicationIcon() {
        return applicationIcon;
    }


    public void setApplicationIcon(String applicationIcon) {
        this.applicationIcon = applicationIcon;
    }


    public String getApplicationSplashImage() {
        return applicationSplashImage;
    }


    public void setApplicationSplashImage(String applicationSplashImage) {
        this.applicationSplashImage = applicationSplashImage;
    }


    public String getDefaultLogin() {
        return defaultLogin;
    }


    public void setDefaultLogin(String defaultLogin) {
        this.defaultLogin = defaultLogin;
    }


    public String getDefaultPassword() {
        return defaultPassword;
    }


    public void setDefaultPassword(String defaultPassword) {
        this.defaultPassword = defaultPassword;
    }


    public List<Server> getServerList() {
        return Collections.unmodifiableList(serverList);
    }


    public boolean addServer(Server server) {
        return serverList.add(server);
    }


    public List<Ldap> getLdapList() {
        return Collections.unmodifiableList(ldapList);
    }


    public boolean addLdap(Ldap ldap) {
        return ldapList.add(ldap);
    }


    public static class Server extends SingleProperty {
        public Server(String key, String value) {
            super(key, value);
        }


        public String getName() {
            return getKey();
        }


        public String getUrl() {
            return getValue();
        }


        @Override
        public String toString() {
            return getName();
        }
    }

    public static class Ldap extends SingleProperty {
        public Ldap(String key, String value) {
            super(key, value);
        }


        public String getName() {
            return getKey();
        }


        public String getLabel() {
            return getValue();
        }


        @Override
        public String toString() {
            return getLabel();
        }
    }

    private static class SingleProperty implements Comparable<SingleProperty> {
        private final String key;
        private final String value;


        private SingleProperty(String key, String value) {
            if (key == null || value == null) {
                throw new IllegalArgumentException("key == null || value == null");
            }
            this.key = key;
            this.value = value;
        }


        protected String getKey() {
            return key;
        }


        protected String getValue() {
            return value;
        }


        public int compareTo(SingleProperty singleProperty) {
            return key.compareTo(singleProperty.key);
        }
    }
}
