package utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class DateUtils2 {


    public static LocalDateTime date(Long time){
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
    }
    private static final long YEAR_MILLISECOND_365 = 31536000000L;
    private static final long YEAR_MILLISECOND_366 = 31622400000L;

    public static void checkQuery(Long startTime,Long endTime){
        if(startTime == null || endTime == null){
            throw new  RuntimeException("exception");
        }
        if(startTime > endTime){
            throw new  RuntimeException("exception");
        }
        long timeDifference  = endTime - startTime;
        if(timeDifference <= YEAR_MILLISECOND_365){
            return;
        }
        if(timeDifference > YEAR_MILLISECOND_366){
            throw new  RuntimeException("exception");
        }

        LocalDateTime startTimeL = DateUtils.date(startTime);
        LocalDateTime endTimeL = DateUtils.date(endTime);
        var startTimeYear = startTimeL.getYear();
        var endTimeYear = endTimeL.getYear();

        if(DateUtils.isLeap(startTimeYear)){
            if(LocalDate.parse(startTimeYear+"-02-29", DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay().isBefore(startTimeL)){
                throw new  RuntimeException("exception");
            }
        }else if(DateUtils.isLeap(endTimeYear)){
            if(LocalDate.parse(endTimeYear+"-02-29", DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay().isAfter(endTimeL)){
                throw new  RuntimeException("exception");
            }
        }else{
            throw new  RuntimeException("exception");
        }
    }


    public static void main(String[] args) {

//        Date startTime = new Date(1646064000000l);
//        Date endTime = new Date(1677600000000l);
//        System.out.println(minusTime(startTime,endTime,Calendar.MONTH));

        checkQuery(DateUtils.toMilliseconds(LocalDateTime.of(2023,1,1,0,0)),
                   DateUtils.toMilliseconds(LocalDateTime.of(2024,1,1,0,0)));


        checkQuery(DateUtils.toMilliseconds(LocalDateTime.of(2024,2,29,0,0)),
                DateUtils.toMilliseconds(LocalDateTime.of(2025,3,1,0,0)));

    }


    public static boolean isLeap(int year){
        return (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
    }


    public static int minusTime(Date smallDate, Date bigDate, int type) {
        if (smallDate == null || bigDate == null) {
            return Integer.MAX_VALUE;
        }
        Calendar smallCalendar = Calendar.getInstance();
        smallCalendar.setTime(smallDate);
        Calendar bigCalendar = Calendar.getInstance();
        bigCalendar.setTime(bigDate);
        smallCalendar.set(
                smallCalendar.get(Calendar.YEAR),
                smallCalendar.get(Calendar.MONTH),
                smallCalendar.get(Calendar.DAY_OF_MONTH),
                0,
                0,
                0);
        smallCalendar.set(Calendar.MILLISECOND, 0);
        bigCalendar.set(
                bigCalendar.get(Calendar.YEAR),
                bigCalendar.get(Calendar.MONTH),
                bigCalendar.get(Calendar.DAY_OF_MONTH),
                0,
                0,
                0);
        bigCalendar.set(Calendar.MILLISECOND, 0);
        long smallTime = smallCalendar.getTimeInMillis();
        long bigTime = bigCalendar.getTimeInMillis();
        int bigTotalMonth =
                (bigCalendar.get(Calendar.YEAR) - 1) * 12 + bigCalendar.get(Calendar.MONTH);
        int smallTotalMonth =
                (smallCalendar.get(Calendar.YEAR) - 1) * 12 + smallCalendar.get(Calendar.MONTH);
        switch (type) {
            case Calendar.YEAR:
                return bigCalendar.get(Calendar.YEAR) - smallCalendar.get(Calendar.YEAR);
            case Calendar.MONTH:
                return bigTotalMonth - smallTotalMonth;
            case Calendar.DATE:
                return (int) ((bigTime - smallTime) / (1000L * 3600 * 24)) + 1;
            case Calendar.HOUR:
                return (int) ((bigTime - smallTime) / (1000L * 3600));
            case Calendar.MINUTE:
                return (int) ((bigTime - smallTime) / (1000L * 60));
            default:
                return Integer.MAX_VALUE;
        }
    }

}
