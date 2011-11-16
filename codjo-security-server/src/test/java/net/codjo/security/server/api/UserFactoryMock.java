/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.security.server.api;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.codjo.agent.UserId;
import net.codjo.security.common.api.SecurityContext;
import net.codjo.security.common.api.User;
import net.codjo.security.common.api.UserMock;
import net.codjo.test.common.LogString;
/**
 * Mock d'un {@link UserFactory}.
 */
public class UserFactoryMock implements UserFactory {
    private UserMock userMock = new UserMock();
    private LogString log;
    private List<String> roleNamesMock = Collections.emptyList();


    public UserFactoryMock() {
        log = new LogString();
    }


    public UserFactoryMock(LogString logString) {
        log = logString;
    }


    public User getUser(UserId userId, SecurityContext securityContext) {
        log.call("getUser", toString(userId), toString(securityContext));
        return userMock;
    }


    private String toString(SecurityContext securityContext) {
        return securityContext == null ? null : securityContext.getClass().getSimpleName();
    }


    private String toString(UserId userId) {
        return userId == null ? null : userId.encode();
    }


    public List<String> getRoleNames() {
        log.call("getRoleNames");
        return roleNamesMock;
    }


    public UserFactoryMock mockUserIsAllowedTo(boolean newIsAllowedTo) {
        userMock.mockIsAllowedTo(newIsAllowedTo);
        return this;
    }


    public void mockGetRoleNames(String... roleNames) {
        roleNamesMock = Arrays.asList(roleNames);
    }


    public void setLog(LogString log) {
        this.log = log;
    }
}
