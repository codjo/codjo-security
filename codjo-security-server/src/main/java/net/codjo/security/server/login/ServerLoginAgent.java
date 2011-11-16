package net.codjo.security.server.login;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Agent;
import net.codjo.agent.Behaviour;
import net.codjo.agent.Behaviour.UncaughtErrorHandler;
import net.codjo.plugin.common.session.SessionManager;
import net.codjo.security.common.login.LoginEvent;
import org.apache.log4j.Logger;

import static net.codjo.agent.Behaviour.thatNeverFails;
/**
 *
 */
public class ServerLoginAgent extends Agent {
    private static final Logger LOG = Logger.getLogger(ServerLoginAgent.class);
    private final SessionManager sessionManager;
    private final String version;


    public ServerLoginAgent(SessionManager sessionManager, String applicationVersion) {
        this.sessionManager = sessionManager;
        this.version = applicationVersion;
    }


    @Override
    protected void setup() {
        SessionLifecycleBehaviour lifecycleBehaviour = new SessionLifecycleBehaviour(sessionManager);

        addBehaviour(thatNeverFails(lifecycleBehaviour,
                                    new LifeCycleErrorHandler()));
        addBehaviour(thatNeverFails(new AuthenticationBehaviour(version, lifecycleBehaviour),
                                    new AuthenticationUncaughtErrorHandler()));
    }


    private static class AuthenticationUncaughtErrorHandler implements UncaughtErrorHandler {
        public void handle(Throwable error, Behaviour behaviour) {
            AuthenticationBehaviour authenticationBehaviour = (AuthenticationBehaviour)behaviour;

            AclMessage message = authenticationBehaviour.getLastAuthenticationMessage();

            LOG.error("Authentication failure detected "
                      + "- ServerLoginAgent has been protected and will stay alive "
                      + "- this error has been produced by :\n" + message.toFipaACLString());

            authenticationBehaviour.sendReplyMessage(message, new LoginEvent(error));
        }
    }
    private static class LifeCycleErrorHandler implements UncaughtErrorHandler {
        public void handle(Throwable error, Behaviour behaviour) {
            LOG.error("Lifecycle failure detected "
                      + "- ServerLoginAgent has been protected and will stay alive "
                      + "- this error has been produced by a message from the AMS");
        }
    }
}
