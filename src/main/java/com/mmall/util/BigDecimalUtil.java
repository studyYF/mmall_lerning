package com.mmall.util;

import java.math.BigDecimal;

/**
 * Created by yangf on 2018/2/28.
 */
public class BigDecimalUtil {

    // 把构造方法私有化是为了防止外界创建这个类的实例，该类只提供类方法，不需要创建实例
    private BigDecimalUtil() {

    }

    /**
     * 加法
     * @param a1 参数1
     * @param a2 参数2
     * @return 结果
     */
    public static BigDecimal add(Double a1, Double a2) {
        BigDecimal b1 = new BigDecimal(Double.toString(a1));
        BigDecimal b2 = new BigDecimal(Double.toString(a2));
        return b1.add(b2);
    }


    /**
     * 减法
     * @param a1 减数
     * @param a2 被减数
     * @return 结果
     */
    public static BigDecimal sub(Double a1, Double a2) {
        BigDecimal b1 = new BigDecimal(Double.toString(a1));
        BigDecimal b2 = new BigDecimal(Double.toString(a2));
        return b1.subtract(b2);
    }

    /**
     * 乘法
     * @param a1 乘数1
     * @param a2 乘数2
     * @return 结果
     */
    public static BigDecimal multiply(Double a1, Double a2) {
        BigDecimal b1 = new BigDecimal(Double.toString(a1));
        BigDecimal b2 = new BigDecimal(Double.toString(a2));
        return b1.multiply(b2);
    }

    /**
     * 除法
     * @param a1 除数
     * @param a2 被除数
     * @return 结果
     */
    public static  BigDecimal div(Double a1, Double a2) {
        BigDecimal b1 = new BigDecimal(Double.toString(a1));
        BigDecimal b2 = new BigDecimal(Double.toString(a2));
        return  b1.divide(b2,2,BigDecimal.ROUND_HALF_UP);
        //保留两位小数，并且四舍五入
    }

}
