package net.codjo.security.client.plugin;
import net.codjo.plugin.common.ApplicationCore;
import org.apache.log4j.Logger;
/**
 * Listener ecoutant la mort du loginAgent pour déclencher un system exit.
 */
class DeathLoginAgentListener implements LoginAgentListener {
    public static final int INTERNAL_ERROR_LOGIN_AGENT_KILLED = 201;
    private static final Logger logger = Logger.getLogger(DeathLoginAgentListener.class);
    private ApplicationCore applicationCore;


    DeathLoginAgentListener(ApplicationCore core) {
        this.applicationCore = core;
    }


    public void stopped() {
        if (!applicationCore.isStopping()) {
            new Thread(new Runnable() {
                public void run() {
                    silentStop();
                }
            }).start();
        }
    }


    private void silentStop() {
        if (!applicationCore.isStopping()) {
            //noinspection finally
            try {
                applicationCore.stop();
            }
            catch (Exception e) {
                logger.error(
                      "Impossible d'arreter proprement et en urgence le container !"
                      + e.getLocalizedMessage());
            }
            finally {
                System.exit(INTERNAL_ERROR_LOGIN_AGENT_KILLED);
            }
        }
    }
}