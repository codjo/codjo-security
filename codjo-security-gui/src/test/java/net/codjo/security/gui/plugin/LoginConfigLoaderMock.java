package net.codjo.security.gui.plugin;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import net.codjo.security.gui.login.LoginConfig;
/**
 *
 */
public class LoginConfigLoaderMock extends LoginConfigLoader {
    private LoginConfig loginConfig;


    public LoginConfigLoaderMock() {
        super(new DefaultSecurityGuiConfiguration());
    }


    @Override
    public LoginConfig load(String confFile) throws IOException {
        return loginConfig;
    }


    @Override
    public LoginConfig load(URL confFile) throws IOException {
        return loginConfig;
    }


    @Override
    public LoginConfig load(Properties properties) {
        return loginConfig;
    }


    public void mockLoad(LoginConfig aLoginConfig) {
        this.loginConfig = aLoginConfig;
    }
}
