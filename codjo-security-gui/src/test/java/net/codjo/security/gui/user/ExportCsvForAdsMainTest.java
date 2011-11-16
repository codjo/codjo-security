package net.codjo.security.gui.user;
import junit.framework.Assert;
import junit.framework.TestCase;
/**
 *
 */
public class ExportCsvForAdsMainTest extends TestCase {
    public void test_cvsCodec_toCsv() throws Exception {
        Assert.assertEquals(";unitRole2;composedRole1\r\n"
                            + "user2;Y;N\r\n"
                            + "user1;N;Y\r\n"
              ,
                            ExportCsvForAdsMain.CsvCodec.toCsv(new ModelBuilder().addRole("unitRole1")
                                                                     .addRole("unitRole2")
                                                                     .addRoleComposite("composedRole1")
                                                                     .addRoleToUser("composedRole1", "user1")
                                                                     .addRoleToUser("unitRole2", "user2")
                                                                     .get()));
    }
}
