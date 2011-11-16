package net.codjo.security.common.login;
/**
 *
 */
public class IncompatibleVersionException extends Exception {
    public IncompatibleVersionException(String serverVersion, String clientVersion) {
        super("Version client incompatible avec le serveur.\n"
              + "Vous devez utiliser un client ayant une version égale ou supérieure à celle du serveur.\n"
              + "Version serveur : '" + serverVersion + "'\n" + "Version client : '" + clientVersion + "'\n\n"
              + "Merci de bien vouloir contacter ASSET-SVP pour mettre à jour votre client.");
    }


    @Override
    public String toString() {
        return getLocalizedMessage();
    }
}
