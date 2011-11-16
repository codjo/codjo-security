package net.codjo.security.server.service.ads;
import net.codjo.ads.AdsSystemException;
import net.codjo.agent.ServiceException;
import net.codjo.agent.UserId;
import net.codjo.security.common.AccountLockedException;
import net.codjo.security.common.BadLoginException;
import net.codjo.security.common.SecurityLevel;
import net.codjo.security.common.message.SecurityEngineConfiguration;
import net.codjo.security.server.api.SecurityContextFactory;
import net.codjo.security.server.api.StorageFactory;
import net.codjo.security.server.api.UserFactory;
import net.codjo.security.server.model.ServerModelManager;
import net.codjo.security.server.service.AbstractSecurityServiceHelper;
/**
 *
 */
public class AdsSecurityServiceHelper extends AbstractSecurityServiceHelper {
    private final AdsServiceWrapper adsServiceWrapper;


    public AdsSecurityServiceHelper(AdsServiceWrapper adsServiceWrapper,
                                    UserFactory userFactory,
                                    SecurityContextFactory securityContextFactory,
                                    ServerModelManager serverModelManager,
                                    StorageFactory storageFactory) {
        super(userFactory, securityContextFactory, serverModelManager, storageFactory);
        this.adsServiceWrapper = adsServiceWrapper;
    }


    public UserId login(String login, String password, SecurityLevel securityLevel)
          throws ServiceException, BadLoginException, AccountLockedException {
        return login(login, password, null, securityLevel);
    }


    public UserId login(String login, String password, String domain, SecurityLevel securityLevel)
          throws ServiceException, BadLoginException, AccountLockedException {
        logInfo(login, securityLevel);
        try {
            return adsServiceWrapper.login(login, password, securityLevel);
        }
        catch (net.codjo.ads.AccountLockedException e) {
            throw new AccountLockedException(String.format("Le compte '%s' est bloqué.", login),
                                             SecurityEngineConfiguration.URL_TO_UNLOCK_ACCOUNT_FOR_ADS);
        }
        catch (AdsSystemException e) {
            throw new BadLoginException("Login et/ou mot de passe invalide(s).");
        }
    }
}
