package net.codjo.security.gui.plugin;
import javax.swing.JComponent;
/**
 *
 */
public class DefaultSecurityGuiConfiguration implements SecurityGuiConfiguration {
    private String splashImageUrl = "/images/splash.jpg";
    private LoginCaseType loginCaseType = LoginCaseType.UNDEFINED;
    private JComponent loginExtraComponent = null;


    public String getSplashImageUrl() {
        return splashImageUrl;
    }


    public void setSplashImageUrl(String splashImageUrl) {
        if (splashImageUrl == null) {
            throw new IllegalArgumentException("splashImageUrl ne peut pas être null");
        }
        this.splashImageUrl = splashImageUrl;
    }


    public LoginCaseType getLoginCaseType() {
        return loginCaseType;
    }


    public void setLoginCaseType(LoginCaseType loginCaseType) {
        this.loginCaseType = loginCaseType;
    }


    public JComponent getLoginExtraComponent() {
        return loginExtraComponent;
    }


    public void setLoginExtraComponent(JComponent loginExtraComponent) {
        this.loginExtraComponent = loginExtraComponent;
    }
}
