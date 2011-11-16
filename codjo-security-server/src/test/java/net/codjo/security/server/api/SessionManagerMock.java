package net.codjo.security.server.api;
import net.codjo.plugin.common.session.SessionListener;
import net.codjo.plugin.common.session.SessionManager;
import net.codjo.test.common.LogString;
/**
 *
 */
public class SessionManagerMock extends SessionManager {
    private final LogString log;


    public SessionManagerMock(LogString log) {
        this.log = log;
    }


    @Override
    public void addListener(SessionListener sessionListener) {
        log.call("addListener", sessionListener.getClass().getSimpleName());
    }
}
