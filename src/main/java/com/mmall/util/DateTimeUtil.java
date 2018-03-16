package com.mmall.util;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

/**
 * Created by yangfan on 2017/11/16.
 */
public class DateTimeUtil {

    /**
     * 标准日期格式
     */
    private static final String STANDFORMAT = "yyyy-MM-dd HH:mm:ss";
    /**
     * 根据字符串转成对应的日期格式
     * @param dateTimeString 字符串
     * @param dateFormatString 日期格式
     */
    public static Date stringToDate(String dateTimeString, String dateFormatString) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(dateFormatString);
        DateTime dateTime = dateTimeFormatter.parseDateTime(dateTimeString);
        return dateTime.toDate();
    }

    /**
     * 日期转成字符串
     * @param date 日期
     * @param dateFormatString 日期格式
     * @return 字符串
     */
    public static String dateToString(Date date, String dateFormatString) {
        if (date == null) {
            return StringUtils.EMPTY;
        }
        DateTime dateTime = new DateTime(date);
        return dateTime.toString(dateFormatString);
    }
    /**
     * 根据字符串转成对应的日期格式
     * @param dateTimeString 字符串
     */
    public static Date stringToDate(String dateTimeString) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(STANDFORMAT);
        DateTime dateTime = dateTimeFormatter.parseDateTime(dateTimeString);
        return dateTime.toDate();
    }

    /**
     * 日期转成字符串
     * @param date 日期
     * @return 字符串
     */
    public static String dateToString(Date date) {
        if (date == null) {
            return StringUtils.EMPTY;
        }
        DateTime dateTime = new DateTime(date);
        return dateTime.toString(STANDFORMAT);
    }
}
