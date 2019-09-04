package com.example.chatui.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TimeUtil {
    private Calendar calendars;
    //源时间字符串
    private String time;
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int second;

    public String getCurrentTime(){
        calendars=Calendar.getInstance();
        String year = String.valueOf(calendars.get(Calendar.YEAR));

        String month = String.valueOf(calendars.get(Calendar.MONTH));

        String day = String.valueOf(calendars.get(Calendar.DATE));

        String hour = String.valueOf(calendars.get(Calendar.HOUR_OF_DAY));

        String minute = String.valueOf(calendars.get(Calendar.MINUTE));

        String second = String.valueOf(calendars.get(Calendar.SECOND));

        return (year+'_' +
                month+'_'+
                day+'_'+
                hour+'_'+
                minute+'_'+
                second);
    }

    private ArrayList<Integer> split(String time){
        ArrayList<Integer> tmp=new ArrayList<>();
        String[] splitTime=time.split("_");
        for(String string:splitTime){
            try{
                tmp.add(Integer.parseInt(string));
            }catch (NumberFormatException e){
                e.printStackTrace();
            }
        }
        return tmp;
    }

    public int getYear(String time) {
        ArrayList<Integer> tmp=split(time);
        return tmp.get(0);
    }

    public int getMonth(String time) {
        ArrayList<Integer> tmp=split(time);
        return tmp.get(1);
    }

    public int getDay(String time) {
        ArrayList<Integer> tmp=split(time);
        return tmp.get(2);
    }

    public int getHour(String time) {
        ArrayList<Integer> tmp=split(time);
        return tmp.get(3);
    }

    public int getMinute(String time) {
        ArrayList<Integer> tmp=split(time);
        return tmp.get(4);
    }

    public int getSecond(String time) {
        ArrayList<Integer> tmp=split(time);
        return tmp.get(5);
    }
}
