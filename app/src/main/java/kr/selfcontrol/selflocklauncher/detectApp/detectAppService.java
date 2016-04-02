package kr.selfcontrol.selflocklauncher.detectApp;

import android.accessibilityservice.AccessibilityService;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

import kr.selfcontrol.selflocklauncher.MainActivity;
import kr.selfcontrol.selflocklauncher.dao.SelfLockDao;
import kr.selfcontrol.selflocklauncher.manager.TimeManager;
import kr.selfcontrol.selflocklauncher.model.TimeDetail;
import kr.selfcontrol.selflocklauncher.vo.PackageVo;

/**
 * Created by owner on 2015-12-19.
 */
public class detectAppService extends AccessibilityService
{
    public boolean isBlocked(SelfLockDao lockDao){
        long dateUnlock=lockDao.getSettingLong(SelfLockDao.SETTING_DATE_UNLOCK);
        if(dateUnlock==0 || dateUnlock>System.currentTimeMillis()) {
            return true;
        }
        return false;
    }
    public boolean isUnlocking(SelfLockDao lockDao){
        long dateUnlock=lockDao.getSettingLong(SelfLockDao.SETTING_DATE_UNLOCK);
        if(dateUnlock!=0){
            return true;
        }
        return false;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        boolean shouldBlock=false;
        try {
            if(BlockingService.STATE == BlockingService.INACTIVE) {
                Intent i = new Intent(this, BlockingService.class);
                startService(i);
            }
        } catch(Exception exc){}
        final ActivityManager activityManager=(ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningTaskInfo> recentTasks=activityManager.getRunningTasks(Integer.MAX_VALUE);
        Log.d("event",event.getClassName()+"");
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if(event.getPackageName().toString().equals("kr.selfcontrol.selflocklauncher")){
                BlockingService.whiteing(false);
                return;
            }
            SelfLockDao.createInstance(this);
            SelfLockDao dao=SelfLockDao.getInstance();

            List<PackageVo> blockApps=dao.readPackageList();
            long debugMode=dao.getSettingLong(dao.SETTING_CHECK_ACTIVITY);
            if(debugMode==1) {
                toastShow(event.getClassName()+"");
            }
            List<TimeDetail> times=dao.getTimeing();

            boolean shouldAllow=false;
            TimeManager.ReturnType resultType = TimeManager.getInstance().isBlockedTime(times);
            System.out.println(resultType.name());
            if(resultType == TimeManager.ReturnType.WHITE){
                shouldAllow=true;
            }
            if(blockApps!=null && isBlocked(dao) && !shouldAllow) {
                for (PackageVo app : blockApps) {
                    if (app.isBlocked() && (event.getPackageName().equals(app.key) || event.getClassName().equals(app.key))) {

                        shouldBlock=true;
                      /*  for(int i=recentTasks.size()-1 ; i>=0 ; i--){
                            Log.d("recent_" + i, recentTasks.get(i).baseActivity.toShortString());
                            if(!recentTasks.get(i).baseActivity.toShortString().contains(app.key) && !recentTasks.get(i).baseActivity.toShortString().contains("kr.selfcontrol.selflocklauncher") ) {
                                activityManager.moveTaskToFront(recentTasks.get(i).id, ActivityManager.MOVE_TASK_WITH_HOME);
                                Log.d("Recent",recentTasks.get(i).baseActivity.toShortString());
                                //break;
                            }
                        }
                        boolean shouldStart=true;
                        for(int i=0 ; i<recentTasks.size() ; i++){
                            if(recentTasks.get(i).baseActivity.toShortString().contains("kr.selfcontrol.selflocklauncher.MainActivity") ) {
                                activityManager.moveTaskToFront(recentTasks.get(i).id, ActivityManager.MOVE_TASK_WITH_HOME);
                                Log.d("Recent", "already");
                                shouldStart=false;
                                break;
                            }
                        }
                        if(shouldStart) {
                            //Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            //startActivity(intent);
                        }
                        */
                        break;
                    }
                }
            }
            dao.close();
        }
        if(shouldBlock) {
            BlockingService.blocking();
        } else {
            BlockingService.whiteing(false);
        }

    }

    public void toastShow(String str){
        Toast.makeText(getApplicationContext(), str,
                Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onInterrupt(){

    }
}
