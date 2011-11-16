package net.codjo.security.gui.user;
import java.util.Arrays;
import java.util.Date;
import javax.swing.JFrame;
import javax.swing.UIManager;
import net.codjo.security.common.message.DefaultModelManager;
import net.codjo.security.common.message.Role;
import net.codjo.security.common.message.RoleComposite;
import net.codjo.security.common.message.SecurityEngineConfiguration;
import net.codjo.security.common.message.User;
/**
 *
 */
public class TestMain {
    private TestMain() {
    }


    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        DefaultModelManager manager = new DefaultModelManager();
        manager.setRoles(Arrays.asList(new Role("guest"),
                                       new Role("admin"),
                                       new Role("dev"),
                                       new Role("king"),
                                       new RoleComposite("admin_vl")));
        manager.addUser(new User("dupont", new Date()));
        manager.addUser(new User("louis",
                                 java.sql.Date.valueOf("2002-01-01"),
                                 java.sql.Date.valueOf("2002-01-02")));
        manager.addRoleToUser(new Role("admin"), new User("smith"));
        manager.addRoleToUser(new Role("king"), new User("louis"));

        JFrame frame = new JFrame("Administration");
        MainForm form = new MainForm();
        form.setManager(manager, SecurityEngineConfiguration.defaultConfiguration());
//        form.setManager(manager, SecurityEngineConfiguration.adsConfiguration("http://google.com"));
        frame.setContentPane(form.getMainPanel());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
