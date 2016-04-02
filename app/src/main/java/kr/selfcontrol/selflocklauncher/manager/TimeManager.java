package kr.selfcontrol.selflocklauncher.manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import kr.selfcontrol.selflocklauncher.model.TimeDetail;

/**
 * Created by owner2 on 2016-03-27.
 */

public class TimeManager {
    public static TimeManager instance = new TimeManager();
    public static TimeManager getInstance(){
        return instance;
    }
    public enum ReturnType{
        BLOCKED,
        WHITE,
        OTHERCASE
    }

    public boolean isValid(String exp){
        String[] temp = exp.split("\\|");
        if(temp.length!=5) return false;
        try {
            Integer.parseInt(temp[0]);
        } catch(Exception exc){
            return false;
        }
        return true;
    }

    public ReturnType isBlockedTime(List<TimeDetail> details){
        List<String> exps=new ArrayList<>();
        for(TimeDetail detail : details) {
            if(isValid(detail.name) && detail.isBlocked()) {
                exps.add(detail.name);
            }
        }
        Calendar today = Calendar.getInstance();
        ReturnType result;
        try {
            result = isBlockedTime(today.get(Calendar.YEAR), today.get(Calendar.MONTH) + 1, today.get(Calendar.DAY_OF_MONTH), today.get(Calendar.DAY_OF_WEEK),
                    today.get(Calendar.HOUR_OF_DAY), today.get(Calendar.MINUTE), today.get(Calendar.SECOND), exps);
        } catch(Exception exc){
            return ReturnType.BLOCKED;
        }
        return result;
    }
    public ReturnType isBlockedTime(int _year,int _month,int _date,int _dayOfweek,int _hour,int _minute,int _second,List<String> exps){
        Collections.sort(exps,new Compare());
        for(String exp : exps) {
            ReturnType result = isBlockedTime(_year, _month, _date, _dayOfweek, _hour, _minute, _second, exp);
            if(result!=ReturnType.OTHERCASE)
                return result;
        }
        return ReturnType.OTHERCASE;
    }
    public ReturnType isBlockedTime(int _year,int _month,int _date,int _dayOfweek,int _hour,int _minute,int _second,String exp){
        String[] temp = exp.split("\\|");
        String priority = temp[0];
        String wb = temp[1];
        String ymd = temp[2];
        String dayOfWeek = temp[3];
        String hms = temp[4];
        ReturnType defaultType;

        if(wb.trim().equals("w")){
            defaultType = ReturnType.WHITE;
        } else {
            defaultType = ReturnType.BLOCKED;
        }

        if(isYmd(_year, _month, _date, ymd) && isYmd(_hour,_minute,_second,hms) && isExpSuitable(_dayOfweek,dayOfWeek)){
            return defaultType;
        }
        return ReturnType.OTHERCASE;
    }
    private boolean isYmd(int _a1,int _a2,int _a3,String exp){
        if(exp.trim().equals("*")) return true;
        String[] ymdExps;
        if(exp.contains("/"))
            ymdExps = exp.split("/");
        else
            ymdExps = exp.split(":");
        String aExp = ymdExps[0];
        String bExp = ymdExps[1];
        String cExp = ymdExps[2];
        return isExpSuitable(_a1,aExp) && isExpSuitable(_a2,bExp) && isExpSuitable(_a3,cExp);
    }
    private boolean isExpSuitable(int number,String exp){
        String[] list=exp.split(",");
        for(String ex : list){
            if(ex.contains("-")){
                String[] temp = ex.split("-");
                if(Integer.parseInt(temp[0])<=number && number<=Integer.parseInt(temp[1])){
                    return true;
                }
            } else {
                if(ex.contains("*")){
                    return true;
                }
                if(Integer.parseInt(ex)==number) {
                    return true;
                }
            }
        }
        return false;
    }
    public String toString(){
        return "haha";
    }

    public static class Compare implements Comparator<String> {
        /**
         * 오름차순(ASC)
         */
        @Override
        public int compare(String arg0, String arg1) {
            int a0=Integer.parseInt(arg0.split("\\|")[0]);
            int a1=Integer.parseInt(arg1.split("\\|")[0]);
            if(a0<a1) return -1;
            if(a0==a1) {
                return arg0.compareTo(arg1);
            }
            return 1;
        }

    }

    public static class CompareTimeDetail implements Comparator<TimeDetail> {
        /**
         * 오름차순(ASC)
         */
        @Override
        public int compare(TimeDetail arg0, TimeDetail arg1) {
            int a0=Integer.parseInt(arg0.name.split("\\|")[0]);
            int a1=Integer.parseInt(arg1.name.split("\\|")[0]);
            if(a0<a1) return -1;
            if(a0==a1){
                return arg0.name.compareTo(arg1.name);
            }
            return 1;
        }

    }

}


/*
wb:우선순위|년/월/일|요일(일:1)|시:분:초
80|b|2016/2/8|*|*
90|w|*|1-5|10-18,1-3:*:*
100|b|*|*|0
 */

class Aa {
    public static void main(String []args) {
        isTrue(TimeManager.getInstance().isBlockedTime(2016,3,27,1,12,35,27,"80|b|2016/2/8|*|*"), TimeManager.ReturnType.OTHERCASE);
        isTrue(TimeManager.getInstance().isBlockedTime(2016,2,8,1,12,35,27,"80|b|2016/2/8|*|*"), TimeManager.ReturnType.BLOCKED);
        isTrue(TimeManager.getInstance().isBlockedTime(2016,2,8,1,12,35,27,"80|w|2016/2/8|*|*"), TimeManager.ReturnType.WHITE);
        isTrue(TimeManager.getInstance().isBlockedTime(2016,2,8,1,12,35,27,"80|b|*|*|*"), TimeManager.ReturnType.BLOCKED);
        isTrue(TimeManager.getInstance().isBlockedTime(2016,2,8,1,12,35,27,"80|b|2016/*/*|*|*"), TimeManager.ReturnType.BLOCKED);
        isTrue(TimeManager.getInstance().isBlockedTime(2016,5,8,1,12,35,27,"80|b|2016/*/*|*|*"), TimeManager.ReturnType.BLOCKED);
        isTrue(TimeManager.getInstance().isBlockedTime(2017,5,8,1,12,35,27,"80|b|2016/*/*|*|*"), TimeManager.ReturnType.OTHERCASE);
        isTrue(TimeManager.getInstance().isBlockedTime(2017,5,8,1,12,35,27,"80|b|2017/*/8|2|*"), TimeManager.ReturnType.OTHERCASE);
        isTrue(TimeManager.getInstance().isBlockedTime(2017,5,8,1,12,35,27,"80|b|2017/*/8|1|*"), TimeManager.ReturnType.BLOCKED);


        isTrue(TimeManager.getInstance().isBlockedTime(2014,5,8,1,12,35,27,"80|b|2015-2017/*/*|1|*"), TimeManager.ReturnType.OTHERCASE);
        isTrue(TimeManager.getInstance().isBlockedTime(2015,5,8,1,12,35,27,"80|b|2015-2017/*/*|1|*"), TimeManager.ReturnType.BLOCKED);
        isTrue(TimeManager.getInstance().isBlockedTime(2016,5,8,1,12,35,27,"80|b|2015-2017/*/*|1|*"), TimeManager.ReturnType.BLOCKED);
        isTrue(TimeManager.getInstance().isBlockedTime(2017,5,8,1,12,35,27,"80|b|2015-2017/*/*|1|*"), TimeManager.ReturnType.BLOCKED);
        isTrue(TimeManager.getInstance().isBlockedTime(2018,5,8,1,12,35,27,"80|b|2015-2017/*/*|1|*"), TimeManager.ReturnType.OTHERCASE);


        isTrue(TimeManager.getInstance().isBlockedTime(2015,5,8,1,12,35,27,"80|b|2016,2018-2019/*/*|1|*"), TimeManager.ReturnType.OTHERCASE);
        isTrue(TimeManager.getInstance().isBlockedTime(2016,5,8,1,12,35,27,"80|b|2016,2018-2019/*/*|1|*"), TimeManager.ReturnType.BLOCKED);
        isTrue(TimeManager.getInstance().isBlockedTime(2017,5,8,1,12,35,27,"80|b|2016,2018-2019/*/*|1|*"), TimeManager.ReturnType.OTHERCASE);
        isTrue(TimeManager.getInstance().isBlockedTime(2018,5,8,1,12,35,27,"80|b|2016,2018-2019/*/*|1|*"), TimeManager.ReturnType.BLOCKED);
        isTrue(TimeManager.getInstance().isBlockedTime(2019,5,9,1,12,35,27,"80|b|2016,2018-2019/*/*|1|*"), TimeManager.ReturnType.BLOCKED);
        isTrue(TimeManager.getInstance().isBlockedTime(2020,5,8,1,12,35,27,"80|b|2016,2018-2019/*/*|1|*"), TimeManager.ReturnType.OTHERCASE);


        isTrue(TimeManager.getInstance().isBlockedTime(2015,5,3,1,11,12,0,"80|b|2015/5/3|1|11:12:*"), TimeManager.ReturnType.BLOCKED);
        isTrue(TimeManager.getInstance().isBlockedTime(2015,5,3,1,11,11,0,"80|b|2015/5/3|1|11:12:*"), TimeManager.ReturnType.OTHERCASE);

        isTrue(TimeManager.getInstance().isBlockedTime(2015,5,3,1,11,12,0,"80|b|2015/5/3|1-3,7|11:12:*"), TimeManager.ReturnType.BLOCKED);
        isTrue(TimeManager.getInstance().isBlockedTime(2015,5,3,2,11,12,0,"80|b|2015/5/3|1-3,7|11:12:*"), TimeManager.ReturnType.BLOCKED);
        isTrue(TimeManager.getInstance().isBlockedTime(2015,5,3,3,11,12,0,"80|b|2015/5/3|1-3,7|11:12:*"), TimeManager.ReturnType.BLOCKED);
        isTrue(TimeManager.getInstance().isBlockedTime(2015,5,3,4,11,12,0,"80|b|2015/5/3|1-3,7|11:12:*"), TimeManager.ReturnType.OTHERCASE);
        isTrue(TimeManager.getInstance().isBlockedTime(2015,5,3,5,11,12,0,"80|b|2015/5/3|1-3,7|11:12:*"), TimeManager.ReturnType.OTHERCASE);
        isTrue(TimeManager.getInstance().isBlockedTime(2015, 5, 3, 6, 11, 12, 0, "80|b|2015/5/3|1-3,7|11:12:*"), TimeManager.ReturnType.OTHERCASE);
        isTrue(TimeManager.getInstance().isBlockedTime(2015, 5, 3, 7, 11, 12, 0, "80|b|2015/5/3|1-3,7|11:12:*"), TimeManager.ReturnType.BLOCKED);

        List<String> list=new ArrayList<>();
        list.add("80|b|2016/2/8|*|*");
        list.add("80|b|2016/3/15|*|*");
        list.add("90|w|*|1-5|10-18:*:*");
        list.add("100|b|*|*|*");
        Collections.sort(list, new TimeManager.Compare());
        System.out.println("list test");

        isTrue(TimeManager.getInstance().isBlockedTime(2015,5,3,7,11,12,0,list),TimeManager.ReturnType.BLOCKED);
        isTrue(TimeManager.getInstance().isBlockedTime(2015,5,3,5,11,12,0,list),TimeManager.ReturnType.WHITE);
        isTrue(TimeManager.getInstance().isBlockedTime(2016,2,8,4,11,12,0,list),TimeManager.ReturnType.BLOCKED);
        isTrue(TimeManager.getInstance().isBlockedTime(2016,8,8,4,11,12,0,list),TimeManager.ReturnType.WHITE);
        isTrue(TimeManager.getInstance().isBlockedTime(2016,3,15,4,11,12,0,list),TimeManager.ReturnType.BLOCKED);

    }
    public static void isTrue(TimeManager.ReturnType a, TimeManager.ReturnType b){
        if(a==b)
            System.out.println("맞음");
        else
            System.out.println("틀림");
    }
}
/*
wb:우선순위|년/월/일|요일(일:1)|시:분:초
80|b|2016/2/8|*|*
90|w|*|1-5|10-18,1-3:*:*
100|b|*|*|0
 */
