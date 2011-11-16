package net.codjo.security.server.storage;
import net.codjo.security.server.api.StorageFactory;
import org.apache.log4j.Logger;
/**
 *
 */
public abstract class AbstractStorageFactory implements StorageFactory {
    private static final Logger LOG = Logger.getLogger(StorageFactory.class);


    protected AbstractStorageFactory() {
        LOG.info(String.format("Utilisation de la stratégie %s pour la sauvegarde des données de connexion",
                               getClass().getSimpleName()));
    }
}
