package net.codjo.security.server.api;
import net.codjo.agent.ServiceException;
import net.codjo.agent.ServiceHelper;
import net.codjo.agent.UserId;
import net.codjo.security.common.AccountLockedException;
import net.codjo.security.common.BadLoginException;
import net.codjo.security.common.SecurityLevel;
import net.codjo.security.common.api.User;
import net.codjo.security.common.api.UserData;
/**
 * @see net.codjo.security.server.api.SecurityService
 */
public interface SecurityServiceHelper extends ServiceHelper {
    public static final String NAME = "SecurityService.NAME";


    public UserId login(String login, String password, SecurityLevel securityLevel)
          throws ServiceException, BadLoginException, AccountLockedException;


    public UserId login(String login, String password, String domain, SecurityLevel securityLevel)
          throws ServiceException, BadLoginException, AccountLockedException;


    public User getUser(UserId userId) throws ServiceException;


    public UserData getUserData(UserId userId, String userName) throws ServiceException;
}
