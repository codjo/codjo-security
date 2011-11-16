package net.codjo.security.client.plugin;
import java.net.InetAddress;
import java.net.UnknownHostException;
import net.codjo.agent.AgentContainer;
import net.codjo.agent.AgentController;
import net.codjo.agent.BadControllerException;
import net.codjo.agent.ContainerConfiguration;
import net.codjo.agent.UserId;
import net.codjo.agent.util.IdUtil;
import net.codjo.plugin.common.ApplicationCore;
import net.codjo.plugin.common.ApplicationPlugin;
import net.codjo.plugin.common.session.SessionManager;
import net.codjo.security.common.SecurityLevel;
import net.codjo.security.common.SecurityLevelHelper;
import net.codjo.security.common.api.User;
import net.codjo.security.common.login.LoginEvent;
import net.codjo.util.system.ClassUtil;
import net.codjo.util.system.EventSynchronizer;
import org.apache.log4j.Logger;
/**
 *
 */
public class SecurityClientPlugin implements ApplicationPlugin {
    private static final Logger APP = Logger.getLogger(SecurityClientPlugin.class);
    static final String MISSING_LOGIN_PASSWORD_MESSAGE =
          "Les informations de login et de password sont manquantes.";
    static final String NO_RESPONSE_MESSAGE =
          "Le serveur ne semble pas répondre. Veuillez réessayer.\n"
          + "Si le problème persiste merci de contacter l'administrateur.";
    private final ApplicationCore applicationCore;
    private final SessionManager sessionManager;
    private final LoginAgentListener killListener;
    private UserId userId;
    private String login;
    private String password;
    private String ip;
    private String optionalLDap;
    private String version;
    private AgentController loginAgentController;
    private final SecurityClientPluginConfiguration pluginConfiguration
          = new SecurityClientPluginConfiguration();
    private SecurityLevel securityLevel;


    public SecurityClientPlugin(ApplicationCore applicationCore, SessionManager sessionManager) {
        this(new DeathLoginAgentListener(applicationCore), applicationCore, sessionManager);
    }


    SecurityClientPlugin(LoginAgentListener killListener,
                         ApplicationCore applicationCore,
                         SessionManager sessionManager) {
        this.killListener = killListener;
        this.applicationCore = applicationCore;
        this.sessionManager = sessionManager;
        version = ClassUtil.getMainClassVersion();
    }


    public void initContainer(ContainerConfiguration containerConfiguration) throws Exception {
        login = containerConfiguration.getParameter(SecurityClientPluginConfiguration.LOGIN_PARAMETER);
        password = containerConfiguration.getParameter(SecurityClientPluginConfiguration.PASSWORD_PARAMETER);
        ip = containerConfiguration.getLocalHost();
        optionalLDap = containerConfiguration.getParameter(SecurityClientPluginConfiguration.LDAP_PARAMETER);
        securityLevel = initSecurityLevel(containerConfiguration);
        if (login == null || password == null) {
            throw new IllegalArgumentException(MISSING_LOGIN_PASSWORD_MESSAGE);
        }
        containerConfiguration.setContainerName(
              new StringBuilder("country_of_")
                    .append(login)
                    .append((optionalLDap != null) ? "_in_" + optionalLDap : "")
                    .append("_")
                    .append(IdUtil.createUniqueId(this)).toString());
        containerConfiguration.setJadeFileDirToTemporaryDir();
    }


    public void start(AgentContainer agentContainer) throws Exception {
        EventSynchronizer<LoginEvent> loginEventSynchronizer = new EventSynchronizer<LoginEvent>();
        LoginAgent loginAgent = new LoginAgent(login,
                                               password,
                                               optionalLDap,
                                               getVersion(),
                                               ip,
                                               securityLevel, loginEventSynchronizer);
        loginAgentController = agentContainer.acceptNewAgent(createLoginAgentName(), loginAgent);
        loginAgentController.start();

        LoginEvent loginEvent = loginEventSynchronizer.waitEvent();
        if (loginEvent == null) {
            throw new NoServerResponseException(NO_RESPONSE_MESSAGE);
        }
        if (loginEvent.hasFailed()) {
            throw new LoginFailureException(loginEvent.getLoginFailureException());
        }
        loginAgent.setLoginAgentListener(killListener);

        System.setProperty("user.name", login);
        userId = loginEvent.getUserId();
        applicationCore.addGlobalComponent(userId);
        applicationCore.addGlobalComponent(User.class, new UserWithLog(loginEvent.getUser()));
        sessionManager.startSession(userId);
    }


    private SecurityLevel initSecurityLevel(ContainerConfiguration containerConfiguration) {
        SecurityLevel returnSecurityLevel = SecurityLevelHelper.getSecurityLevel(containerConfiguration);
        if (returnSecurityLevel == null) {
            return SecurityLevel.SERVICE;
        }
        return returnSecurityLevel;
    }


    public void stop() throws BadControllerException {
        if (userId != null) {
            sessionManager.stopSession(userId);
            userId = null;
        }
        applicationCore.removeGlobalComponent(UserId.class);
        applicationCore.removeGlobalComponent(User.class);
        if (loginAgentController != null) {
            try {
                loginAgentController.kill();
            }
            catch (BadControllerException e) {
                ;
            }
        }
        SecurityLevelHelper.setSecurityLevel(applicationCore.getContainerConfig(), null);
    }


    public String getVersion() {
        Class mainClass = pluginConfiguration.getMainClass();
        return (mainClass != null ? ClassUtil.getMainClassVersion(mainClass) : version);
    }


    public UserId getUserId() {
        return userId;
    }


    private String createLoginAgentName() {
        return "Login-" + login + "-" + getComputerName() + IdUtil.createUniqueId(this);
    }


    private String getComputerName() {
        try {
            return InetAddress.getLocalHost().getCanonicalHostName();
        }
        catch (UnknownHostException e) {
            return "Unknown";
        }
    }


    public SecurityClientPluginConfiguration getPluginConfiguration() {
        return pluginConfiguration;
    }


    public static class LoginFailureException extends Exception {
        LoginFailureException(String message) {
            super(message);
        }


        LoginFailureException(Throwable cause) {
            super(cause);
        }
    }

    public static class NoServerResponseException extends Exception {
        public NoServerResponseException(String message) {
            super(message);
        }


        public NoServerResponseException(Throwable cause) {
            super(cause);
        }
    }

    private static class UserWithLog implements User {
        private User user;


        private UserWithLog(User user) {
            this.user = user;
        }


        public UserId getId() {
            return user.getId();
        }


        public boolean isAllowedTo(String function) {
            boolean allowedTo = user.isAllowedTo(function);
            if (APP.isDebugEnabled()) {
                APP.debug("La fonction '" + function + "'" + (allowedTo ? "est acceptée" : "est refusée"));
            }
            return allowedTo;
        }


        public boolean isInRole(String roleId) {
            return user.isInRole(roleId);
        }
    }
}
