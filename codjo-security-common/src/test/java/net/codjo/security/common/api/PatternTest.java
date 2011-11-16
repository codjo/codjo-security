package net.codjo.security.common.api;
import junit.framework.TestCase;
/**
 * Classe de test de {@link net.codjo.security.common.api.Pattern}.
 */
public class PatternTest extends TestCase {
    public void test_patternEndsWithStar() throws Exception {
        Pattern pattern = new Pattern("select*");

        assertEquals(true, pattern.match("selectAllTruc"));
        assertEquals(true, pattern.match("selectFundPriceByDate"));
        assertEquals(true, pattern.match("select"));
        assertEquals(false, pattern.match("selec"));
        assertEquals(false, pattern.match("updateFundPriceById"));
        assertEquals(false, pattern.match("update_selectById"));
        assertEquals(false, pattern.match("SelectFundPriceByDate"));
    }


    public void test_patternSurroundedByStar() throws Exception {
        Pattern pattern = new Pattern("*FundPrice*");

        assertEquals(false, pattern.match("selectAllTruc"));
        assertEquals(true, pattern.match("selectFundPriceByDate"));
        assertEquals(false, pattern.match("selectFundPric"));
        assertEquals(false, pattern.match("select"));
        assertEquals(true, pattern.match("updateFundPriceById"));
    }


    public void test_patternWithNoStar() throws Exception {
        Pattern pattern = new Pattern("updateFundPriceById");

        assertEquals(false, pattern.match("selectAllTruc"));
        assertEquals(false, pattern.match("selectFundPriceByDate"));
        assertEquals(false, pattern.match("select"));
        assertEquals(true, pattern.match("updateFundPriceById"));
        assertEquals(false, pattern.match("updateFundPriceByIdTruc"));
        assertEquals(false, pattern.match("JJJJupdateFundPriceByIdTruc"));
    }


    public void test_patternWithIncludedStar() throws Exception {
        Pattern pattern = new Pattern("*FundPrice*Id");

        assertEquals(false, pattern.match("selectAllTruc"));
        assertEquals(false, pattern.match("selectFundPriceByDate"));
        assertEquals(false, pattern.match("select"));
        assertEquals(true, pattern.match("selectFundPriceFromId"));
        assertEquals(true, pattern.match("updateFundPriceById"));
    }


    public void test_patternWithChoice() throws Exception {
        Pattern pattern = new Pattern("(new|delete|update)FundPriceFrequency");
        assertEquals(true, pattern.match("newFundPriceFrequency"));
        assertEquals(true, pattern.match("deleteFundPriceFrequency"));
        assertEquals(true, pattern.match("updateFundPriceFrequency"));
        assertEquals(false, pattern.match("selectFundPriceFrequency"));
    }
}
