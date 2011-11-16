package net.codjo.security.server.service.ads;
import net.codjo.agent.Agent;
import net.codjo.agent.ServiceHelper;
import net.codjo.plugin.common.session.SessionManager;
import net.codjo.security.server.api.SecurityContextFactory;
import net.codjo.security.server.api.StorageFactory;
import net.codjo.security.server.api.UserFactory;
import net.codjo.security.server.model.ServerModelManager;
import net.codjo.security.server.service.AbstractSecurityService;
/**
 *
 */
public class AdsSecurityService extends AbstractSecurityService {
    private final AdsServiceWrapper adsServiceWrapper;
    private final UserFactory userFactory;
    private final SecurityContextFactory securityContextFactory;


    public AdsSecurityService(ServerModelManager serverModelManager,
                              SessionManager sessionManager,
                              AdsServiceWrapper adsServiceWrapper,
                              UserFactory userFactory,
                              SecurityContextFactory securityContextFactory,
                              StorageFactory storageFactory) {
        super(serverModelManager, sessionManager, storageFactory);
        this.adsServiceWrapper = adsServiceWrapper;
        this.userFactory = userFactory;
        this.securityContextFactory = securityContextFactory;
    }


    public ServiceHelper getServiceHelper(Agent agent) {
        return new AdsSecurityServiceHelper(adsServiceWrapper,
                                            userFactory,
                                            securityContextFactory,
                                            getServerModelManager(),
                                            getStorageFactory());
    }
}
