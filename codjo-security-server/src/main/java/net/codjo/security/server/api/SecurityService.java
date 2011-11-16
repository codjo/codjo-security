package net.codjo.security.server.api;
import net.codjo.agent.ContainerConfiguration;
import net.codjo.agent.Service;
import net.codjo.agent.ServiceException;
import org.apache.log4j.Logger;
/**
 * Service sécurité.
 *
 * <p>Cette classe intègre le composant sécurité dans le mécanisme des services JADE. Elle permet de fournir un {@link
 * SecurityServiceHelper} aux agents nécessitant une gestion des droits. </p>
 *
 * NB : ne doit pas être utilisé directement
 *
 * @see net.codjo.security.server.api.SecurityServiceHelper
 */
public abstract class SecurityService implements Service {
    private final Logger log = Logger.getLogger(SecurityService.class.getName());


    public String getName() {
        return SecurityServiceHelper.NAME;
    }


    public void boot(ContainerConfiguration containerConfiguration) throws ServiceException {
        log.info("Activation du service Security : " + getClass());
    }
}
