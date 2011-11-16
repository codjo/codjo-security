/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.security.common.datagen;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import junit.framework.TestCase;
import net.codjo.security.common.api.Role;
import org.xml.sax.SAXException;
/**
 * Classe de test de {@link RoleReader}.
 */
public class RoleReaderTest extends TestCase {
    public void test_read() throws Exception {

        List roles = createReader().getRoles();

        assertEquals(2, roles.size());

        Role consultation = (Role)roles.get(0);
        assertEquals("consultation", consultation.getRoleId());
        assertTrue(consultation.isAllowedTo("selectTruc"));
        assertFalse(consultation.isAllowedTo("updateTruc"));

        Role adminVLRole = (Role)roles.get(1);
        assertEquals("administration_vl", adminVLRole.getRoleId());
        assertTrue(adminVLRole.isAllowedTo("selectFundPriceById"));
        assertTrue(adminVLRole.isAllowedTo("PortFolioCodificationDoStuff"));
        assertFalse(adminVLRole.isAllowedTo("selectAllTruc"));
        assertFalse(adminVLRole.isAllowedTo("selectAllFundPrice"));
    }


    public void test_rolesAreUnmodifiable() throws Exception {

        List roles = createReader().getRoles();

        try {
            roles.remove(0);
            fail();
        }
        catch (UnsupportedOperationException ex) {
            ; // Ok
        }
    }


    private RoleReader createReader() throws IOException, ParserConfigurationException, SAXException {
        File roleConfFile = new File(RoleReaderTest.class.getResource("RoleReaderTest.xml").getFile());
        return new RoleReader(new FileReader(roleConfFile));
    }
}
