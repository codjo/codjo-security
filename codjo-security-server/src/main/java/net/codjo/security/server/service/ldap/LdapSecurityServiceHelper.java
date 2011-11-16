/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.security.server.service.ldap;
import java.util.Map;
import net.codjo.agent.UserId;
import net.codjo.security.common.BadLoginException;
import net.codjo.security.common.SecurityLevel;
import net.codjo.security.server.api.SecurityContextFactory;
import net.codjo.security.server.api.StorageFactory;
import net.codjo.security.server.api.UserFactory;
import net.codjo.security.server.model.ServerModelManager;
import net.codjo.security.server.service.AbstractSecurityServiceHelper;
/**
 * Classe Helper du service {@link LdapSecurityService}.
 */
public class LdapSecurityServiceHelper extends AbstractSecurityServiceHelper {
    private final Map<String, Ldap> ldaps;


    LdapSecurityServiceHelper(UserFactory userFactory,
                              SecurityContextFactory securityContextFactory,
                              Map<String, Ldap> ldaps,
                              ServerModelManager serverModelManager,
                              StorageFactory storageFactory) {
        super(userFactory, securityContextFactory, serverModelManager, storageFactory);
        this.ldaps = ldaps;
    }


    public UserId login(String login, String password, SecurityLevel securityLevel) throws BadLoginException {
        return login(login, password, null, securityLevel);
    }


    public UserId login(String login, String password, String domain, SecurityLevel securityLevel)
          throws BadLoginException {
        logInfo(login, securityLevel);
        if (domain == null) {
            domain = LdapSecurityService.DEFAULT_LDAP;
        }
        return ldaps.get(domain).connect(login, password);
    }
}
