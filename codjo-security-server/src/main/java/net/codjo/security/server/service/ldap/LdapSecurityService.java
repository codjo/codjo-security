/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.security.server.service.ldap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.codjo.agent.Agent;
import net.codjo.agent.ContainerConfiguration;
import net.codjo.agent.ServiceHelper;
import net.codjo.plugin.common.session.SessionManager;
import net.codjo.security.server.api.SecurityContextFactory;
import net.codjo.security.server.api.StorageFactory;
import net.codjo.security.server.api.UserFactory;
import net.codjo.security.server.model.ServerModelManager;
import net.codjo.security.server.service.AbstractSecurityService;
/*
 * Service Securite base sur LDAP et connexion applicative.
 */
public class LdapSecurityService extends AbstractSecurityService {
    public static final String DEFAULT_LDAP = "default";

    public static final String CONFIG_LDAP_URL_DEFAULT = "LdapSecurityService.ldap.url";
    public static final String CONFIG_LDAP_POSTFIX_DEFAULT = "LdapSecurityService.ldap.postfix";
    public static final String CONFIG_OTHER_DOMAINS = "LdapSecurityService.ldap.other-domains";
    public static final String CONFIG_BACKUP_SERVERS = "LdapSecurityService.ldap.backup-servers";
    public static final String CONFIG_LDAP_URL_OTHER = "LdapSecurityService.ldap.%s.url";
    public static final String CONFIG_LDAP_POSTFIX_OTHER = "LdapSecurityService.ldap.%s.postfix";
    public static final String CONFIG_BACKUP_SERVERS_OTHER = "LdapSecurityService.ldap.%s.backup-servers";

    private final UserFactory userFactory;
    private final SecurityContextFactory securityContextFactory;
    private Map<String, Ldap> ldaps;
    private static final String BAD_LDAP_CONFIGURATION_MESSAGE =
          "La configuration nécessaire pour la connexion LDAP sur le domaine %s est incomplète : "
          + "Vérifier la présence des properties '" + CONFIG_LDAP_URL_OTHER
          + "' et '" + CONFIG_LDAP_POSTFIX_OTHER + "'";
    private static final String BACKUP_ERROR = "Le serveur backup %s ne peut pas avoir de backup !!!";
    private static final String BACKUP_AND_OTHER_DOMAIN_ERROR
          = "Le serveur %s ne peut pas à la fois définir un autre domaine et être backup !!!";


    public LdapSecurityService(ContainerConfiguration configuration,
                               UserFactory userFactory,
                               SecurityContextFactory securityContextFactory,
                               ServerModelManager serverModelManager,
                               SessionManager sessionManager,
                               StorageFactory storageFactory) {
        super(serverModelManager, sessionManager, storageFactory);

        this.userFactory = userFactory;
        this.securityContextFactory = securityContextFactory;
        ldaps = loadLdapProperties(configuration);
    }


    private static Map<String, Ldap> loadLdapProperties(ContainerConfiguration configuration) {
        Ldap defaultLdap = new Ldap();
        defaultLdap.setLoginPostFix(get(configuration, CONFIG_LDAP_POSTFIX_DEFAULT, "@AM.AGF.FR"));
        defaultLdap.setServerUrl(get(configuration, CONFIG_LDAP_URL_DEFAULT, "ldap://a7sw302:389"));

        Map<String, Ldap> ldaps = new HashMap<String, Ldap>();
        ldaps.put(DEFAULT_LDAP, defaultLdap);

        String otherDomains = get(configuration, CONFIG_OTHER_DOMAINS, null);
        List<String> domains;
        if (otherDomains == null) {
            domains = Collections.emptyList();
        }
        else {
            domains = splitProperty(otherDomains);
            for (String domain : domains) {
                Ldap ldap = new Ldap();
                ldap.setLoginPostFix(configuration.getParameter(String.format(CONFIG_LDAP_POSTFIX_OTHER,
                                                                              domain)));
                ldap.setServerUrl(configuration.getParameter(String.format(CONFIG_LDAP_URL_OTHER, domain)));
                ldaps.put(domain, ldap);
                loadBackupServers(configuration,
                                  ldap,
                                  ldaps,
                                  String.format(CONFIG_BACKUP_SERVERS_OTHER, domain),
                                  domains);
            }
        }
        loadBackupServers(configuration, defaultLdap, ldaps, CONFIG_BACKUP_SERVERS, domains);
        return ldaps;
    }


    private static List<String> splitProperty(String otherDomains) {
        List<String> stringList = new ArrayList<String>();
        for (String s : otherDomains.split(",")) {
            stringList.add(s.trim());
        }
        return stringList;
    }


    private static void loadBackupServers(ContainerConfiguration configuration,
                                          Ldap ldap,
                                          Map<String, Ldap> ldaps,
                                          String backupServerKeys,
                                          List<String> domains) {
        String backupServers = get(configuration, backupServerKeys, null);
        if (backupServers != null) {
            for (String backup : splitProperty(backupServers)) {
                if (domains.contains(backup)) {
                    throw new IllegalArgumentException(String.format(BACKUP_AND_OTHER_DOMAIN_ERROR, backup));
                }
                if (configuration.getParameter(String.format(CONFIG_BACKUP_SERVERS_OTHER, backup)) != null) {
                    throw new IllegalArgumentException(String.format(BACKUP_ERROR, backup));
                }

                Ldap backupLdap = new Ldap();
                backupLdap.setLoginPostFix(configuration.getParameter(String.format(CONFIG_LDAP_POSTFIX_OTHER,
                                                                                    backup)));
                backupLdap.setServerUrl(configuration.getParameter(String.format(CONFIG_LDAP_URL_OTHER,
                                                                                 backup)));

                ldaps.put(backup, backupLdap);
                ldap.addBackupServer(backupLdap);
            }
        }
    }


    public ServiceHelper getServiceHelper(Agent agent) {
        return new LdapSecurityServiceHelper(userFactory,
                                             securityContextFactory,
                                             ldaps,
                                             getServerModelManager(),
                                             getStorageFactory());
    }


    public Map<String, Ldap> getLdaps() {
        return ldaps;
    }


    public static void assertConfigurationIsValid(ContainerConfiguration configuration) {
        Map<String, Ldap> ldaps = loadLdapProperties(configuration);
        for (String domain : ldaps.keySet()) {
            Ldap ldap = ldaps.get(domain);
            if (ldap.getServerUrl() == null || ldap.getLoginPostFix() == null) {
                throw new IllegalArgumentException(String.format(BAD_LDAP_CONFIGURATION_MESSAGE,
                                                                 domain,
                                                                 domain,
                                                                 domain));
            }
        }
    }
}
