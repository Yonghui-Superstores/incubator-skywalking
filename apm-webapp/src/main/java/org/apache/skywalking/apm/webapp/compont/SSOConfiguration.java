package org.apache.skywalking.apm.webapp.compont;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Liu-XinYuan
 */
@Component
@ConfigurationProperties(prefix = "sso")
public class SSOConfiguration {
    private String ssologin;
    private String redicturl;
    private String registerurl;
    private String clientId;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getRegisterurl() {
        return registerurl;
    }

    public void setRegisterurl(String registerurl) {
        this.registerurl = registerurl;
    }

    public String getRedicturl() {
        return redicturl;
    }

    public void setRedicturl(String redicturl) {
        this.redicturl = redicturl;
    }

    public String getSsologin() {
        return ssologin;
    }

    public void setSsologin(String ssologin) {
        this.ssologin = ssologin;
    }
}
