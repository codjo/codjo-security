package net.codjo.security.server.plugin;
import net.codjo.security.server.api.SecurityContextFactory;
import net.codjo.security.server.api.SecurityService;
import net.codjo.security.server.api.UserFactory;
/**
 *
 */
public class SecurityServerPluginConfiguration {
    private Class<? extends SecurityService> securitServiceClass;
    private UserFactory userFactory;
    private SecurityContextFactory securityContextFactory;


    public Class<? extends SecurityService> getSecuritServiceClass() {
        return securitServiceClass;
    }


    public void setSecuritServiceClass(Class<? extends SecurityService> securitServiceClass) {
        this.securitServiceClass = securitServiceClass;
    }


    public UserFactory getUserFactory() {
        return userFactory;
    }


    public void setUserFactory(UserFactory userFactory) {
        this.userFactory = userFactory;
    }


    public SecurityContextFactory getSecurityContextFactory() {
        return securityContextFactory;
    }


    public void setSecurityContextFactory(SecurityContextFactory securityContextFactory) {
        this.securityContextFactory = securityContextFactory;
    }
}
