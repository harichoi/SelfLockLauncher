package kr.selfcontrol.selflocklauncher.model;

import android.graphics.drawable.Drawable;

import kr.selfcontrol.selflocklauncher.vo.PackageVo;

/**
 * Created by owner2 on 2016-03-27.
 */
public class TimeDetail {
    public CharSequence label;
    public String name;
    public long dateUnlock;
    public long dateAffect;
    public TimeDetail(String name){
        this.name = name;
    }

    public boolean isAffecting(){
        if(dateAffect>System.currentTimeMillis()) return true;
        return false;
    }
    public boolean isBlocked(){
        if(isAffecting()) return false;
        if(dateUnlock==0 || dateUnlock>System.currentTimeMillis()) {
            return true;
        }
        return false;
    }
    public boolean isUnlocking(){
        if(dateUnlock>System.currentTimeMillis()){
            return true;
        }
        return false;
    }

    public static class Builder{
        TimeDetail timeDetail;
        public Builder(String name){
            timeDetail=new TimeDetail(name);
        }
        public Builder setDateUnlock(long dateUnlock){
            timeDetail.dateUnlock = dateUnlock;
            return this;
        }
        public Builder setDateAffect(long dateAffect){
            timeDetail.dateAffect = dateAffect;
            return this;
        }
        public TimeDetail build(){
            return timeDetail;
        }

    }
}