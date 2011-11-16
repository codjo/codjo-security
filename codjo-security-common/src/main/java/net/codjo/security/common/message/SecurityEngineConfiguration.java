package net.codjo.security.common.message;
/**
 *
 */
public class SecurityEngineConfiguration {
    public static final String URL_TO_UNLOCK_ACCOUNT_FOR_ADS
          = "https://ldapbv.intradit.net/adsbv-online/pages/actions/other/UnlockAccount.jsp ";
    private UserManagement userManagementType;
    private String jnlp;
    private String helpUrl;


    private SecurityEngineConfiguration(UserManagement userManagementType, String jnlp, String helpUrl) {
        this.userManagementType = userManagementType;
        this.jnlp = jnlp;
        this.helpUrl = helpUrl;
    }


    public static SecurityEngineConfiguration adsConfiguration(String jnlpUrl) {
        return new SecurityEngineConfiguration(
              UserManagement.EXTERNAL,
              jnlpUrl,
              "http://wp-confluence/confluence/display/framework/Guide+Utilisateur+IHM+de+agf-security+ads");
    }


    public static SecurityEngineConfiguration defaultConfiguration() {
        return new SecurityEngineConfiguration(
              UserManagement.INTERNAL,
              null,
              "http://wp-confluence/confluence/display/framework/Guide+Utilisateur+IHM+de+agf-security");
    }


    public UserManagement getUserManagementType() {
        return userManagementType;
    }


    public String getJnlp() {
        return jnlp;
    }


    public String getHelpUrl() {
        return helpUrl;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SecurityEngineConfiguration other = (SecurityEngineConfiguration)o;

        return !(jnlp != null ? !jnlp.equals(other.jnlp) : other.jnlp != null)
               && userManagementType == other.userManagementType;
    }


    @Override
    public int hashCode() {
        int result = userManagementType.hashCode();
        result = 31 * result + (jnlp != null ? jnlp.hashCode() : 0);
        return result;
    }


    public static enum UserManagement {
        INTERNAL("InternalManagement"),
        EXTERNAL("ExternalManagement");
        private String id;


        UserManagement(String id) {
            this.id = id;
        }


        public String getId() {
            return id;
        }
    }
}
