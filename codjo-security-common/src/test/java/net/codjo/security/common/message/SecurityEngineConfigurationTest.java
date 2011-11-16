package net.codjo.security.common.message;
import net.codjo.security.common.message.SecurityEngineConfiguration.UserManagement;
import org.junit.Test;

import static net.codjo.security.common.message.SecurityEngineConfiguration.adsConfiguration;
import static net.codjo.security.common.message.SecurityEngineConfiguration.defaultConfiguration;
import static net.codjo.test.common.matcher.JUnitMatchers.assertThat;
import static net.codjo.test.common.matcher.JUnitMatchers.equalTo;
import static net.codjo.test.common.matcher.JUnitMatchers.is;
import static net.codjo.test.common.matcher.JUnitMatchers.not;
import static net.codjo.test.common.matcher.JUnitMatchers.nullValue;
/**
 *
 */
public class SecurityEngineConfigurationTest {

    @Test
    public void testAdsBean() throws Exception {
        SecurityEngineConfiguration engineConfiguration = adsConfiguration("http://my.jnlp");
        assertThat(engineConfiguration.getUserManagementType(), is(UserManagement.EXTERNAL));
        assertThat(engineConfiguration.getJnlp(), is("http://my.jnlp"));
        assertThat(engineConfiguration.getHelpUrl(),
                   is("http://wp-confluence/confluence/display/framework/Guide+Utilisateur+IHM+de+agf-security+ads"));
    }


    @Test
    public void testDefaultBean() throws Exception {
        SecurityEngineConfiguration engineConfiguration = defaultConfiguration();
        assertThat(engineConfiguration.getUserManagementType(), is(UserManagement.INTERNAL));
        assertThat(engineConfiguration.getJnlp(), is(nullValue()));
        assertThat(engineConfiguration.getHelpUrl(),
                   is("http://wp-confluence/confluence/display/framework/Guide+Utilisateur+IHM+de+agf-security"));
    }


    @Test
    public void testEquals() throws Exception {
        assertThat(defaultConfiguration(), equalTo(defaultConfiguration()));
        assertThat(adsConfiguration("jnlp"), equalTo(adsConfiguration("jnlp")));

        assertThat(adsConfiguration("jnlp"), not(equalTo(adsConfiguration("otherJnlp"))));
        assertThat(adsConfiguration("jnlp"), not(equalTo(defaultConfiguration())));
    }
}
