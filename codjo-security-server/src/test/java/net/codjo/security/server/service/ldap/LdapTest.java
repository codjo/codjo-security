package net.codjo.security.server.service.ldap;
import java.util.Hashtable;
import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingException;
import net.codjo.agent.UserId;
import net.codjo.security.common.BadLoginException;
import net.codjo.test.common.LogString;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
/**
 *
 */
public class LdapTest {
    private static final LogString log = new LogString();


    @Test
    public void test_connect_setters() throws Exception {
        Ldap ldap = new Ldap();
        ldap.setContextFactory(MyLdapMock.class.getName());
        ldap.setServerUrl("ldap://local");
        ldap.setLoginPostFix("@am.fr");

        ldap.connect("gonnot", "secret");

        log.assertContent("getInitialContext(ldap://local, gonnot@am.fr, secret)"
                          + ", close()");
    }


    @Test
    public void test_connect_constructor() throws Exception {
        Ldap ldap = new Ldap("ldap://local", "@am.fr");
        ldap.setContextFactory(MyLdapMock.class.getName());

        ldap.connect("gonnot", "secret");

        log.assertContent("getInitialContext(ldap://local, gonnot@am.fr, secret)"
                          + ", close()");
    }


    @Test
    public void test_connect_empty() throws Exception {
        Ldap ldap = new Ldap("ldap://local", "@am.fr");
        ldap.setContextFactory(MyLdapMock.class.getName());

        assertConnectionFailure(ldap, "Mot de passe vide", "gonnot", "");
        assertConnectionFailure(ldap, "Mot de passe vide", "gonnot", "    ");
        assertConnectionFailure(ldap, "Mot de passe vide", "gonnot", null);
        assertConnectionFailure(ldap, "Identifiant de connexion vide", "", "secret");
        assertConnectionFailure(ldap, "Identifiant de connexion vide", null, "secret");

        log.assertContent("");
    }


    @Test
    public void test_connectRetry() throws Exception {
        Ldap defaultLdap = new Ldap("ldap://unknown", "@agif.fr");
        defaultLdap.setContextFactory(MyLdapMock.class.getName());

        Ldap backupLdap = new Ldap("ldap://local", "@agif.fr");
        backupLdap.setContextFactory(MyLdapMock.class.getName());

        defaultLdap.addBackupServer(backupLdap);

        UserId id = defaultLdap.connect("polo", "toto");

        log.assertContent("getInitialContext(ldap://unknown, polo@agif.fr, toto)"
                          + ", getInitialContext(ldap://local, polo@agif.fr, toto)"
                          + ", close()");
        assertNotNull(id);
    }


    @Test
    public void test_connectRetry_badLogin() throws Exception {
        Ldap defaultLdap = new Ldap("ldap://unknown", "@agif.fr");
        defaultLdap.setContextFactory(MyLdapMock.class.getName());

        Ldap backupLdap = new Ldap("ldap://local", "@agif.fr");
        backupLdap.setContextFactory(MyLdapMock.class.getName());

        defaultLdap.addBackupServer(backupLdap);

        try {
            defaultLdap.connect("polo", "badPassword");
            fail();
        }
        catch (BadLoginException e) {
            assertEquals("Compte ou mot de passe incorrect.", e.getMessage());
        }

        log.assertContent("getInitialContext(ldap://unknown, polo@agif.fr, badPassword)"
                          + ", getInitialContext(ldap://local, polo@agif.fr, badPassword)");
    }


    @Test
    public void test_connectRetry_connectionException() throws Exception {
        Ldap defaultLdap = new Ldap("ldap://unknown", "@agif.fr");
        defaultLdap.setContextFactory(MyLdapMock.class.getName());

        Ldap backupLdap = new Ldap("ldap://unknown", "@agif.fr");
        backupLdap.setContextFactory(MyLdapMock.class.getName());

        defaultLdap.addBackupServer(backupLdap);

        try {
            defaultLdap.connect("polo", "password");
            fail();
        }
        catch (BadLoginException e) {
        }

        log.assertContent("getInitialContext(ldap://unknown, polo@agif.fr, password)"
                          + ", getInitialContext(ldap://unknown, polo@agif.fr, password)");
    }


    @Before
    public void setUp() throws Exception {
        log.clear();
    }


    private void assertConnectionFailure(Ldap ldap, String message, String login, String password) {
        try {
            ldap.connect(login, password);
            fail();
        }
        catch (BadLoginException ex) {
            assertEquals(message, ex.getMessage());
        }
    }


    public static class MyLdapMock extends InitialContextMock {

        @SuppressWarnings({"SuspiciousMethodCalls"})
        @Override
        public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
            log.call("getInitialContext",
                     environment.get(Context.PROVIDER_URL),
                     environment.get(Context.SECURITY_PRINCIPAL),
                     environment.get(Context.SECURITY_CREDENTIALS));
            if (!"ldap://local".equals(environment.get(Context.PROVIDER_URL))) {
                throw new CommunicationException("network Error");
            }
            if ("badPassword".equals(environment.get(Context.SECURITY_CREDENTIALS))) {
                throw new AuthenticationException("password wrong");
            }
            return super.getInitialContext(environment);
        }


        @Override
        public void close() throws NamingException {
            log.call("close");
            super.close();
        }
    }
}
