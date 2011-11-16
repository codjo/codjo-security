package net.codjo.security.server.service.ldap;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.naming.Context;
import javax.naming.NamingException;
import net.codjo.agent.UserId;
import net.codjo.security.common.SecurityLevel;
import net.codjo.security.common.api.User;
import net.codjo.security.common.api.UserData;
import net.codjo.security.server.api.SecurityContextFactoryMock;
import net.codjo.security.server.api.SecurityServiceHelper;
import net.codjo.security.server.api.SecurityServiceHelperTestCase;
import net.codjo.security.server.api.UserFactoryMock;
import net.codjo.security.server.model.ServerModelManagerMock;
import net.codjo.security.server.storage.StorageFactoryMock;
import net.codjo.security.server.storage.StorageMock;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
/**
 *
 */
public class LdapSecurityServiceHelperTest extends SecurityServiceHelperTestCase {
    private ServerModelManagerMock modelManager = new ServerModelManagerMock();
    private UserFactoryMock userFactory = new UserFactoryMock();

    private SecurityContextFactoryMock securityContextFactory = new SecurityContextFactoryMock();

    private LdapSecurityServiceHelper security;

    private Map<String, Ldap> ldaps = new HashMap<String, Ldap>();
    private StorageFactoryMock storageFactoryMock = new StorageFactoryMock(log);
    private StorageMock storageMock;


    @Override
    public void setUp() throws Exception {
        super.setUp();

        Ldap ldap = new Ldap();
        ldap.setContextFactory(AcceptLoginMock.class.getName());
        ldap.setLoginPostFix("@am.fr");
        ldap.setServerUrl("ldap:/url");
        ldaps.put(LdapSecurityService.DEFAULT_LDAP, ldap);
        storageMock = new StorageMock(log);
        storageFactoryMock.mockStorage(storageMock);
    }


    @Override
    protected SecurityServiceHelper createSecurityServiceHelper() {
        security = new LdapSecurityServiceHelper(userFactory,
                                                 securityContextFactory,
                                                 ldaps,
                                                 modelManager,
                                                 storageFactoryMock);
        return security;
    }


    @Test
    public void test_login() throws Exception {
        UserId userId = security.login("gonnot", "secret", SecurityLevel.SERVICE);

        assertEquals("gonnot", userId.getLogin());
        log.assertContent("authenticate(gonnot@am.fr, secret)");
    }


    @Test
    public void test_login_multiLDap() throws Exception {
        Ldap ldap = new Ldap();
        ldap.setContextFactory(AcceptLoginMock.class.getName());
        ldap.setLoginPostFix("@gdo.fr");
        ldap.setServerUrl("ldap:/url_gdo");
        ldaps.put("gdo", ldap);
        UserId userId = security.login("gonnot_gdo", "secret2", "gdo", SecurityLevel.SERVICE);

        assertEquals("gonnot_gdo", userId.getLogin());
        log.assertContent("authenticate(gonnot_gdo@gdo.fr, secret2)");
    }


    @Test
    public void test_getUser() throws Exception {
        userFactory.setLog(logger("user"));
        securityContextFactory.setLog(logger("context"));

        User user = security.getUser(UserId.decodeUserId("gonnot/4b314c6b425947634573733d/0/0"));

        assertNotNull(user);
        log.assertContent("create(gonnot, secret)",
                          String.format(
                                "context.createSecurityContext(gonnot/4b314c6b425947634573733d/0/0, %s)",
                                storageMock),
                          "user.getUser(gonnot/4b314c6b425947634573733d/0/0, SecurityContextMock)",
                          "release()");
    }


    @Test
    public void test_getUserData() throws Exception {
        modelManager.setLog(logger("modelManager"));

        modelManager.mockGetUserData(new net.codjo.security.common.message.User("mock"));

        UserData userData = security.getUserData(UserId.decodeUserId("gonnot/4b314c6b425947634573733d/0/0"),
                                                 "bobo");

        log.assertContent("create(gonnot, secret)",
                          "modelManager.getUserData(StorageMock, bobo)",
                          "release()");
        assertNotNull(userData);
    }


    public static class AcceptLoginMock extends InitialContextMock {

        @Override
        public Context getInitialContext(Hashtable environment) throws NamingException {
            log.call("authenticate",
                     environment.get(Context.SECURITY_PRINCIPAL),
                     environment.get(Context.SECURITY_CREDENTIALS));
            return super.getInitialContext(environment);
        }
    }
}
