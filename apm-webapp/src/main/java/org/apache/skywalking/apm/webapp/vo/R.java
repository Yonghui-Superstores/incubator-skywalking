package org.apache.skywalking.apm.webapp.vo;

import lombok.Data;

/**
 * @author Liu-XinYuan
 */
@Data
public class R {

    private int code;
    private String message;
    private Object projects;
    private String env;

    public R(int code, String message, Object projects) {
        this.code = code;
        this.message = message;
        this.projects = projects;
    }
}
