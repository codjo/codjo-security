/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.security.server.api;
import java.sql.SQLException;
import net.codjo.agent.UserId;
import net.codjo.security.common.api.SecurityContext;
import net.codjo.security.common.api.SecurityContextMock;
import net.codjo.test.common.LogString;
/**
 *
 */
public class SecurityContextFactoryMock implements SecurityContextFactory {
    private LogString log = new LogString();
    private String mockedRole = "administrateur";


    public SecurityContextFactoryMock() {
    }


    public SecurityContextFactoryMock(LogString log) {
        this.log = log;
    }


    public SecurityContext createSecurityContext(UserId id, Storage storage)
          throws SQLException {
        log.call("createSecurityContext", id.encode(), storage);
        return new SecurityContextMock(mockedRole);
    }


    public void mockSecurityContextRole(String role) {
        mockedRole = role;
    }


    public void setLog(LogString log) {
        this.log = log;
    }
}
