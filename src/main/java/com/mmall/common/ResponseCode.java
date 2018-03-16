package com.mmall.common;

/**
 * Created by yangfan on 2017/9/6.
 */
public enum  ResponseCode {
    SUCCESS(0,"SUCCESS"),
    NEED_MANAGER_AUTHORITY(20,"NEED_MANAGER_AUTHORITY"),
    ERROR(1,"ERROR"),
    NEED_LOGIN(10,"NEED_LOGIN"),
    ILLEGAL_ARGUMENT(2,"ILLEGAL_ARGUMENT");

    private final int code;
    private final String desc;

    ResponseCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public  int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
