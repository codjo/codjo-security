/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.security.server.api;
import net.codjo.agent.Agent;
import net.codjo.agent.ServiceException;
import net.codjo.agent.UserId;
import net.codjo.security.common.BadLoginException;
import net.codjo.security.common.SecurityLevel;
import net.codjo.security.common.api.User;
import net.codjo.security.common.api.UserData;
import net.codjo.security.common.api.UserMock;
import net.codjo.test.common.LogString;
/**
 *
 */
public class SecurityServiceHelperMock implements SecurityServiceHelper {
    private final LogString logString;
    private UserMock userMock = new UserMock();
    private BadLoginException loginFailure;
    private UserData userData;
    private RuntimeException getUserException;


    public SecurityServiceHelperMock() {
        this(new LogString());
    }


    public SecurityServiceHelperMock(LogString logString) {
        this.logString = logString;
    }


    public UserId login(String login, String password, SecurityLevel securityLevel)
          throws BadLoginException, ServiceException {
        return login(login, password, null, securityLevel);
    }


    public UserId login(String login, String password, String domain, SecurityLevel securityLevel)
          throws ServiceException, BadLoginException {
        if (domain != null) {
            logString.call("login", login, password, domain, securityLevel);
        }
        else {
            logString.call("login", login, password, securityLevel);
        }
        if (loginFailure != null) {
            throw loginFailure;
        }
        return UserId.createId(login, password);
    }


    public User getUser(UserId userId) throws ServiceException {
        if (getUserException != null) {
            throw getUserException;
        }
        logString.call("getUser", userId.getLogin());
        return userMock;
    }


    public UserData getUserData(UserId userId, String userName) throws ServiceException {
        logString.call("getUserData", userId.getLogin());
        return userData;
    }


    public void init(Agent agent) {
        logString.call("init");
    }


    public void mockLoginFailure(BadLoginException failure) {
        loginFailure = failure;
    }


    public SecurityServiceHelperMock mockGetUserData(UserData mock) {
        userData = mock;
        return this;
    }


    public SecurityServiceHelperMock mockGetUserFailure(RuntimeException e) {
        getUserException = e;
        return this;
    }
}
