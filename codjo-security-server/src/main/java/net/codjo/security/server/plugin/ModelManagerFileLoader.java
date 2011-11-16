package net.codjo.security.server.plugin;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import net.codjo.security.common.message.DefaultModelManager;
import net.codjo.security.common.message.ModelManager;
import net.codjo.security.common.message.XmlCodec;
import org.apache.log4j.Logger;
/**
 *
 */
class ModelManagerFileLoader {
    private static final Logger LOG = Logger.getLogger(ModelManagerFileLoader.class);
    private final File securityFile;


    ModelManagerFileLoader(File securityFile) {
        this.securityFile = securityFile;
    }


    public ModelManager load() {
        if (securityFile == null || !securityFile.exists()) {
            displayUsage();
            return new DefaultModelManager();
        }

        LOG.info("Chargement de la configuration de sécurité par défaut '" + securityFile + "' chargé.");

        try {
            FileReader fileReader = new FileReader(securityFile);
            try {
                return XmlCodec.createFromXml(fileReader);
            }
            finally {
                fileReader.close();
            }
        }
        catch (IOException e) {
            LOG.warn("Impossible de charger la configuration par défaut");
            throw new IllegalStateException("Impossible de charger la configuration par défaut");
        }
    }


    private void displayUsage() {
        if (securityFile == null) {
            LOG.info("Parametre '-" + SecurityServerPlugin.SECURITY_FILE_PARAMETER
                     + "' n'est pas spécifié. Aucune configuration de sécurité par défaut n'est chargée.");
        }
        else {
            LOG.info("Fichier '" + securityFile + "' est introuvable. "
                     + "Aucune configuration de sécurité par défaut n'est chargée.");
        }
    }
}
