/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.security.common.api;
import java.util.SortedSet;
import java.util.TreeSet;
import junit.framework.TestCase;
/**
 */
public class DefaultSecurityContextTest extends TestCase {
    public void test_isCallerInRole() throws Exception {
        SortedSet<String> set = new TreeSet<String>();
        set.add("roleA");
        set.add("roleB");
        SecurityContext ctxt = new DefaultSecurityContext(set);

        assertTrue("Utilisateur a le roleA", ctxt.isCallerInRole("roleA"));
        assertTrue("Utilisateur a le roleB", ctxt.isCallerInRole("roleB"));
        assertFalse("Utilisateur n'a pas le roleC", ctxt.isCallerInRole("rolec"));
    }
}
