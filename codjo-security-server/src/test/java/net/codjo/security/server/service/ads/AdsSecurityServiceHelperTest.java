package net.codjo.security.server.service.ads;
import net.codjo.ads.AdsException;
import net.codjo.ads.AdsServiceMock;
import net.codjo.agent.UserId;
import net.codjo.security.common.AccountLockedException;
import net.codjo.security.common.SecurityLevel;
import net.codjo.security.common.api.User;
import net.codjo.security.common.api.UserData;
import net.codjo.security.common.message.SecurityEngineConfiguration;
import net.codjo.security.server.api.SecurityContextFactoryMock;
import net.codjo.security.server.api.SecurityServiceHelper;
import net.codjo.security.server.api.SecurityServiceHelperTestCase;
import net.codjo.security.server.api.UserFactoryMock;
import net.codjo.security.server.model.ServerModelManagerMock;
import net.codjo.security.server.storage.StorageFactoryMock;
import net.codjo.security.server.storage.StorageMock;
import net.codjo.test.common.LogString;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
/**
 *
 */
public class AdsSecurityServiceHelperTest extends SecurityServiceHelperTestCase {
    private UserId userId = UserId.createId("myLogin", "myPassword");
    private SecurityContextFactoryMock securityContextFactoryMock
          = new SecurityContextFactoryMock(new LogString("SecurityContextFactory", log));
    private UserFactoryMock userFactoryMock = new UserFactoryMock(new LogString("UserFactory", log));
    private AdsSecurityServiceHelper adsSecurityServiceHelper;
    private AdsServiceWrapper adsServiceWrapper;
    private StorageMock storageMock = new StorageMock(log);
    private AdsServiceMock adsServiceMock;


    @Override
    protected SecurityServiceHelper createSecurityServiceHelper() {
        adsServiceMock = new AdsServiceMock(new LogString("AdsService", log));
        adsServiceWrapper = new AdsServiceWrapper(adsServiceMock);
        StorageFactoryMock storageFactoryMock = new StorageFactoryMock(log);
        storageFactoryMock.mockStorage(storageMock);
        adsSecurityServiceHelper = new AdsSecurityServiceHelper(
              adsServiceWrapper,
              userFactoryMock,
              securityContextFactoryMock,
              new ServerModelManagerMock(new LogString("ServerModelManager", log)),
              storageFactoryMock);

        return adsSecurityServiceHelper;
    }


    @Test
    public void test_login() throws Exception {
        UserId aUserId = adsSecurityServiceHelper.login("myLogin", "myPassword", SecurityLevel.USER);

        log.assertAndClear("AdsService.login(myLogin, myPassword, 1)");
        assertEquals("myLogin", aUserId.getLogin());
        assertEquals("myPassword", aUserId.getPassword());

        aUserId = adsSecurityServiceHelper.login("myLogin", "myPassword", "myDomain", SecurityLevel.USER);

        log.assertAndClear("AdsService.login(myLogin, myPassword, 1)");
        assertEquals("myLogin", aUserId.getLogin());
        assertEquals("myPassword", aUserId.getPassword());
    }


    @Test
    public void test_login_correctlyLinkedWithWrapper() throws Exception {
        UserId id = adsSecurityServiceHelper.login("myLogin", "myPasword", SecurityLevel.USER);
        try {
            adsServiceWrapper.rolesFor(id);
        }
        catch (AdsException e) {
            fail();
        }
    }


    @Test
    public void test_getUser() throws Exception {
        User user = adsSecurityServiceHelper.getUser(userId);

        log.assertContent("create(myLogin, myPassword)",
                          String.format("SecurityContextFactory.createSecurityContext(%s, %s)",
                                        userId.encode(),
                                        storageMock),
                          String.format("UserFactory.getUser(%s, SecurityContextMock)", userId.encode()),
                          "release()");
        assertNotNull(user);
        assertSame(userFactoryMock.getUser(null, null), user);
    }


    @Test
    public void test_getUserData() throws Exception {
        UserData userData = adsSecurityServiceHelper.getUserData(userId, "myLogin");

        log.assertContent("create(myLogin, myPassword)",
                          "ServerModelManager.getUserData(StorageMock, myLogin)",
                          "release()");
        assertEquals("dummy", userData.getName());
    }


    @Test
    public void test_accountLockedException() throws Exception {
        adsServiceMock.mockAccountLockedException("Account locked !!!");

        try {
            adsSecurityServiceHelper.login("myLogin", "myPassword", SecurityLevel.USER);
            fail();
        }
        catch (AccountLockedException e) {
            assertEquals("Le compte 'myLogin' est bloqué.", e.getMessage());
            assertEquals(SecurityEngineConfiguration.URL_TO_UNLOCK_ACCOUNT_FOR_ADS, e.getUrlToUnlock());
        }
    }
}
