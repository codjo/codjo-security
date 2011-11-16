package net.codjo.security.client.plugin;
import net.codjo.security.common.login.LoginEvent;
import net.codjo.test.common.LogString;
import net.codjo.util.system.EventSynchronizer;
/**
 *
 */
class LoginEventSynchronizerMock extends EventSynchronizer<LoginEvent> {
    private final LogString log;


    LoginEventSynchronizerMock(LogString log) {
        this.log = log;
    }


    @Override
    public LoginEvent waitEvent() throws InterruptedException {
        log.call("waitEvent");
        return super.waitEvent();
    }


    @Override
    public void receivedEvent(LoginEvent event) {
        log.call("receivedEvent", event);
        super.receivedEvent(event);
    }
}
