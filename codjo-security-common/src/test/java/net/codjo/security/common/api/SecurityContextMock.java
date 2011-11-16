package net.codjo.security.common.api;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
 *
 */
public class SecurityContextMock implements SecurityContext {
    private final List<String> roles = new ArrayList<String>();


    public SecurityContextMock() {
        this("one-role");
    }


    public SecurityContextMock(String role) {
        roles.add(role);
    }


    public boolean isCallerInRole(String roleId) {
        return roles.contains(roleId);
    }


    public void mockCallerInRole(String roleId) {
        roles.add(roleId);
    }


    public void mockCallerInRoles(String... roleIds) {
        roles.addAll(Arrays.asList(roleIds));
    }
}
