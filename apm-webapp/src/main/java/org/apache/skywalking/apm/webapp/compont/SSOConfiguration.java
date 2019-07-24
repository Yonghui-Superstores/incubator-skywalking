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
