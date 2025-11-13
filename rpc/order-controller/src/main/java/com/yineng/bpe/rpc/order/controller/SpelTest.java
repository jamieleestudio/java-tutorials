package com.yineng.bpe.rpc.order.controller;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SpelTest {

    public static void main(String[] args) throws NoSuchMethodException {

        EvaluationContext context = SimpleEvaluationContext.forReadOnlyDataBinding().build();
        Method method = SpelTest.class.getDeclaredMethod("sum", Integer[].class);
        context.setVariable("sum", method);

        Method methodDateDiff = SpelTest.class.getDeclaredMethod("dateDiff", String.class);
        context.setVariable("dateDiff", methodDateDiff);

        ExpressionParser parser = new SpelExpressionParser();
//        Expression exp = parser.parseExpression("#sum(#sum(1,2,4)+5,10,#dateDiff('2024-07-19'))");
        Expression exp = parser.parseExpression("2023-07-09 > 2024-08-09");
//        Expression exp = parser.parseExpression("1  >  1");
//        System.out.println(exp.getValue());
//        Expression exp = parser.parseExpression("#this");
        System.out.println(exp.getValue(context));

    }

//    public static Integer dateDiff(String dateStr){
//        return null;
//    }

    public static int dateDiff(String dateStr){
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

    public  static int sum(Integer ...a){
        int b = 0;
        for (Integer i : a) {
            if(i != null){
                b+=i;
            }
        }
        return b;
    }


}
