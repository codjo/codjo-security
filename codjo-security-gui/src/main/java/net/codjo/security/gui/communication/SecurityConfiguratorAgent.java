package net.codjo.security.gui.communication;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Agent;
import net.codjo.agent.Aid;
import net.codjo.agent.Behaviour;
import net.codjo.agent.DFService;
import net.codjo.agent.UserId;
import net.codjo.agent.protocol.InitiatorHandler;
import net.codjo.agent.protocol.RequestInitiator;
import net.codjo.agent.protocol.RequestProtocol;
import net.codjo.security.common.Constants;
import net.codjo.security.common.message.MessageBody;
import net.codjo.security.common.message.ModelManager;
import net.codjo.security.common.message.SecurityEngineConfiguration;
import net.codjo.security.common.message.XmlCodec;
/**
 * Agent permettant de configurer la couche de sécurité
 */
class SecurityConfiguratorAgent extends Agent {
    private final Handler handler;
    private final UserId userId;


    SecurityConfiguratorAgent(Handler securityModelHandler, UserId userId) {
        this.handler = securityModelHandler;
        this.userId = userId;
    }


    @Override
    protected void setup() {
        setEnabledO2ACommunication(true, 10);

        Aid securityManagerAid = findSecurityManager();
        if (securityManagerAid == null) {
            return;
        }

        AclMessage querySecurityModel = new AclMessage(AclMessage.Performative.QUERY);
        querySecurityModel.setProtocol(RequestProtocol.QUERY);
        querySecurityModel.setContent("securityModel");
        querySecurityModel.addReceiver(securityManagerAid);
        querySecurityModel.encodeUserId(userId);

        addBehaviour(new RequestInitiator(this, new QueryModelInitiator(), querySecurityModel));
        addBehaviour(new SaveListenerBehavior());
    }


    @Override
    protected void tearDown() {
    }


    private Aid findSecurityManager() {
        try {
            DFService.AgentDescription[] descriptions =
                  DFService.searchForService(this, Constants.SECURITY_MANAGER_SERVICE);

            if (descriptions.length == 0) {
                handler.handleCommunicationError("Impossible de trouver l'agent"
                                                 + " gérant la sécurité (absent de la plateforme)");
                return null;
            }
            return descriptions[0].getAID();
        }
        catch (DFService.DFServiceException e) {
            handler.handleCommunicationError("Impossible de trouver l'agent gérant la sécurité "
                                             + e.getMessage());
            return null;
        }
    }


    public static interface Handler {

        void handleReceiveSecurityModel(ModelManager modelManager,
                                        SecurityEngineConfiguration engineConfiguration);


        void handleCommunicationError(String badThingsHappen);


        void handleSaveSucceed();
    }

    private class SaveListenerBehavior extends Behaviour {
        @Override
        public void action() {
            Object obj = getAgent().getO2AObject();
            if (obj == null) {
                block();
                return;
            }

            Aid securityManagerAid = findSecurityManager();
            if (securityManagerAid == null) {
                return;
            }

            AclMessage requestSaveMessage = new AclMessage(AclMessage.Performative.REQUEST);
            requestSaveMessage.setProtocol(RequestProtocol.REQUEST);
            requestSaveMessage.setContent(XmlCodec.toXml((ModelManager)obj));
            requestSaveMessage.addReceiver(securityManagerAid);
            requestSaveMessage.encodeUserId(userId);

            addBehaviour(new RequestInitiator(getAgent(), new RequestSaveInitiator(), requestSaveMessage));
        }


        @Override
        public boolean done() {
            return false;
        }
    }
    private abstract class AbstractIntiatorHandler implements InitiatorHandler {

        public void handleAgree(AclMessage agree) {
        }


        public void handleRefuse(AclMessage refuse) {
            triggerError(refuse);
        }


        public void handleFailure(AclMessage failure) {
            triggerError(failure);
        }


        public void handleOutOfSequence(AclMessage outOfSequenceMessage) {
            triggerError(outOfSequenceMessage);
        }


        public void handleNotUnderstood(AclMessage notUnderstoodMessage) {
            triggerError(notUnderstoodMessage);
        }


        private void triggerError(AclMessage error) {
            handler.handleCommunicationError("Erreur technique : " + error.toFipaACLString());
        }
    }
    private class QueryModelInitiator extends AbstractIntiatorHandler {
        public void handleInform(AclMessage inform) {
            MessageBody body = XmlCodec.createBodyFromXml(inform.getContent());
            handler.handleReceiveSecurityModel(body.getModel(), body.getConfiguration());
        }
    }
    private class RequestSaveInitiator extends AbstractIntiatorHandler {
        public void handleInform(AclMessage inform) {
            handler.handleSaveSucceed();
        }
    }
}
