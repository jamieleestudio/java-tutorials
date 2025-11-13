package utils;

/**
 *
 * 日期变量
 *
 * @author lixf
 */
public interface DateConstants {

    String DATE_FORMAT = "yyyy-MM-dd";

    String MONTH_FORMAT = "yyyy-MM";
    String DATE_FORMAT_CN = "yyyy年MM月dd日";
    String MONTH_FORMAT_CN = "yyyy年MM月";

    String DAY_FORMAT_CN = "MM月dd日";

    String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    String DATETIME_MINUTE_FORMAT = "yyyy-MM-dd HH:mm";

    String SIMPLE_DATE_FORMAT = "MM-dd";

    String TIME_FORMAT = "HH:mm";

    String TIME_FORMAT_ALL = "HH:mm:ss";

    String HOUR_FORMAT = "H";

    String MINUTE_FORMAT = "m";

    /**
     * 一天有多少秒，24小时 X 每小时60分钟 X 每分钟60秒
     */
    int DAY_SECONDS = 60 * 60 * 24;

}
