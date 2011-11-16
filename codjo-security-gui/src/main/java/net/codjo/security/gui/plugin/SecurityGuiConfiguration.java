package net.codjo.security.gui.plugin;
import javax.swing.JComponent;
/**
 *
 */
public interface SecurityGuiConfiguration {
    String getSplashImageUrl();


    void setSplashImageUrl(String splashImageUrl);


    LoginCaseType getLoginCaseType();


    void setLoginCaseType(LoginCaseType type);


    JComponent getLoginExtraComponent();


    void setLoginExtraComponent(JComponent loginExtraComponent);


    enum LoginCaseType {
        LOWER_CASE,
        UPPER_CASE,
        UNDEFINED
    }
}
