package net.codjo.security.client.plugin;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Agent;
import net.codjo.agent.Aid;
import net.codjo.agent.Behaviour;
import net.codjo.agent.MessageTemplate;
import net.codjo.agent.MessageTemplate.MatchExpression;
import net.codjo.agent.behaviour.CyclicBehaviour;
import net.codjo.security.common.SecurityLevel;
import net.codjo.security.common.login.LoginAction;
import net.codjo.security.common.login.LoginEvent;
import net.codjo.security.common.login.LoginProtocol;
import net.codjo.util.system.EventSynchronizer;
import org.apache.log4j.Logger;

import static net.codjo.agent.AclMessage.Performative;
import static net.codjo.agent.MessageTemplate.and;
import static net.codjo.agent.MessageTemplate.matchLanguage;
import static net.codjo.agent.MessageTemplate.matchProtocol;
import static net.codjo.agent.MessageTemplate.matchWith;
/**
 *
 */
class LoginAgent extends Agent {
    private static final Aid LOGIN_SERVER_AID = new Aid(LoginProtocol.SERVER_LOGIN_AGENT);
    private static final MessageTemplate RESPONSE_TEMPLATE = and(matchProtocol(LoginProtocol.ID),
                                                                 matchLanguage(AclMessage.OBJECT_LANGUAGE));
    private final LoginAction loginAction;
    private final EventSynchronizer<LoginEvent> synchronizer;
    private LoginAgentListener loginAgentListener;


    LoginAgent(String login,
               String password,
               String optionalLdap,
               String version,
               String ip,
               SecurityLevel securityLevel,
               EventSynchronizer<LoginEvent> synchronizer) {
        this.synchronizer = synchronizer;
        loginAction = new LoginAction(login, password, optionalLdap, version, ip, securityLevel);
    }


    public void setLoginAgentListener(LoginAgentListener loginAgentListener) {
        this.loginAgentListener = loginAgentListener;
    }


    @Override
    protected void setup() {
        addBehaviour(new LoginBehaviour());
        addBehaviour(new LoggingUnexpectedMessageBehaviour());
    }


    @Override
    protected void tearDown() {
        if (loginAgentListener != null) {
            loginAgentListener.stopped();
            loginAgentListener = null;
        }
    }


    private class LoginBehaviour extends Behaviour {
        private int step = 1;
        private boolean responseReceived = false;


        @Override
        protected void action() {
            if (step == 1) {
                sendRequest();
            }
            else {
                waitResponse();
            }
        }


        private void sendRequest() {
            AclMessage aclMessage = new AclMessage(Performative.REQUEST);
            aclMessage.setLanguage(AclMessage.OBJECT_LANGUAGE);
            aclMessage.setProtocol(LoginProtocol.ID);
            aclMessage.setContentObject(loginAction);
            aclMessage.addReceiver(LOGIN_SERVER_AID);

            send(aclMessage);
            step = 2;
            block();
        }


        private void waitResponse() {
            AclMessage aclMessage = getAgent().receive(RESPONSE_TEMPLATE);
            if (aclMessage == null) {
                block();
                return;
            }

            LoginEvent loginEvent = (LoginEvent)aclMessage.getContentObject();
            if (loginEvent.hasFailed()) {
                die();
            }
            responseReceived = true;

            synchronizer.receivedEvent(loginEvent);
        }


        @Override
        public boolean done() {
            return responseReceived;
        }
    }
    private class LoggingUnexpectedMessageBehaviour extends CyclicBehaviour {
        private final Logger logger = Logger.getLogger(LoginAgent.class);


        @Override
        protected void action() {
            AclMessage aclMessage = getAgent().receive(matchWith(new MatchExpression() {
                public boolean match(AclMessage aclMessage) {
                    return !RESPONSE_TEMPLATE.match(aclMessage);
                }
            }));
            if (aclMessage == null) {
                block();
                return;
            }
            logger.warn("unexpected message received - " + aclMessage.toFipaACLString());
        }
    }
}
