package org.apache.skywalking.apm.webapp.vo;

import lombok.Data;

/**
 * @author Liu-XinYuan
 */
@Data
public class R {

    private int code;
    private String message;
    private Object data;
    private String env;

    public R(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}