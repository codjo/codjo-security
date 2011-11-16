package net.codjo.security.common;
import net.codjo.agent.ContainerConfiguration;
import org.apache.log4j.Logger;
/**
 *
 */
public class SecurityLevelHelper {
    private static final Logger APP = Logger.getLogger(SecurityLevelHelper.class);
    public static final String SECURITY_LEVEL_PARAMETER = "securityLevel";


    private SecurityLevelHelper() {
    }


    public static SecurityLevel getSecurityLevel(ContainerConfiguration containerConfiguration) {
        SecurityLevel returnValue = null;
        String value
              = containerConfiguration.getParameter(SECURITY_LEVEL_PARAMETER);
        if (value == null) {
            return returnValue;
        }
        try {
            returnValue = SecurityLevel.toEnum(value);
        }
        catch (Exception e) {
            APP.info(e);
            return returnValue;
        }
        return returnValue;
    }


    public static void setSecurityLevel(ContainerConfiguration containerConfiguration,
                                        SecurityLevel securityLevel) {
        String securityLevelStrValue = null;
        if (securityLevel != null) {
            securityLevelStrValue = Integer.toString(securityLevel.getValue());
        }
        containerConfiguration.setParameter(SECURITY_LEVEL_PARAMETER, securityLevelStrValue);
    }
}
