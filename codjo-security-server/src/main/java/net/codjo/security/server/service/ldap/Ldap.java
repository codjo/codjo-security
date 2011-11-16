/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.security.server.service.ldap;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import net.codjo.agent.UserId;
import net.codjo.security.common.BadLoginException;
/**
 * Classe utilitaire permettant de s'authentifier auprès d'un serveur LDAP.
 */
class Ldap {
    static final String LDAP_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
    private String serverUrl;
    private String loginPostFix;
    private String contextFactory = LDAP_FACTORY;
    private final List<Ldap> backupServers = new ArrayList<Ldap>();


    Ldap() {
    }


    Ldap(String serverUrl, String loginPostFix) {
        this.loginPostFix = loginPostFix;
        this.serverUrl = serverUrl;
    }


    public String getServerUrl() {
        return serverUrl;
    }


    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }


    public String getLoginPostFix() {
        return loginPostFix;
    }


    public void setLoginPostFix(String loginPostFix) {
        this.loginPostFix = loginPostFix;
    }


    public String getContextFactory() {
        return contextFactory;
    }


    public void setContextFactory(String contextFactory) {
        this.contextFactory = contextFactory;
    }


    public List<Ldap> getBackupServers() {
        return backupServers;
    }


    public void addBackupServer(Ldap ldap) {
        backupServers.add(ldap);
    }


    UserId connect(String login, String password) throws BadLoginException {
        assertNotEmpty(login, "Identifiant de connexion vide");
        assertNotEmpty(password, "Mot de passe vide");
        try {
            tryConnect(login, password);
            return UserId.createId(login, password);
        }
        catch (CommunicationException e) {
            throw new BadLoginException("Impossible de se connecter au serveur.");
        }
    }


    private void tryConnect(String login, String password) throws BadLoginException, CommunicationException {
        //noinspection CollectionDeclaredAsConcreteClass,UseOfObsoleteCollectionType
        Hashtable<String, Object> env = new Hashtable<String, Object>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, contextFactory);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.PROVIDER_URL, serverUrl);
        env.put(Context.SECURITY_PRINCIPAL, login + loginPostFix);
        env.put(Context.SECURITY_CREDENTIALS, password);

        try {
            new InitialLdapContext(env, null).close();
        }
        catch (AuthenticationException e) {
            throw new BadLoginException("Compte ou mot de passe incorrect.");
        }
        catch (NamingException e) {
            for (Ldap backup : backupServers) {
                try {
                    backup.tryConnect(login, password);
                    return;
                }
                catch (CommunicationException e1) {
                    ;
                }
            }
            throw new CommunicationException();
        }
    }


    private void assertNotEmpty(String value, String message) throws BadLoginException {
        if (value == null || "".equals(value.trim())) {
            throw new BadLoginException(message);
        }
    }

/* LDAP FAST BINDING
    void fastBind(String login, String password) throws NamingException {
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.PROVIDER_URL, "ldap://a7sw302:389");

        Control[] connCtls = new Control[]{new FastBindConnectionControl()};

        //first time we initialize the context, no credentials are supplied
        //therefore it is an anonymous bind.

        InitialLdapContext ctx = new InitialLdapContext(env, connCtls);

        ctx.addToEnvironment(Context.SECURITY_PRINCIPAL, login);
        ctx.addToEnvironment(Context.SECURITY_CREDENTIALS, password);
        try {
            ctx.reconnect(connCtls);
        }
        catch (Throwable e) {
            e.printStackTrace();
        }

        ctx.close();
    }
*/
}

