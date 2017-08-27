package com.guardias.yornel.gpslocation.util;/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.joda.time.Seconds;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Locale;

/**
 *
 * @author Yornel Marval
 */
public class DateUtil {
    
    public static String getMonthName(int month){
        Calendar cal = Calendar.getInstance();
        // Calendar numbers months from 0
        cal.set(Calendar.MONTH, month - 1);
        return cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
    }
    
    public static String getFechaConMes(DateTime dateTime) {
        String fecha = dateTime.getDayOfMonth() + " de " 
                + getMonthName(dateTime.getMonthOfYear()) 
                + " " + dateTime.getYear();
        return fecha;
    }
    
    public static String getFechaConMesSinAno(DateTime dateTime) {
        String fecha = dateTime.getDayOfMonth() + " de " 
                + getMonthName(dateTime.getMonthOfYear());
        return fecha;
    }
    
    public static String getFechaConMes(Date date) {
        DateTime dateTime = new DateTime(date.getTime());
        String fecha = dateTime.getDayOfMonth() + " de " 
                + getMonthName(dateTime.getMonthOfYear()) 
                + " " + dateTime.getYear();
        return fecha;
    }
    
    public static String getFechaConMes(Timestamp timestamp) {
        DateTime dateTime = new DateTime(timestamp.getTime());
        String fecha = dateTime.getDayOfMonth() + " de " 
                + getMonthName(dateTime.getMonthOfYear()) 
                + " " + dateTime.getYear();
        return fecha;
    }
    
    public static String getFechaConMesYHora(DateTime dateTime) {
        String fecha = dateTime.getDayOfMonth() + " de " 
                + getMonthName(dateTime.getMonthOfYear()) 
                + " " + dateTime.getYear() + " a las "
                + dateTime.toString("HH:mm:ss");
        return fecha;
    }

    public static String getHora(DateTime dateTime) {
        String fecha = "a las "+dateTime.toString("HH:mm:ss");
        return fecha;
    }

    public static String getHora(Long longDate) {
        DateTime dateTime = new DateTime(longDate);
        String date = "a las "+dateTime.toString("HH:mm");
        return date;
    }
    
    public static String getFechaConMesYHora(Timestamp timestamp) {
        DateTime dateTime = new DateTime(timestamp.getTime());
        String fecha = dateTime.getDayOfMonth() + " de " 
                + getMonthName(dateTime.getMonthOfYear()) 
                + " " + dateTime.getYear() + " a las " 
                + dateTime.toString("HH:mm:ss");
        return fecha;
    }
    public static String getFechaCorta(Timestamp timestamp) {
        DateTime dateTime = new DateTime(timestamp.getTime());
        return dateTime.toString("dd/MM/yyyy");
    }
    
    public static String getFechaCorta(Date date) {
        DateTime dateTime = new DateTime(date.getTime());
        return dateTime.toString("dd/MM/yyyy");
    }
    
    public static String getFechaCorta(DateTime dateTime) {
        return dateTime.toString("dd/MM/yyyy");
    }

    public static long differenceBetweenMinutes(Long time1, Long time2) {
        DateTime dateTime1 = new DateTime(time1);
        DateTime dateTime2 = new DateTime(time2);
        Minutes minutes = Minutes.minutesBetween(dateTime2, dateTime1);
        return Long.valueOf(minutes.getMinutes());
    }

    public static long differenceBetweenSeconds(Long time1, Long time2) {
        DateTime dateTime1 = new DateTime(time1);
        DateTime dateTime2 = new DateTime(time2);
        Seconds seconds = Seconds.secondsBetween(dateTime2, dateTime1);
        return Long.valueOf(seconds.getSeconds());
    }
}
