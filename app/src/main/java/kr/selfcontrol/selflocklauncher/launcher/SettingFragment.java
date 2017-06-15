package kr.selfcontrol.selflocklauncher.launcher;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import kr.selfcontrol.selflocklauncher.R;
import kr.selfcontrol.selflocklauncher.dao.SelfLockDao;
import kr.selfcontrol.selflocklauncher.picker.HorizontalPicker;
import kr.selfcontrol.selflocklauncher.util.SelfControlUtil;

/**
 * Created by owner on 2015-12-18.
 */
public class SettingFragment extends Fragment {

    boolean needTimer;
    private Timer timer;
    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {

            if (!needTimer) {
                timer.cancel();
                timer.purge();
                timer = null;
            }
            try {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        update();
                    }
                });
            } catch (Exception exc) {
                timer.cancel();
                timer.purge();
                timer = null;
            }
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    SelfLockDao lockDao = SelfLockDao.getInstance();
    private PackageManager manager;
    private ListView list;
    private LayoutInflater inflater;
    BaseAdapter adapter;
    View view;
    TextView keyView;
    TextView valueView, delayView, explainView;
    Button blockButton, editButton, backupButton, restoreButton;
    ToggleButton turnButton, toggleButton;

    public String getTimeToString(long time) {
        time = time / 1000;
        StringBuilder sb = new StringBuilder();
        if ((int) (time / 3600 / 24) > 0) {
            sb.append((int) time / 3600 / 24 + "days ");
        }
        time = time % (3600 * 24);
        if ((int) (time / 3600) > 0) {
            sb.append((int) time / 3600 + "hours ");
        }

        time = time % (3600);
        if ((int) (time / 60) > 0) {
            sb.append((int) time / 60 + "minitues ");
        }

        time = time % (60);
        if ((int) (time) > 0) {
            sb.append((int) time + "seconds ");
        }
        return sb.toString();
    }

    public void update() {
        valueView.setText("Time To Lock App : " + getTimeToString(lockDao.getSettingLong(SelfLockDao.SETTING_APP_LOCK_TIME)) + "");
        delayView.setText("Time To Lock Permission : " + getTimeToString(lockDao.getSettingLong(SelfLockDao.SETTING_DELAY)) + "");
        if (!isBlocked()) {
            needTimer = false;
            turnButton.setEnabled(true);
            editButton.setEnabled(true);
            restoreButton.setEnabled(true);
            blockButton.setText("Block");
            view.setBackgroundColor(0xFFABABFF);
        } else if (!isUnlocking()) {
            needTimer = false;
            turnButton.setEnabled(false);
            editButton.setEnabled(false);
            restoreButton.setEnabled(false);
            blockButton.setText("UnBlock");
            view.setBackgroundColor(0xFFFFABAB);
        } else {
            editButton.setEnabled(false);
            turnButton.setEnabled(false);
            restoreButton.setEnabled(false);
            needTimer = true;
            view.setBackgroundColor(0xFFABFFAB);
            blockButton.setText("Cancel");
            explainView.setText(getTimeToString((lockDao.getSettingLong("dateunlock") - System.currentTimeMillis())) + "초 남음");
        }
        long debugMode = lockDao.getSettingLong(lockDao.SETTING_CHECK_ACTIVITY);
        if (debugMode == 0) {
            toggleButton.setChecked(false);
        } else {
            toggleButton.setChecked(true);
        }

        if (isAccessibilityEnabled(getActivity().getApplicationContext())) {
            turnButton.setChecked(true);
        } else {
            turnButton.setChecked(false);
        }

        if (needTimer && timer == null) {
            try {
                timer = new Timer();
                timer.schedule(timerTask, 0, 1000);
            } catch (Exception exc) {
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.inflater = inflater;

        view = inflater.inflate(R.layout.package_setting, null);

        keyView = (TextView) view.findViewById(R.id.key);
        valueView = (TextView) view.findViewById(R.id.value);
        delayView = (TextView) view.findViewById(R.id.delay);
        explainView = (TextView) view.findViewById(R.id.explain);
        blockButton = (Button) view.findViewById(R.id.lock_button);
        backupButton = (Button) view.findViewById(R.id.backup_button);
        restoreButton = (Button) view.findViewById(R.id.restore_button);
        editButton = (Button) view.findViewById(R.id.edit_button);
        toggleButton = (ToggleButton) view.findViewById(R.id.toggle);
        turnButton = (ToggleButton) view.findViewById(R.id.turn_on_off);
        blockButton.setOnClickListener(buttonClickListener);
        backupButton.setOnClickListener(buttonClickListener);
        restoreButton.setOnClickListener(buttonClickListener);
        editButton.setOnClickListener(buttonClickListener);
        toggleButton.setOnClickListener(buttonClickListener);
        turnButton.setOnClickListener(buttonClickListener);

        update();
        return view;
    }

    public void showDialog() {
        final String[] options = new String[]{"30seconds", "1minute", "5minutes", "10minutes", "30minutes", "1hour", "2hours", "4hours", "6hours", "9hours", "12hours", "1day", "2days", "5days"};
        final long[] optionValues = new long[]{30000, 60000, 5 * 60000, 10 * 60000, 30 * 60000, 60 * 60000, 2 * 60 * 60000, 4 * 60 * 60000, 6 * 60 * 60000, 9 * 60 * 60000, 12 * 60 * 60000, 24 * 60 * 60000, 2 * 24 * 60 * 60000, 5 * 24 * 60 * 60000};
        final View innerView = inflater.inflate(R.layout.package_setting_time_dialog, null);
        AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
        ab.setTitle("setting");
        ab.setView(innerView);
        final HorizontalPicker hpValue = (HorizontalPicker) innerView.findViewById(R.id.value);
        hpValue.setValues(options);
        for (int i = 0; i < optionValues.length; i++) {
            if (optionValues[i] == lockDao.getSettingLong(SelfLockDao.SETTING_APP_LOCK_TIME)) {
                hpValue.setSelectedItem(i);
            }
        }
        final HorizontalPicker hpDelay = (HorizontalPicker) innerView.findViewById(R.id.delay);
        for (int i = 0; i < optionValues.length; i++) {
            if (optionValues[i] == lockDao.getSettingLong(SelfLockDao.SETTING_DELAY)) {
                hpDelay.setSelectedItem(i);
            }
        }
        hpDelay.setValues(options);

//time to delay setting permission

        ab.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                long value = optionValues[hpValue.getSelectedItem()];
                long delay = optionValues[hpDelay.getSelectedItem()];
                lockDao.setSetting(SelfLockDao.SETTING_APP_LOCK_TIME, value);
                lockDao.setSetting(SelfLockDao.SETTING_DELAY, delay);
                update();
            }
        });

        ab.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                arg0.cancel();
            }
        });

        ab.create().show();
    }

    public boolean isBlocked() {
        long dateUnlock = lockDao.getSettingLong(SelfLockDao.SETTING_DATE_UNLOCK);
        if (dateUnlock == 0 || dateUnlock > System.currentTimeMillis()) {
            return true;
        }
        return false;
    }

    public boolean isUnlocking() {
        long dateUnlock = lockDao.getSettingLong(SelfLockDao.SETTING_DATE_UNLOCK);
        if (dateUnlock != 0) {
            return true;
        }
        return false;
    }

    public static boolean isAccessibilityEnabled(Context context) {

        AccessibilityManager am = (AccessibilityManager) context
                .getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> runningServices = am
                .getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK);
        for (AccessibilityServiceInfo service : runningServices) {
            //        Log.d("ddddd",service.getId());
            if (service.getId().contains("kr.selfcontrol")) {
                return true;
            }
        }

        return false;
    }

    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.lock_button) {
                manager = getActivity().getPackageManager();
                long dateUnlock = lockDao.getSettingLong("dateunlock");
                if (!isBlocked()) {
                    lockDao.setSetting(SelfLockDao.SETTING_DATE_UNLOCK, 0);
                } else if (isUnlocking()) {
                    lockDao.setSetting(SelfLockDao.SETTING_DATE_UNLOCK, 0);
                } else {
                    lockDao.setSetting(SelfLockDao.SETTING_DATE_UNLOCK, System.currentTimeMillis() + lockDao.getSettingLong(SelfLockDao.SETTING_DELAY));
                }
                update();
            } else if (v.getId() == R.id.backup_button) {
                exportDB();
            } else if (v.getId() == R.id.restore_button) {
                importDB();
            } else if (v.getId() == R.id.edit_button) {
                showDialog();
            } else if (v.getId() == R.id.toggle) {
                if (toggleButton.isChecked()) {
                    lockDao.setSetting(lockDao.SETTING_CHECK_ACTIVITY, 1);
                } else {
                    lockDao.setSetting(lockDao.SETTING_CHECK_ACTIVITY, 0);
                }
            } else if (v.getId() == R.id.turn_on_off) {
                if (turnButton.isChecked()) {
                    Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivity(intent);
                /*if(!isAccessibilityEnabled(getActivity().getApplicationContext())){
                    turnButton.setChecked(false);
                }*/
                    //lockDao.setSetting(lockDao.SETTING_TURN_ON_OFF,1);
                } else {
                    //lockDao.setSetting(lockDao.SETTING_TURN_ON_OFF,1);
                    Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivity(intent);
                /*if(isAccessibilityEnabled(getActivity().getApplicationContext())){
                    turnButton.setChecked(true);
                }*/
                }
            }
        }
    };

    private void importDB() {
        try {
            File sd = Environment.getExternalStorageDirectory();
            if (sd.canWrite()) {
                String backupDBPath = "selfcontrol.db";
                File currentDB = getActivity().getDatabasePath("selfcontrol.db");
                File backupDB = new File(sd, backupDBPath);

                FileChannel src = new FileInputStream(backupDB).getChannel();
                FileChannel dst = new FileOutputStream(currentDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                Toast.makeText(getActivity().getApplicationContext(), "Import Successful!",
                        Toast.LENGTH_SHORT).show();

            }
        } catch (Exception e) {
            Toast.makeText(getActivity().getApplicationContext(), "Import Failed!", Toast.LENGTH_SHORT)
                    .show();

        }
    }

    private void exportDB() {
        try {
            File sd = Environment.getExternalStorageDirectory();

            if (sd.canWrite()) {
                String backupDBPath = "selfcontrol.db";
                File currentDB = getActivity().getDatabasePath("selfcontrol.db");
                File backupDB = new File(sd, backupDBPath);

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                Toast.makeText(getActivity().getApplicationContext(), "Backup Successful!",
                        Toast.LENGTH_SHORT).show();

            }
        } catch (Exception e) {
            Toast.makeText(getActivity().getApplicationContext(), "Backup Failed!", Toast.LENGTH_SHORT)
                    .show();

        }
    }
}
