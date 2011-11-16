package net.codjo.security.server.plugin;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.codjo.ads.AdsException;
import net.codjo.ads.AdsServiceMock;
import net.codjo.agent.UserId;
import net.codjo.security.common.api.UserData;
import net.codjo.security.common.message.DefaultModelManager;
import net.codjo.security.common.message.ModelManager;
import net.codjo.security.common.message.Role;
import net.codjo.security.common.message.RoleComposite;
import net.codjo.security.common.message.RoleVisitor;
import net.codjo.security.common.message.RoleVisitorMock;
import net.codjo.security.common.message.User;
import net.codjo.security.common.message.XmlCodec;
import net.codjo.security.server.api.Storage;
import net.codjo.security.server.api.UserFactoryMock;
import net.codjo.security.server.service.ads.AdsServiceWrapper;
import net.codjo.security.server.storage.StorageMock;
import net.codjo.test.common.LogString;
import net.codjo.test.common.matcher.JUnitMatchers;
import org.junit.Before;
import org.junit.Test;

import static net.codjo.test.common.matcher.JUnitMatchers.fail;
import static net.codjo.test.common.matcher.JUnitMatchers.hasItem;
import static net.codjo.test.common.matcher.JUnitMatchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
/**
 *
 */
public class ServerModelManagerImplTest {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
    private LogString log = new LogString();
    private ServerModelManagerImpl serverModelManager;
    private ModelManagerFileLoaderMock fileLoaderMock;
    private UserFactoryMock userFactoryMock;
    private StorageMock storageMock;


    @Before
    public void setUp() throws Exception {
        userFactoryMock = new UserFactoryMock();
        storageMock = createStorageMock();
        fileLoaderMock = createFileLoaderMock();
        serverModelManager = new ServerModelManagerImpl();
        serverModelManager.init(fileLoaderMock, userFactoryMock);
        log.clear();
    }


    @Test
    public void test_visit() throws Exception {
        userFactoryMock.mockGetRoleNames("admin");

        DefaultModelManager modelFromDB = new DefaultModelManager();
        modelFromDB.addRoleToUser(new Role("admin"), new User("smith"));

        DefaultModelManager modelFromFile = new DefaultModelManager();
        modelFromFile.addRoleToUser(new Role("guest"), new User("smith"));

        serverModelManager.init(fileLoaderMock.mockLoad(modelFromFile), userFactoryMock);

        serverModelManager.visit(storageMock.mockModel(modelFromDB),
                                 UserId.createId("smith", "passwd"),
                                 new RoleVisitorMock(log));

        log.assertContent("storage.getModelTimestamp()",
                          "storage.loadModel()",
                          "fileLoader.load()",
                          "visit(admin)",
                          "visit(guest)");
    }


    @Test
    public void test_visit_doNotLoadTwiceWithoutDbChange() throws Exception {
        serverModelManager.visit(storageMock,
                                 UserId.createId("smith", "passwd"),
                                 new RoleVisitorMock(log));
        log.clear();

        serverModelManager.visit(storageMock,
                                 UserId.createId("smith", "passwd"),
                                 new RoleVisitorMock(log));
        log.assertContent("storage.getModelTimestamp()");
    }


    @Test
    public void test_visit_reloadIfDatabaseChange() throws Exception {
        serverModelManager.visit(storageMock,
                                 UserId.createId("smith", "passwd"),
                                 new RoleVisitorMock(log));
        log.clear();

        storageMock.mockGetModelTimestamp(new Timestamp(System.currentTimeMillis() + 1));

        serverModelManager.visit(storageMock,
                                 UserId.createId("smith", "passwd"),
                                 new RoleVisitorMock(log));
        log.assertContent("storage.getModelTimestamp()", "storage.loadModel()");
    }


    @Test
    public void test_updateUserLastLogin() throws Exception {
        storageMock.mockGetModelTimestamp(Timestamp.valueOf("2007-01-05 10:20:30.0"));
        serverModelManager.updateUserLastLogin(storageMock, "smith");

        log.assertContent(
              "storage.getModelTimestamp()",
              "storage.loadModel()",
              "fileLoader.load()",
              String.format("storage.saveModel(%s)", toXml(serverModelManager.getDatabaseModel())));
    }


    @Test
    public void test_updateUserLastLogin_noReload() throws Exception {
        Timestamp initialModelTimestamp = Timestamp.valueOf("2000-01-01 10:00:00.0");
        Timestamp savedModelTimestamp = Timestamp.valueOf("2000-01-01 10:00:05.0");

        storageMock.mockGetModelTimestamp(initialModelTimestamp);
        storageMock.mockSaveModelTimestamp(savedModelTimestamp);
        serverModelManager.updateUserLastLogin(storageMock, "smith");
        log.clear();

        storageMock.mockGetModelTimestamp(savedModelTimestamp);
        serverModelManager.updateUserLastLogin(storageMock, "smith");

        String xml = toXml(serverModelManager.getDatabaseModel());
        log.assertContent("storage.getModelTimestamp()", String.format("storage.saveModel(%s)", xml));
    }


    @Test
    public void test_updateUserLastLogin_knownUser() throws Exception {
        userFactoryMock.mockGetRoleNames("admin");

        DefaultModelManager modelFromDB = new DefaultModelManager();
        modelFromDB.addRoleToUser(new Role("admin"), new User("smith"));
        storageMock.mockModel(modelFromDB);

        serverModelManager.updateUserLastLogin(storageMock, "smith");

        String xml = toXml(serverModelManager.getDatabaseModel());
        assertTrue(xml.contains("<role name=\"admin\"/>"));
        log.assertContent(
              "storage.getModelTimestamp()",
              "storage.loadModel()",
              "fileLoader.load()",
              String.format("storage.saveModel(%s)", xml));
    }


    @Test
    public void test_updateUserLastLogout() throws Exception {
        storageMock.mockGetModelTimestamp(Timestamp.valueOf("2007-01-05 10:20:30.0"));
        serverModelManager.updateUserLastLogout(storageMock, "smith");

        log.assertContent(
              "storage.getModelTimestamp()",
              "storage.loadModel()",
              "fileLoader.load()",
              String.format("storage.saveModel(%s)", toXml(serverModelManager.getDatabaseModel())));
    }


    @Test
    public void test_save() throws Exception {
        DefaultModelManager originalModel = new DefaultModelManager();
        originalModel.addRoleToUser(new Role("admin"), new User("smith", new Date(100)));
        originalModel.addUser(new User("wesson", new Date(100)));
        storageMock.mockModel(originalModel);

        DefaultModelManager modelFromGui = new DefaultModelManager();
        modelFromGui.addRoleToUser(new Role("guest"), new User("smith"));
        modelFromGui.addRoleToUser(new Role("guest"), new User("john"));

        serverModelManager.save(storageMock, modelFromGui);

        DefaultModelManager expected = new DefaultModelManager();
        expected.addRoleToUser(new Role("guest"), new User("smith", new Date(100)));
        expected.addRoleToUser(new Role("guest"), new User("john"));

        log.assertContent(
              "storage.getModelTimestamp()",
              "storage.loadModel()",
              "fileLoader.load()",
              String.format("storage.saveModel(%s)", toXml(expected)));
    }


    @Test
    public void test_save_noReload() throws Exception {
        Timestamp initialModelTimestamp = Timestamp.valueOf("2000-01-01 10:00:00.0");
        Timestamp savedModelTimestamp = Timestamp.valueOf("2000-01-01 10:00:05.0");

        storageMock.mockGetModelTimestamp(initialModelTimestamp);
        storageMock.mockSaveModelTimestamp(savedModelTimestamp);
        serverModelManager.save(storageMock, new DefaultModelManager());
        log.clear();

        storageMock.mockGetModelTimestamp(savedModelTimestamp);
        serverModelManager.save(storageMock, new DefaultModelManager());

        log.assertContent(
              "storage.getModelTimestamp()",
              String.format("storage.saveModel(%s)", toXml(serverModelManager.getDatabaseModel())));
    }


    @Test
    public void test_getUserData_fromDb() throws Exception {
        DefaultModelManager modelFromDB = new DefaultModelManager();
        modelFromDB.addRoleToUser(new Role("admin"), new User("smith", new Date(0)));

        serverModelManager.init(fileLoaderMock,
                                userFactoryMock);

        UserData userData = serverModelManager.getUserData(storageMock.mockModel(modelFromDB), "smith");
        assertNotNull(userData);
        assertEquals("smith", userData.getName());
        assertEquals(new Date(0), userData.getLastLogin());
    }


    @Test
    public void test_getUserData_fromFile() throws Exception {
        DefaultModelManager modelManager = new DefaultModelManager();
        modelManager.addRoleToUser(new Role("admin"), new User("smith", new Date(0)));

        serverModelManager.init(fileLoaderMock.mockLoad(modelManager),
                                userFactoryMock);

        UserData userData = serverModelManager.getUserData(storageMock, "smith");
        assertNotNull(userData);
        assertEquals("smith", userData.getName());
        assertEquals(new Date(0), userData.getLastLogin());
    }


    @Test
    public void test_getUserData_unknownUser() throws Exception {
        UserData userData = serverModelManager.getUserData(storageMock, "smith");
        assertNull(userData);
    }


    @Test
    public void test_reloadRoles() throws Exception {
        serverModelManager.reloadRoles(storageMock);
        assertTrue(serverModelManager.getDatabaseModel().getRoles().isEmpty());

        userFactoryMock.mockGetRoleNames("guest", "seb");
        serverModelManager.reloadRoles(storageMock);

        assertEquals(Arrays.asList(new Role("guest"), new Role("seb")),
                     serverModelManager.getDatabaseModel().getRoles());

        userFactoryMock.mockGetRoleNames("polo", "invité", "seb");
        serverModelManager.reloadRoles(storageMock);

        List<Role> roleList = serverModelManager.getDatabaseModel().getRoles();
        assertEquals(3, roleList.size());
        assertThat(roleList, JUnitMatchers.hasItems(new Role("polo"), new Role("invité"), new Role("seb")));
    }


    @Test
    public void test_load_removeDeprecatedRoles() throws Exception {
        storageMock.mockSaveModelTimestamp(StorageMock.NO_TIMESTAMP);
        userFactoryMock.mockGetRoleNames("admin");

        ModelManager guiModelManager = new DefaultModelManager();
        guiModelManager.addRoleToUser(new Role("guest"), new User("hiro"));
        serverModelManager.save(storageMock, guiModelManager);
        serverModelManager.reloadRoles(storageMock);

        ModelManager mergedManager = serverModelManager.getDatabaseModel();
        assertEquals(1, mergedManager.getRoles().size());
        assertThat(mergedManager.getRoles(), hasItem(new Role("admin")));
        assertTrue(mergedManager.getUserRoles(new User("hiro")).isEmpty());
    }


    @Test
    public void test_load_roleCompositeCannotBeDeprecated() throws Exception {
        storageMock.mockSaveModelTimestamp(StorageMock.NO_TIMESTAMP);
        userFactoryMock.mockGetRoleNames("admin");

        ModelManager guiModelManager = new DefaultModelManager();
        guiModelManager.addRoleToUser(new RoleComposite("guest"), new User("hiro"));
        serverModelManager.save(storageMock, guiModelManager);
        serverModelManager.reloadRoles(storageMock);

        ModelManager mergedManager = serverModelManager.getDatabaseModel();
        assertEquals(2, mergedManager.getRoles().size());
        assertThat(mergedManager.getRoles(), hasItems(new RoleComposite("guest"), new Role("admin")));
        assertEquals(1, mergedManager.getUserRoles(new User("hiro")).size());
        assertThat(mergedManager.getUserRoles(new User("hiro")), hasItem((Role)new RoleComposite("guest")));
    }


    public void setUpForAdsTest() throws Exception {
        userFactoryMock.mockGetRoleNames("admin",
                                         "tourist",
                                         "tanathonaute",
                                         "onlyInDb",
                                         "role1InFile",
                                         "role2InFile",
                                         "role1InDb",
                                         "role2InDb",
                                         "roleDb",
                                         "roleFile");

        serverModelManager.init(initModelFromFile(new LogString("ModelManagerFileLoader", log)),
                                userFactoryMock,
                                initModelFromAds(new LogString("AdsServiceWrapper", log)));
    }


    @Test
    public void test_visit_forAds() throws Exception {
        setUpForAdsTest();

        assertRolesForUser(iniModelFromStorage(new LogString("storage", log)),
                           "toto",
                           "storage.getModelTimestamp()",
                           "storage.loadModel()",
                           "ModelManagerFileLoader.load()",
                           "visit(admin)",
                           "visit(tourist)",
                           "visit(tanathonaute)",
                           "visitComposite(roleComposite1InFile)",
                           "visit(role1InFile)",
                           "visitComposite(roleComposite2InFile)",
                           "visit(role2InFile)",
                           "visitComposite(roleCompositeFile&Db)",
                           "visit(roleDb)",
                           "visitComposite(roleCompositeFile&Db)",
                           "visit(roleFile)");
        assertRolesForUser(iniModelFromStorage(new LogString("storage", log)),
                           "tété",
                           "storage.getModelTimestamp()",
                           "visit(admin)",
                           "visit(admin)",
                           "visit(tanathonaute)",
                           "visitComposite(roleComposite2InDb)",
                           "visit(role2InDb)");
    }


    @Test
    public void test_visit_for_ads_adsExceptionThrown() throws Exception {
        serverModelManager.init(initModelFromFile(new LogString("ModelManagerFileLoader", log)),
                                userFactoryMock,
                                new AdsServiceWrapperMock(log) {
                                    @Override
                                    public String[] rolesFor(UserId userId) throws AdsException {
                                        throw new AdsException(new RuntimeException("error is inside"));
                                    }
                                });

        try {
            serverModelManager.visit(iniModelFromStorage(log),
                                     UserId.createId("login", "password"),
                                     new RoleVisitorMock(log));
            fail();
        }
        catch (RuntimeException e) {
            assertEquals("Récupération des rôles impossible.", e.getLocalizedMessage());
            assertNull(e.getCause());
        }
    }


    @Test
    public void test_save_forAds() throws Exception {
        setUpForAdsTest();

        DefaultModelManager modelManagerFromGui = new DefaultModelManager();
        Role tourist = new Role("tourist");
        modelManagerFromGui.addRole(tourist);
        Role admin = new Role("admin");
        modelManagerFromGui.addRole(admin);
        modelManagerFromGui.addRoleToUser(admin, new User("toto",
                                                          dateFormat.parse("01/01/2010 0:00:00"),
                                                          dateFormat.parse("01/01/2010 0:00:10")));
        RoleComposite composite = new RoleComposite("composite");
        modelManagerFromGui.addRoleToComposite(tourist, composite);

        serverModelManager.save(storageMock, modelManagerFromGui);

        log.assertContent("storage.getModelTimestamp()",
                          "storage.loadModel()",
                          "ModelManagerFileLoader.load()",
                          String.format("storage.saveModel(%s)",
                                        "<model>\n"
                                        + "  <roles>\n"
                                        + "    <role name=\"tourist\"/>\n"
                                        + "    <role name=\"admin\"/>\n"
                                        + "    <role-composite name=\"composite\">\n"
                                        + "      <roles>\n"
                                        + "        <role reference=\"/model/roles/role\"/>\n"
                                        + "      </roles>\n"
                                        + "    </role-composite>\n"
                                        + "  </roles>\n"
                                        + "  <grants>\n"
                                        + "    <entry>\n"
                                        + "      <user name=\"toto\" lastLogin=\"01/01/2010 00:00:00\" lastLogout=\"01/01/2010 00:00:10\"/>\n"
                                        + "      <list/>\n"
                                        + "    </entry>\n"
                                        + "  </grants>\n"
                                        + "</model>"));
    }


    private void assertRolesForUser(Storage storage, String userName, String... expecteds) throws Exception {
        serverModelManager.visit(storage,
                                 UserId.createId(userName, "passwd"),
                                 new RoleVisitor() {
                                     public void visit(Role role) {
                                         log.call("visit", role.getName());
                                     }


                                     public void visitComposite(RoleComposite role) {
                                         log.call("visitComposite", role.getName());
                                         role.acceptForSubRoles(this);
                                     }
                                 });

        log.assertAndClear(expecteds);
    }


    private StorageMock iniModelFromStorage(LogString logString) throws ParseException {
        StorageMock aStorageMock = new StorageMock(logString);
        DefaultModelManager modelManager = new DefaultModelManager();

        User toto = new User("toto",
                             dateFormat.parse("01/01/2010 0:00:00"),
                             dateFormat.parse("01/01/2010 0:00:10"));
        Role admin = new Role("admin");
        Role tourist = new Role("tourist");
        Role onlyInDb = new Role("onlyInDB");
        modelManager.addRole(admin);
        modelManager.addRole(tourist);
        modelManager.addRole(onlyInDb);
        modelManager.addUser(toto);
        modelManager.addRoleToUser(admin, toto);
        modelManager.addRoleToUser(tourist, toto);
        modelManager.addRoleToUser(onlyInDb, toto);

        User titi = new User("titi");
        modelManager.addUser(titi);
        modelManager.addRoleToUser(tourist, titi);

        modelManager.addRole(new Role("hoover"));

        RoleComposite roleComposite = new RoleComposite("roleComposite1InDb");
        modelManager.addRoleToComposite(new Role("role1InDb"), roleComposite);
        RoleComposite roleComposite2 = new RoleComposite("roleComposite2InDb");
        modelManager.addRoleToComposite(roleComposite2, roleComposite);
        modelManager.addRoleToComposite(new Role("role2InDb"), roleComposite2);
        modelManager.addRoleToComposite(new Role("roleDb"), new RoleComposite("roleCompositeFile&Db"));

        aStorageMock.mockModel(modelManager);
        return aStorageMock;
    }


    private ModelManagerFileLoaderMock initModelFromFile(LogString logString) {
        ModelManagerFileLoaderMock managerFile = new ModelManagerFileLoaderMock(logString);
        DefaultModelManager modelFromFile = new DefaultModelManager();
        Role admin = new Role("admin");
        modelFromFile.addRole(admin);
        Role consult = new Role("consult");
        modelFromFile.addRole(consult);
        modelFromFile.addRole(new Role("visit"));
        User toto = new User("toto");
        modelFromFile.addUser(toto);
        User tata = new User("tata");
        modelFromFile.addUser(tata);
        modelFromFile.addRoleToUser(admin, toto);
        modelFromFile.addRoleToUser(consult, tata);

        RoleComposite roleComposite = new RoleComposite("roleComposite1InFile");
        modelFromFile.addRoleToComposite(new Role("role1InFile"), roleComposite);
        RoleComposite roleComposite2 = new RoleComposite("roleComposite2InFile");
        modelFromFile.addRoleToComposite(roleComposite2, roleComposite);
        modelFromFile.addRoleToComposite(new Role("role2InFile"), roleComposite2);
        modelFromFile.addRoleToComposite(new Role("roleFile"), new RoleComposite("roleCompositeFile&Db"));
        managerFile.mockLoad(modelFromFile);

        return managerFile;
    }


    private AdsServiceWrapperMock initModelFromAds(LogString logString) {
        AdsServiceWrapperMock adsServiceWrapper = new AdsServiceWrapperMock(logString);
        adsServiceWrapper.mockRolesFor(new User("toto"),
                                       new Role("tourist"),
                                       new Role("tanathonaute"),
                                       new RoleComposite("roleComposite1InFile"),
                                       new RoleComposite("roleCompositeFile&Db"));
        adsServiceWrapper.mockRolesFor(new User("tété"),
                                       new Role("admin"),
                                       new Role("tanathonaute"),
                                       new RoleComposite("roleComposite2InDb"));

        return adsServiceWrapper;
    }


    private class AdsServiceWrapperMock extends AdsServiceWrapper {
        private Map<String, String[]> grants = new HashMap<String, String[]>();


        private AdsServiceWrapperMock(LogString logString) {
            super(new AdsServiceMock(logString));
        }


        @Override
        public String[] rolesFor(UserId userId) throws AdsException {
            return grants.get(userId.getLogin());
        }


        public void mockRolesFor(User user, Role... roles) {
            String[] roleNames = new String[roles.length];
            for (int i = 0; i < roles.length; i++) {
                Role role = roles[i];
                roleNames[i] = role.getName();
            }
            grants.put(user.getName(), roleNames);
        }
    }


    private ModelManagerFileLoaderMock createFileLoaderMock() {
        return new ModelManagerFileLoaderMock((new LogString("fileLoader", log)));
    }


    private StorageMock createStorageMock() {
        return new StorageMock(new LogString("storage", log));
    }


    private String toXml(ModelManager databaseModel) {
        return XmlCodec.toXml(databaseModel);
    }
}
