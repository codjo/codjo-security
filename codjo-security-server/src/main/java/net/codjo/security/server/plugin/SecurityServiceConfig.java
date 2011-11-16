package net.codjo.security.server.plugin;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import net.codjo.agent.ContainerConfiguration;
/**
 *
 */
public class SecurityServiceConfig {
    public static final String SECURITY_SERVICE_CONFIG = "SecurityService.config";
    public static final String STORAGE_TYPE_PARAMETER = "storage.type";
    private final Map<String, String> parameterMap;
    private final String securityServiceConfig;


    SecurityServiceConfig(ContainerConfiguration configuration) {
        securityServiceConfig = configuration.getParameter(SECURITY_SERVICE_CONFIG);
        if (securityServiceConfig != null && !"".equals(securityServiceConfig)) {
            parameterMap = buildParametersMap(securityServiceConfig);
        }
        else {
            parameterMap = new HashMap<String, String>();
        }
    }


    public boolean engineIs(String engineId) {
        return securityServiceConfig != null && engineIs(engineId, securityServiceConfig);
    }


    private static boolean engineIs(String engineId, String securityConfig) {
        return securityConfig.startsWith(engineId);
    }


    public boolean storageIs(String storageId) {
        return storageId.equals(get(STORAGE_TYPE_PARAMETER));
    }


    public String get(String key) {
        return parameterMap.get(key);
    }


    public boolean containsKey(String key) {
        return parameterMap.containsKey(key);
    }


    public Set<Entry<String, String>> entrySet() {
        return parameterMap.entrySet();
    }


    public static Map<String, String> buildParametersMap(String securityConfig) {
        Map<String, String> parameterMap = new HashMap<String, String>();
        if (engineIs("ads:", securityConfig)) {
            securityConfig = securityConfig.substring(4);
        }
        if (securityConfig.length() > 0) {
            String[] parameters = securityConfig.split("\\|");
            if (parameters.length > 0) {
                for (String parameter : parameters) {
                    int index = parameter.indexOf('=');
                    if (index > 0) {
                        parameterMap.put(parameter.substring(0, index), parameter.substring(index + 1));
                    }
                }
            }
        }
        return parameterMap;
    }
}
