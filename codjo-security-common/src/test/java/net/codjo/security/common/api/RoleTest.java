/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.security.common.api;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
/**
 * Classe de test de {@link Role}.
 */
public class RoleTest extends TestCase {
    private Role role = null;


    public void test_patternEndsWithStar() throws Exception {
        role.addInclude(new Pattern("select*"));

        assertTrue(role.isAllowedTo("selectFundprice"));
        assertFalse(role.isAllowedTo("updateFundprice"));
    }


    public void test_roleWithExclude() throws Exception {
        role.addInclude(new Pattern("select*"));
        role.addExclude(new Pattern("selectBench"));

        assertTrue(role.isAllowedTo("selectFundprice"));
        assertFalse(role.isAllowedTo("selectBench"));
        assertFalse(role.isAllowedTo("updateFundprice"));
    }


    public void test_roleWithIncludesAndExcludes()
          throws Exception {
        role.addInclude(new Pattern("select*"));
        role.addInclude(new Pattern("update*"));
        role.addExclude(new Pattern("*Bench*"));
        role.addExclude(new Pattern("selectBobo"));

        assertTrue(role.isAllowedTo("selectFundprice"));
        assertTrue(role.isAllowedTo("updateFundprice"));
        assertFalse(role.isAllowedTo("selectBench"));
        assertFalse(role.isAllowedTo("updateBenchByTruc"));
        assertFalse(role.isAllowedTo("selectBobo"));
    }


    @Override
    protected void setUp() throws Exception {
        List<Pattern> includes = new ArrayList<Pattern>();
        includes.add(new Pattern("select*"));
        includes.add(new Pattern("*FundPrice*"));
        includes.add(new Pattern("PortFolioCodification*"));

        List<Pattern> excludes = new ArrayList<Pattern>();
        excludes.add(new Pattern("selectAllTruc"));

        role = new Role("consultation", includes, excludes);
    }
}
