/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.security.common.api;
import java.io.Serializable;
import java.util.SortedSet;
/**
 *
 */
public class DefaultSecurityContext implements Serializable, SecurityContext {
    private final SortedSet<String> roleNames;


    public DefaultSecurityContext(SortedSet<String> roleNames) {
        this.roleNames = roleNames;
    }


    public boolean isCallerInRole(String roleId) {
        return roleNames.contains(roleId);
    }
}
