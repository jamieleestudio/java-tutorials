package utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 *
 * 日期工具类
 *
 * @author lixf
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateUtils {

    /**
     * 一天中的最后时间
     */
    public static final String DAY_END_TIME_STR = " 23:59:59";
    public static final String DAY_START_TIME_STR = " 00:00:00";


    public static long toMilliseconds(LocalDateTime localDateTime){
        return localDateTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
    }

    public static String buildEndTime(String date){
        return date + DAY_END_TIME_STR;
    }

    public static String buildStartTime(String date){
        return date + DAY_START_TIME_STR;
    }


    public static String formatDateTime(final LocalDateTime date) {
        return DateTimeFormatter.ofPattern(DateConstants.DATETIME_FORMAT).format(date);
    }

    public static String formatDate(final LocalDateTime date) {
        return DateTimeFormatter.ofPattern(DateConstants.DATE_FORMAT).format(date);
    }

    public static LocalDateTime parseDate(final String str) {
        return parse(str,DateConstants.DATE_FORMAT);
    }

    public static LocalDateTime parseDateTime(final String str) {
        return parse(str,DateConstants.DATETIME_FORMAT);
    }


    public static LocalDateTime date(Long time){
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
    }


    public static LocalDateTime parse(final String date, String patterns) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(patterns);
        return LocalDateTime.parse(date,dateTimeFormatter);
    }

    public static String formatDateTime(Long time,String patterns){
        DateTimeFormatter ftf = DateTimeFormatter.ofPattern(patterns);
        return ftf.format(date(time));
    }

    /**
     * 将Long类型的时间戳转换成String 类型的时间格式，时间格式为：yyyy-MM-dd HH:mm:ss
     */
    public static String formatDateTime(Long time){
        DateTimeFormatter ftf = DateTimeFormatter.ofPattern(DateConstants.DATETIME_FORMAT);
        return ftf.format(date(time));
    }

    /**
     * 将Long类型的时间戳转换成String 类型的时间格式，时间格式为：yyyy-MM-dd HH:mm:ss
     */
    public static String formatDate(Long time){
        DateTimeFormatter ftf = DateTimeFormatter.ofPattern(DateConstants.DATE_FORMAT);
        return ftf.format(date(time));
    }

    /**
     * 获取当前日期是星期几<br>
     *
     * @param millis 日期
     * @return 当前日期是星期几
     */
    public static String getWeekOfDate(long millis) {
        String[] weekDays = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0) {
            w = 0;
        }
        return weekDays[w];
    }

    public static boolean isLeap(int year){
        return (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
    }


    public static int getWeek(String dateStr){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = format.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(date == null){
            return -1;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.setTime(date);

        return calendar.get(Calendar.WEEK_OF_YEAR);
    }

    private static  final List<String> UNIT_LIST = List.of("天","小时","分","秒");;

    public static String formatDuring(long mss) {

        List<Long> valueList = new ArrayList<>();
        long days = mss / (1000 * 60 * 60 * 24);
        valueList.add(days);
        long hours = (mss % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        valueList.add(hours);
        long minutes = (mss % (1000 * 60 * 60)) / (1000 * 60);
        valueList.add(minutes);
        long seconds = (mss % (1000 * 60)) / 1000;
        valueList.add(seconds);
        StringBuilder stringBuilder = new StringBuilder();
        boolean flag = false;
        for(int i = 0;i<valueList.size();i++){
            long value = valueList.get(i);
            //如果找到数字大于0的,则表示开始拼接
            if(!flag && value > 0){
                flag  = true;
            }
            //开始拼接
            if(flag){
                stringBuilder.append(value).append(UNIT_LIST.get(i));
            }
        }

        if(mss < 1000){
            stringBuilder.append("1秒");
        }
        return stringBuilder.toString();
    }

}
