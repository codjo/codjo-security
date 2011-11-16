package net.codjo.security.server.service.ads;
import java.util.HashMap;
import java.util.Map;
import net.codjo.ads.AccountLockedException;
import net.codjo.ads.AdsException;
import net.codjo.ads.AdsService;
import net.codjo.ads.AdsSystemException;
import net.codjo.ads.UserToken;
import net.codjo.agent.UserId;
import net.codjo.plugin.common.session.SessionListener;
import net.codjo.security.common.SecurityLevel;
/**
 *
 */
public class AdsServiceWrapper implements SessionListener {
    private final AdsService adsService;
    private final Map<UserId, UserToken> userTokens = new HashMap<UserId, UserToken>();


    public AdsServiceWrapper(AdsService adsService) {
        this.adsService = adsService;
    }


    public void handleSessionStart(UserId userId) {
    }


    public void handleSessionStop(UserId userId) {
        userTokens.remove(userId);
    }


    public UserId login(String login, String password, SecurityLevel securityLevel)
          throws AdsSystemException, AccountLockedException {
        UserToken userToken = adsService.login(login, password, securityLevel.getValue());
        UserId userId = UserId.createId(login, password);
        userTokens.put(userId, userToken);
        return userId;
    }


    public void disconnectAll() throws AdsException {
        adsService.disconnectAll();
    }


    public String[] rolesFor(UserId userId) throws AdsException {
        UserToken userToken = userTokens.get(userId);
        if (userToken == null) {
            throw new AdsException(String.format("Unknown userId: %s", userId.getLogin()));
        }
        return adsService.rolesFor(userToken);
    }
}
