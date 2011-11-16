package net.codjo.security.gui.plugin;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import net.codjo.security.gui.login.LoginConfig;
import net.codjo.security.gui.login.LoginConfig.Ldap;
import net.codjo.security.gui.login.LoginConfig.Server;
/**
 *
 */
class LoginConfigLoader {
    private final SecurityGuiConfiguration securityGuiConfiguration;


    LoginConfigLoader(SecurityGuiConfiguration securityGuiConfiguration) {
        this.securityGuiConfiguration = securityGuiConfiguration;
    }


    public LoginConfig load(String confFile) throws IOException {
        return load(getClass().getResource(confFile));
    }


    public LoginConfig load(URL confFile) throws IOException {
        InputStream inputStream = confFile.openStream();
        try {
            Properties properties = new Properties();
            properties.load(inputStream);
            return load(properties);
        }
        finally {
            inputStream.close();
        }
    }


    public LoginConfig load(Properties properties) {
        Properties data = new Properties();
        data.putAll(properties);
        data.putAll(System.getProperties());

        LoginConfig loginConfig = new LoginConfig();
        loginConfig.setDefaultLogin(data.getProperty("login.default.name"));
        loginConfig.setDefaultPassword(data.getProperty("login.default.pwd"));
        loginConfig.setApplicationName(data.getProperty("application.name"));
        loginConfig.setApplicationVersion(data.getProperty("application.version"));
        loginConfig.setApplicationIcon(data.getProperty("application.icon"));
        loginConfig.setApplicationSplashImage(securityGuiConfiguration.getSplashImageUrl());

        loadServers(loginConfig, data);
        loadLdap(loginConfig, data);

        return loginConfig;
    }


    private void loadServers(LoginConfig loginConfig, Properties data) {
        String defaultServerData = data.getProperty(data.getProperty("server.default.url", "NONE"));
        if (defaultServerData != null) {
            loginConfig.addServer(buildServer(defaultServerData));
        }
        else {
            Set<Server> serverSet = new TreeSet<Server>();
            for (Enumeration iter = data.keys(); iter.hasMoreElements(); ) {
                String key = (String)iter.nextElement();
                if (key.startsWith("server.url.")) {
                    serverSet.add(buildServer(data.getProperty(key)));
                }
            }
            for (Server server : serverSet) {
                loginConfig.addServer(server);
            }
        }
    }


    private Server buildServer(String serverData) {
        StringTokenizer tokenizer = new StringTokenizer(serverData, ",");

        if (tokenizer.countTokens() != 2) {
            throw new IllegalArgumentException();
        }
        return new Server(tokenizer.nextToken().trim(), tokenizer.nextToken().trim());
    }


    private void loadLdap(LoginConfig loginConfig, Properties data) {
        Set<Ldap> ldapSet = new TreeSet<Ldap>();
        for (Enumeration iter = data.keys(); iter.hasMoreElements(); ) {
            String key = (String)iter.nextElement();
            if (key.startsWith("server.ldap.")) {
                String[] keySplitted = key.split("\\.");
                ldapSet.add(new Ldap(keySplitted[keySplitted.length - 1], data.getProperty(key)));
            }
        }
        for (Ldap ldap : ldapSet) {
            loginConfig.addLdap(ldap);
        }
    }
}
