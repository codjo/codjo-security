package net.codjo.security.gui.plugin;
import net.codjo.security.common.api.User;
import net.codjo.security.gui.api.ConfigurationSession;
/**
 *
 */
public interface SecurityGuiPluginOperations {
    User getUser();


    ConfigurationSession createConfigurationSession();
}
