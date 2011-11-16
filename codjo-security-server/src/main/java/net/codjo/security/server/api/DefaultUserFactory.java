/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.security.server.api;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import net.codjo.agent.UserId;
import net.codjo.security.common.api.SecurityContext;
import net.codjo.security.common.api.User;
import net.codjo.security.common.datagen.DatagenUserFactory;
import org.xml.sax.SAXException;
/**
 * Cette classe permet de creer des profiles utilisateurs basées sur la définition datagen.
 */
public class DefaultUserFactory implements UserFactory {
    private final DatagenUserFactory datagenUserFactory;


    public DefaultUserFactory(String path) throws IOException, ParserConfigurationException, SAXException {
        datagenUserFactory = new DatagenUserFactory(path);
    }


    public DefaultUserFactory(InputStream inputStream)
          throws IOException, ParserConfigurationException, SAXException {
        datagenUserFactory = new DatagenUserFactory(inputStream);
    }


    public User getUser(UserId userId, SecurityContext securityContext) {
        return datagenUserFactory.getUser(userId, securityContext);
    }


    public List<String> getRoleNames() {
        return datagenUserFactory.getAllRoleNames();
    }


    public static UserFactory createDefaultUserFactory() {
        InputStream inputStream = DefaultUserFactory.class.getResourceAsStream("/conf/role.xml");
        if (inputStream == null) {
            throw new IllegalArgumentException(
                  "Aucune configuration de securité par défaut n'a été trouvée : "
                  + "le fichier '/conf/role.xml' n'existe pas.");
        }

        try {
            return new DefaultUserFactory(inputStream);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            try {
                inputStream.close();
            }
            catch (IOException e) {
                ;
            }
        }
    }
}
