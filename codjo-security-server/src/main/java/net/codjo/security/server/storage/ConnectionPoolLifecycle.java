package net.codjo.security.server.storage;
import java.sql.SQLException;
import net.codjo.agent.UserId;
import net.codjo.plugin.common.session.SessionListener;
import net.codjo.plugin.common.session.SessionRefusedException;
import net.codjo.sql.server.JdbcManager;
import net.codjo.sql.server.JdbcServerException;
import org.apache.log4j.Logger;
/**
 *
 */
class ConnectionPoolLifecycle implements SessionListener {
    private final Logger logger = Logger.getLogger(ConnectionPoolLifecycle.class);
    private final JdbcManager jdbcManager;
    private final JdbcConfiguration jdbcConfiguration;


    ConnectionPoolLifecycle(JdbcManager jdbcManager, JdbcConfiguration jdbc) {
        this.jdbcManager = jdbcManager;
        this.jdbcConfiguration = jdbc;
    }


    public void handleSessionStart(UserId userId) throws SessionRefusedException {
        try {
            jdbcManager.createPool(userId,
                                   jdbcConfiguration.getApplicationLogin(),
                                   jdbcConfiguration.getApplicationPassword());
        }
        catch (SQLException e) {
            throw new SessionRefusedException("Le login applicatif BD est invalide "
                                              + e.getLocalizedMessage(),
                                              e);
        }
    }


    public void handleSessionStop(UserId userId) {
        try {
            jdbcManager.destroyPool(userId);
        }
        catch (JdbcServerException e) {
            logger.error("Impossible de détruire le pool " + userId.encode(), e);
        }
    }
}
