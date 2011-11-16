/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.security.common.api;
import java.util.HashMap;
import java.util.Map;
import net.codjo.agent.UserId;
/**
 * Mock de la classe {@link User}.
 */
public class UserMock implements User {
    private boolean isAllowedToValue = false;
    private boolean isInRoleValue = false;
    private Map<String, Boolean> securityMap = new HashMap<String, Boolean>();
    private Map<String, Boolean> roleMap = new HashMap<String, Boolean>();
    private UserId userId = UserId.createId("john", "mysecret");


    public UserId getId() {
        return userId;
    }


    public boolean isAllowedTo(String function) {
        if (securityMap.containsKey(function)) {
            return securityMap.get(function);
        }
        return isAllowedToValue;
    }


    public boolean isInRole(String roleId) {
        if (roleMap.containsKey(roleId)) {
            return roleMap.get(roleId);
        }
        return isInRoleValue;
    }


    @Override
    public String toString() {
        return "UserMock()";
    }


    public UserMock mockGetUserId(UserId id) {
        this.userId = id;
        return this;
    }


    public UserMock mockIsAllowedTo(boolean isAllowedTo) {
        this.isAllowedToValue = isAllowedTo;
        return this;
    }


    public UserMock mockIsAllowedTo(String function, boolean isAllowedTo) {
        securityMap.put(function, isAllowedTo);
        return this;
    }


    public UserMock mockIsInRole(boolean isInRole) {
        this.isInRoleValue = isInRole;
        return this;
    }


    public UserMock mockIsInRole(String roleId, boolean isInRole) {
        roleMap.put(roleId, isInRole);
        return this;
    }
}
