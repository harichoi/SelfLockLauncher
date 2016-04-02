package kr.selfcontrol.selflocklauncher.model;

import android.graphics.drawable.Drawable;

import kr.selfcontrol.selflocklauncher.vo.PackageVo;

/**
 * Created by owner on 2015-12-24.
 */
public class AppDetail {
    public PackageVo packageVo;
    public long unLockTime;
    public Drawable icon;
    public CharSequence label;
    public String name;
    public boolean isBlocked(){
        if(packageVo==null){
            return false;
        }
        return packageVo.isBlocked();
    }
    public boolean isUnlocking(){
        if(packageVo==null){
            return false;
        }
        return packageVo.isUnlocking();
    }

}
