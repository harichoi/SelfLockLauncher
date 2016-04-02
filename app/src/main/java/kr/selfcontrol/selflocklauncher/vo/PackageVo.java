package kr.selfcontrol.selflocklauncher.vo;

import kr.selfcontrol.selflocklauncher.util.SelfControlUtil;

/**
 * Created by owner2 on 2015-12-29.
 */
public class PackageVo {
    public String key;
    public long dateUnlock;

    public PackageVo(){

    }

    public PackageVo(String key,long dateUnlock){
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
