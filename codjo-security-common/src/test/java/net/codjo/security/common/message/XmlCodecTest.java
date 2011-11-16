package net.codjo.security.common.message;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import junit.framework.TestCase;
import net.codjo.test.common.XmlUtil;

import static net.codjo.security.common.message.AssertUtil.assertSetEquals;
import static net.codjo.security.common.message.SecurityEngineConfiguration.adsConfiguration;
import static net.codjo.security.common.message.SecurityEngineConfiguration.defaultConfiguration;
/**
 *
 */
public class XmlCodecTest extends TestCase {
    private DefaultModelManager manager = new DefaultModelManager();


    public void test_modelManager_toXml() throws Exception {
        XmlUtil.assertEquals("<model><grants/></model>", XmlCodec.toXml(manager));

        User smith = new User("smith");
        manager.addUser(new User("kasparov"));
        manager.addUser(smith);
        manager.addUser(new User("hiro", new Date(0)));
        manager.addUser(new User("adriana", new Date(0), new Date(1000)));

        Role guest = new Role("guest");
        manager.addRole(guest);

        manager.addRoleToUser(guest, smith);

        XmlUtil.assertEquals("<model>"
                             + "  <roles><role name='guest'/></roles>"
                             + "  <grants>"
                             + "    <entry><user name='kasparov'/><list/></entry>"
                             + "    <entry>"
                             + "      <user name='smith'/>"
                             + "      <list>"
                             + "        <role reference='/model/roles/role'/>"
                             + "      </list>"
                             + "    </entry>"
                             + "    <entry><user lastLogin='01/01/1970 01:00:00' lastLogout='01/01/1970 01:00:01' name='adriana'/><list/></entry>"
                             + "    <entry><user lastLogin='01/01/1970 01:00:00' name='hiro'/><list/></entry>"
                             + "  </grants>"
                             + "</model>"
              , XmlCodec.toXml(manager));
    }


    public void test_modelManager_toXml_composite() throws Exception {

        User smith = new User("smith");
        Role guest = new Role("guest");
        RoleComposite admin = new RoleComposite("admin");
        admin.addRole(guest);

        manager.setRoles(Arrays.asList(guest, admin));
        manager.addRoleToUser(admin, smith);

        XmlUtil.assertEquals("<model>"
                             + "<roles>"
                             + "    <role name='guest'/>"
                             + "    <role-composite name='admin'>"
                             + "        <roles><role reference='/model/roles/role'/></roles>"
                             + "    </role-composite>"
                             + "</roles>"
                             + "<grants>"
                             + "    <entry>"
                             + "        <user name='smith'/>"
                             + "        <list>"
                             + "            <role-composite reference='/model/roles/role-composite'/>"
                             + "        </list>"
                             + "    </entry>"
                             + "</grants>"
                             + "</model>"
              , XmlCodec.toXml(manager));
    }


    public void test_modelManager_fromXml() throws Exception {
        manager = (DefaultModelManager)XmlCodec.createFromXml(simpleModel());

        assertSimpleModel(manager);
    }


    public void test_modelManager_fromXmlWithLastLogin() throws Exception {
        String model = "<model>"
                       + "<grants>"
                       + "    <entry>"
                       + "        <user name='smith' lastLogin='18/03/1973 05:04:03'/>"
                       + "        <list/>"
                       + "    </entry>"
                       + "</grants>"
                       + "</model>";
        manager = (DefaultModelManager)XmlCodec.createFromXml(model);

        assertEquals(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse("18/03/1973 05:04:03"),
                     manager.findUser("smith").getLastLogin());
    }


    public void test_modelManager_fromXmlWithBadLastLogin() throws Exception {
        String model = "<model>"
                       + "<grants>"
                       + "    <entry>"
                       + "        <user name='smith' lastLogin='badbabad'/>"
                       + "        <list/>"
                       + "    </entry>"
                       + "</grants>"
                       + "</model>";
        manager = (DefaultModelManager)XmlCodec.createFromXml(model);

        assertEquals(null, manager.findUser("smith").getLastLogin());
    }


    public void test_modelManager_fromXmlReader() throws Exception {
        manager = (DefaultModelManager)XmlCodec.createFromXml(new StringReader(simpleModel()));

        assertSimpleModel(manager);
    }


    public void test_modelManager_fromXmlWithoutRoles() throws Exception {
        String model = "<model>"
                       + "<grants>"
                       + "    <entry>"
                       + "        <user name='smith'/>"
                       + "        <list><role name='guest'/></list>"
                       + "    </entry>"
                       + "</grants>"
                       + "</model>";
        manager = (DefaultModelManager)XmlCodec.createFromXml(model);
        assertSimpleModel(manager);
        assertEquals(0, manager.getRoles().size());
        manager.addRole(new Role("eur"));
        assertEquals(1, manager.getRoles().size());
    }


    public void test_messageBody_default_toXml() throws Exception {
        MessageBody messageBody = new MessageBody(manager, defaultConfiguration());

        XmlUtil.assertEquals("<body>"
                             + "  <model class='model'><grants/></model>"
                             + "  <configuration>"
                             + "    <userManagementType>INTERNAL</userManagementType>"
                             + "    <helpUrl>http://wp-confluence/confluence/display/framework/Guide+Utilisateur+IHM+de+agf-security</helpUrl>"
                             + "  </configuration>"
                             + "</body>", XmlCodec.toXml(messageBody));
    }


    public void test_messageBody_ads_toXml() throws Exception {
        MessageBody messageBody = new MessageBody(manager, adsConfiguration("myJnlp"));

        XmlUtil.assertEquals("<body>"
                             + "  <model class='model'><grants/></model>"
                             + "  <configuration>"
                             + "    <userManagementType>EXTERNAL</userManagementType>"
                             + "    <jnlp>myJnlp</jnlp>"
                             + "    <helpUrl>http://wp-confluence/confluence/display/framework/Guide+Utilisateur+IHM+de+agf-security+ads</helpUrl>"
                             + "  </configuration>"
                             + "</body>", XmlCodec.toXml(messageBody));
    }


    public void test_messageBody_toAndFromXml() throws Exception {
        MessageBody messageBody = new MessageBody(manager, defaultConfiguration());

        String xml = XmlCodec.toXml(messageBody);
        MessageBody result = XmlCodec.createBodyFromXml(xml);
        XmlUtil.assertEquals(xml, XmlCodec.toXml(result));
    }


    private String simpleModel() {
        return "<model>"
               + "<roles>"
               + "    <role name='guest'/>"
               + "</roles>"
               + "<grants>"
               + "    <entry>"
               + "        <user name='smith'/>"
               + "        <list><role reference='/model/roles/role'/></list>"
               + "    </entry>"
               + "</grants>"
               + "</model>";
    }


    private void assertSimpleModel(DefaultModelManager defaultModelManager) {
        assertSetEquals("[User(smith)]", defaultModelManager.getUsers());

        List<Role> roles = defaultModelManager.getUserRoles(new User("smith"));
        assertEquals("[Role(guest)]", roles.toString());
    }
}
