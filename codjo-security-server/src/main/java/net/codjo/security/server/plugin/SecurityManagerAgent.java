/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.security.server.plugin;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Agent;
import net.codjo.agent.DFService;
import net.codjo.agent.UserId;
import net.codjo.agent.protocol.AbstractRequestParticipantHandler;
import net.codjo.agent.protocol.BasicQueryParticipantHandler;
import net.codjo.agent.protocol.FailureException;
import net.codjo.agent.protocol.RequestParticipant;
import net.codjo.agent.protocol.RequestProtocol;
import net.codjo.security.common.Constants;
import net.codjo.security.common.message.MessageBody;
import net.codjo.security.common.message.SecurityEngineConfiguration;
import net.codjo.security.common.message.XmlCodec;
import net.codjo.security.server.api.Storage;
import net.codjo.security.server.api.StorageFactory;
import org.apache.log4j.Logger;

import static net.codjo.agent.MessageTemplate.and;
import static net.codjo.agent.MessageTemplate.matchPerformative;
import static net.codjo.agent.MessageTemplate.matchProtocol;
/**
 *
 */
class SecurityManagerAgent extends Agent {
    private final Logger logger = Logger.getLogger(SecurityManagerAgent.class);
    private final ServerModelManagerImpl securityModel;
    private SecurityEngineConfiguration securityEngineConfiguration;
    private StorageFactory storageFactory;


    SecurityManagerAgent(ServerModelManagerImpl modelManager,
                         StorageFactory storageFactory,
                         SecurityEngineConfiguration securityEngineConfiguration) {
        this.securityModel = modelManager;
        this.storageFactory = storageFactory;
        this.securityEngineConfiguration = securityEngineConfiguration;
    }


    public String getSecurityModel() {
        return XmlCodec.toXml(new MessageBody(securityModel.getDatabaseModel(), securityEngineConfiguration));
    }


    @Override
    protected void setup() {
        try {
            DFService.register(this,
                               new DFService.AgentDescription(
                                     new DFService.ServiceDescription(Constants.SECURITY_MANAGER_SERVICE,
                                                                      "security-service")));
        }
        catch (DFService.DFServiceException e) {
            die();
            logger.error("Impossible d'inscrire l'agent auprès du DF : " + e.getLocalizedMessage(), e);
            return;
        }

        addBehaviour(new RequestParticipant(this, new BasicQueryParticipantHandler(this),
                                            and(matchPerformative(AclMessage.Performative.QUERY),
                                                matchProtocol(RequestProtocol.QUERY))));
        addBehaviour(new RequestParticipant(this, new SaveRequestHandler(),
                                            and(matchPerformative(AclMessage.Performative.REQUEST),
                                                matchProtocol(RequestProtocol.REQUEST))));
    }


    @Override
    protected void tearDown() {
        try {
            DFService.deregister(this);
        }
        catch (DFService.DFServiceException e) {
            logger.error("Impossible de se desinscrire l'agent auprès du DF : " + e.getLocalizedMessage(), e);
        }
    }


    private class SaveRequestHandler extends AbstractRequestParticipantHandler {
        public AclMessage executeRequest(AclMessage request, AclMessage agreement) throws FailureException {
            try {
                UserId userId = request.decodeUserId();
                logger.info(String.format("Enregistrement du modele de securite par %s", userId.getLogin()));
                Storage storage = storageFactory.create(userId);
                try {
                    securityModel.save(storage, XmlCodec.createFromXml(request.getContent()));
                }
                finally {
                    storageFactory.release();
                }
                return request.createReply(AclMessage.Performative.INFORM);
            }
            catch (Exception e) {
                logger.fatal("Enregistrement du modele de securite en echec", e);
                throw new FailureException("Enregistrement en échec" + e.getMessage());
            }
        }
    }
}
