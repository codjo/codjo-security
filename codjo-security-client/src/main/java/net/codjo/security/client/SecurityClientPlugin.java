package net.codjo.security.client;
import net.codjo.plugin.common.ApplicationCore;
import net.codjo.plugin.common.session.SessionManager;
/**
 *
 */
@Deprecated
public class SecurityClientPlugin extends net.codjo.security.client.plugin.SecurityClientPlugin {

    public SecurityClientPlugin(ApplicationCore applicationCore, SessionManager sessionManager) {
        super(applicationCore, sessionManager);
    }
}
