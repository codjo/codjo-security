package net.codjo.security.server.storage;
import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import net.codjo.security.common.message.DefaultModelManager;
import net.codjo.security.common.message.ModelManager;
import net.codjo.security.common.message.XmlCodec;
import net.codjo.sql.server.ConnectionPool;
import net.codjo.sql.server.util.SqlTransactionalExecutor;
/**
 *
 */
class JdbcStorage extends AbstractStorage {
    static final String MULTIPLE_LINES_ERROR_MESSAGE =
          BAD_DATA + "La table PM_SEC_MODEL contient plus d'une ligne "
          + "(1 seule maximum est autorisée)";
    private final ConnectionPool connectionPool;


    JdbcStorage(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }


    public ModelManager loadModel() throws Exception {
        Connection connection = connectionPool.getConnection();
        try {
            return loadModel(connection);
        }
        finally {
            connectionPool.releaseConnection(connection);
        }
    }


    public Timestamp saveModel(ModelManager manager) throws Exception {
        Connection connection = connectionPool.getConnection();
        try {
            return saveModel(manager, connection);
        }
        finally {
            connectionPool.releaseConnection(connection);
        }
    }


    public Timestamp getModelTimestamp() throws Exception {
        Connection connection = connectionPool.getConnection();
        try {
            return getModelTimestamp(connection);
        }
        finally {
            connectionPool.releaseConnection(connection);
        }
    }


    private ModelManager loadModel(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        try {
            ResultSet resultSet = statement.executeQuery("select VERSION, MODEL from PM_SEC_MODEL");
            if (!resultSet.next()) {
                return new DefaultModelManager();
            }

            int xmlModelVersion = resultSet.getInt("VERSION");
            if (xmlModelVersion != XmlCodec.XML_VERSION) {
                throw new BadConfigurationException(BAD_DATA + "La version du modèle '"
                                                    + xmlModelVersion + "' n'est pas gérée.");
            }

            String xml = resultSet.getString("MODEL");

            if (resultSet.next()) {
                throw new BadConfigurationException(MULTIPLE_LINES_ERROR_MESSAGE);
            }
            return fromXml(xml);
        }
        finally {
            statement.close();
        }
    }


    private Timestamp saveModel(ModelManager manager, Connection connection) throws SQLException {
        String xml = toXml(manager);
        SqlTransactionalExecutor.init(connection)
              .prepare("delete from PM_SEC_MODEL")
              .then()
              .prepare("insert into PM_SEC_MODEL (VERSION, MODEL)  values (?, ?)")
              .withInt(XmlCodec.XML_VERSION)
                    //@fixture : bug sybase avec le setString en echec sur des gros volumes (> 17ko)
                    //       Attention le fichier xml doit être de l'ASCII.
              .withAsciiStream(new ByteArrayInputStream(xml.getBytes()), xml.length())
              .then()
              .execute();
        return getModelTimestamp(connection);
    }


    private Timestamp getModelTimestamp(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        try {
            ResultSet rs = statement.executeQuery("select LAST_UPDATE from PM_SEC_MODEL");
            if (!rs.next()) {
                return NO_TIMESTAMP;
            }
            return rs.getTimestamp("LAST_UPDATE");
        }
        finally {
            statement.close();
        }
    }
}
