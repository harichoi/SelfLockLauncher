package kr.selfcontrol.selflocklauncher.detectApp;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.opengl.Visibility;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import kr.selfcontrol.selflocklauncher.launcher.PackageMgrActivity;

/**
 * Created by owner2 on 2016-04-02.
 */

public class BlockingService extends Service {

    public static LinearLayout mView;
    public static long blockedTime = System.currentTimeMillis();

    public static int STATE;

    public static final int INACTIVE=0;
    public static final int ACTIVE=1;
    public static final int BGCOLOR=0x77FFFFFF;
    public static final long DELAYTIME = 60000*5;

    static{
        STATE=INACTIVE;
    }

    @Override
    public IBinder onBind(Intent i) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        STATE=ACTIVE;

        mView = new LinearLayout(this);
        mView.setBackgroundColor(BGCOLOR);

        Button button = new Button(this);
        button.setText("Open SelfLock");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whiteing(false);
                Intent intent=new Intent(getApplicationContext(),PackageMgrActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        mView.addView(button);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        wm.addView(mView, params);
        whiteing(false);
    }
    
    public static void blocking() {
        if(mView != null) {
            mView.setVisibility(View.VISIBLE);
        }
        blockedTime = System.currentTimeMillis()+DELAYTIME;
    }

    public static void whiteing(boolean force) {
        if(mView != null && blockedTime < System.currentTimeMillis() || force) {
            mView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onDestroy() {
        STATE = INACTIVE;
        super.onDestroy();
        if(mView!=null){
            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            wm.removeView(mView);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mView.setBackgroundColor(BGCOLOR);
        return super.onStartCommand(intent, flags, startId);
    }
}