package net.codjo.security.gui.user;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.JList;
import net.codjo.security.common.message.Role;
import net.codjo.security.common.message.User;
import net.codjo.security.gui.model.GuiModelManager;
/**
 *
 */
class UserSearchAdapter extends AbstractSearchAdapter<User> {
    private static final String NEW_USER = "new";
    private static final String DEPRECATED_USER = "old";
    private static final String EMPTY_USER = "empty";
    private final GuiModelManager guiManager;


    UserSearchAdapter(GuiModelManager guiManager, JList userList) {
        super(userList);
        this.guiManager = guiManager;
    }


    public List<User> doSearch(String searchPattern) {
        searchPattern = searchPattern.toLowerCase();
        List<User> foundUser = new ArrayList<User>();
        for (User user : guiManager.getUsers()) {
            if (userMatch(user, searchPattern)) {
                foundUser.add(user);
            }
        }
        return foundUser;
    }


    boolean directMatch(User user, String searchPattern) {
        return directMatchImpl(user, searchPattern.toLowerCase());
    }


    private boolean userMatch(User user, String searchPattern) {
        if (directMatchImpl(user, searchPattern)) {
            return true;
        }
        List<Role> roleList = guiManager.getUserRoles(user);
        for (Role role : roleList) {
            if (role.getName().toLowerCase().contains(searchPattern)) {
                return true;
            }
        }
        return false;
    }


    private boolean directMatchImpl(User user, String searchPattern) {
        if (EMPTY_USER.equals(searchPattern)) {
            return guiManager.getUserRoles(user).isEmpty();
        }
        if (NEW_USER.equals(searchPattern)) {
            return user.getLastLogin() == null;
        }
        if (DEPRECATED_USER.equals(searchPattern)) {
            Date lastLogout = user.getLastLogout();
            Calendar instance = Calendar.getInstance();
            instance.set(Calendar.MONTH, -3);
            Date threeMonthAgo = instance.getTime();
            return lastLogout != null
                   && lastLogout.compareTo(threeMonthAgo) < 0;
        }
        return user.getName().toLowerCase().contains(searchPattern);
    }
}
