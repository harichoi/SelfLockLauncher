package kr.selfcontrol.selflocklauncher.vo;

/**
 * Created by owner2 on 2015-12-29.
 */
public class ActivityVo {

    public String pack;
    public String key;
    public long dateUnlock;

    public ActivityVo(){

    }

    public ActivityVo(String pack,String key,long dateUnlock){
        this.pack=pack;
        this.key= key;
        this.dateUnlock=dateUnlock;
    }
    public boolean isBlocked(){
        if(dateUnlock==0 || dateUnlock>System.currentTimeMillis()) {
            return true;
        }
        return false;
    }
    public boolean isUnlocking(){
        if(dateUnlock!=0){
            return true;
        }
        return false;
    }
}
