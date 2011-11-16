package net.codjo.security.gui.user;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import javax.swing.JFrame;
import net.codjo.database.common.api.ConnectionMetadata;
import net.codjo.database.common.api.DatabaseFactory;
import net.codjo.database.common.api.JdbcFixture;
import net.codjo.security.common.message.DefaultModelManager;
import net.codjo.security.common.message.ModelManager;
import net.codjo.security.common.message.Role;
import net.codjo.security.common.message.RoleComposite;
import net.codjo.security.common.message.SecurityEngineConfiguration;
import net.codjo.security.common.message.User;
import net.codjo.security.common.message.XmlCodec;
@SuppressWarnings({"UseOfSystemOutOrSystemErr"})
public class TestRosesMain {
    DefaultModelManager manager = new DefaultModelManager();


    TestRosesMain() throws SQLException {
        buildRolesList();

        buildComposite();

        linkUserToRole();

        System.out.println(XmlCodec.toXml(manager));

        ConnectionMetadata connectionMetadata = new ConnectionMetadata();
        connectionMetadata.setHostname("ar_roses");
        connectionMetadata.setPort("24100");
        connectionMetadata.setUser("GUILLIE");
        connectionMetadata.setPassword("?????");
        connectionMetadata.setCatalog("ROSES");
        JdbcFixture jdbc = new DatabaseFactory().createJdbcFixture(connectionMetadata);

        Connection connection = jdbc.getConnection();

        saveModel(connection, manager, Timestamp.valueOf("2007-07-30 10:10:10"));
    }


    public static void saveModel(Connection connection, ModelManager manager, Timestamp timestamp)
          throws SQLException {
        PreparedStatement statement =
              connection.prepareStatement("delete from PM_SEC_MODEL "
                                          + "insert into PM_SEC_MODEL (VERSION, LAST_UPDATE, MODEL) "
                                          + "values (?, ?, ?)");
        try {
            statement.setInt(1, XmlCodec.XML_VERSION);
            statement.setTimestamp(2, timestamp);
            statement.setString(3, XmlCodec.toXml(manager));
            statement.execute();
        }
        finally {
            statement.close();
        }
    }


    private void linkUserToRole() {
        linkRoleToUser("ROSES_retrocharges",
                       "renauxb\n");
        linkRoleToUser("ROSES_attestation",
                       "renauxb\n");
        linkRoleToUser("ROSES_fraisgestionOPCVM",
                       "hureaut\n");
        linkRoleToUser("ROSES_group",
                       "tourneu\n"
                       + "bras\n"
                       + "gaujouc\n"
                       + "lanvier\n"
                       + "guillie\n"
                       + "levequt\n"
                       + "wilson\n"
                       + "gonnot\n"
                       + "testpds\n"
                       + "user_batch\n"
                       + "mocquer\n"
                       + "idelovi\n"
                       + "alvesm\n"
                       + "tourell\n"
                       + "duchemc\n"
                       + "tans\n"
                       + "dore\n");
    }


    private void buildComposite() {
        createRole("ROSES_group", new String[]{"admin"});
        createRole("ROSES_consultation", new String[]{"consultation"});
        createRole("ROSES_encours", new String[]{"modification_encours"});
        createRole("ROSES_attestation", new String[]{"attestation"});
        createRole("ROSES_retrocharges", new String[]{"retrocharges"});
        createRole("ROSES_fraisgestionOPCVM", new String[]{"fraisgestionOPCVM"});
        createRole("ROSES_fraisgestionMANDAT", new String[]{"fraisgestionMANDAT"});
    }


    private void buildRolesList() {
        manager.addRole(new Role("admin"));
        manager.addRole(new Role("consultation"));
        manager.addRole(new Role("modification_encours"));
        manager.addRole(new Role("attestation"));
        manager.addRole(new Role("retrocharges"));
        manager.addRole(new Role("fraisgestionOPCVM"));
        manager.addRole(new Role("fraisgestionMANDAT"));
    }


    private RoleComposite createRole(String name, String[] content) {
        RoleComposite composite = new RoleComposite(name);
        manager.addRole(composite);
        for (String role : content) {
            manager.addRoleToComposite(getRole(role), composite);
        }
        return composite;
    }


    private void linkRoleToUser(String roleName, String users) {
        linkRoleToUser(roleName, users.split("\\n"));
    }


    private void linkRoleToUser(String roleName, String[] users) {
        Role role = getComposite(roleName);
        for (String userName : users) {
            if (userName.trim().length() == 0) {
                break;
            }
            User user = manager.findUser(userName.trim());
            if (user == null) {
                user = new User(userName.trim());
                manager.addUser(user);
            }
            manager.addRoleToUser(role, user);
        }
    }


    private Role getComposite(String roleName) {
        for (Role role : manager.getRoles()) {
            if (roleName.trim().equalsIgnoreCase(role.getName())) {
                return role;
            }
        }
        throw new IllegalArgumentException("Role inconnu " + roleName);
    }


    private Role getRole(String name) {
        List<Role> roles = manager.getRoles();
        int idx = roles.indexOf(new Role(name.trim()));
        if (idx == -1) {
            throw new IllegalArgumentException("Role inconnu " + name);
        }
        return roles.get(idx);
    }


    public static void main(String[] args) throws SQLException {
        DefaultModelManager manager = new TestRosesMain().manager;

        JFrame frame = new JFrame("UserForm");
        MainForm form = new MainForm();
        form.setManager(manager, SecurityEngineConfiguration.defaultConfiguration());
        frame.setContentPane(form.getMainPanel());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
