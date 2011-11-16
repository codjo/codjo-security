/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.security.common.datagen;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import net.codjo.agent.UserId;
import net.codjo.security.common.api.DefaultUser;
import net.codjo.security.common.api.Role;
import net.codjo.security.common.api.SecurityContext;
import net.codjo.security.common.api.User;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
/**
 * Construction d'un {@link User} à partir du fichier '/conf/role.xml' generer par Datagen.
 */
public class DatagenUserFactory {
    private RoleReader reader;


    public DatagenUserFactory(InputStream confFile)
          throws IOException, ParserConfigurationException, SAXException {
        if (confFile != null) {
            reader = new RoleReader(confFile);
        }
        else {
            Logger.getLogger(getClass()).warn("Configuration introuvable. Bascule en mode development");
        }
    }


    public DatagenUserFactory(String path) throws SAXException, ParserConfigurationException, IOException {
        this(DatagenUserFactory.class.getResourceAsStream(path));
    }


    public DatagenUserFactory() {
    }


    public User getUser(UserId userId, SecurityContext securityContext) {
        if (reader == null) {
            return new Admin(userId);
        }
        return new DefaultUser(userId, reader.getRoles(), securityContext);
    }


    public List<String> getAllRoleNames() {
        if (reader == null) {
            return Collections.emptyList();
        }

        List<String> names = new ArrayList<String>();
        for (Role role : reader.getRoles()) {
            names.add(role.getRoleId());
        }

        return names;
    }


    /**
     * Utilisateur par défaut si aucune gestion de profil (autorisé a tout faire).
     */
    private static class Admin implements User {
        private final UserId userId;


        Admin(UserId userId) {
            this.userId = userId;
        }


        public UserId getId() {
            return userId;
        }


        public boolean isAllowedTo(String function) {
            return true;
        }


        public boolean isInRole(String roleId) {
            return true;
        }
    }
}
