package net.codjo.security.common.api;
import java.io.Serializable;
import java.util.List;
/**
 *
 */
public class Role implements Serializable {
    private String roleId;
    private List<Pattern> includes;
    private List<Pattern> excludes;


    public Role(String roleId, List<Pattern> includes, List<Pattern> excludes) {
        this.roleId = roleId;
        this.includes = includes;
        this.excludes = excludes;
    }


    public String getRoleId() {
        return roleId;
    }


    public void addInclude(Pattern pattern) {
        includes.add(pattern);
    }


    public void addExclude(Pattern pattern) {
        excludes.add(pattern);
    }


    public boolean isAllowedTo(String function) {
        if (match(includes, function)) {
            if (!match(excludes, function)) {
                return true;
            }
        }
        return false;
    }


    private boolean match(List<Pattern> patternList, String function) {
        for (Pattern pattern : patternList) {
            if (pattern.match(function)) {
                return true;
            }
        }
        return false;
    }
}
