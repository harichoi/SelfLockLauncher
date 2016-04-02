package kr.selfcontrol.selflocklauncher.launcher;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TabHost;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import kr.selfcontrol.selflocklauncher.R;
import kr.selfcontrol.selflocklauncher.dao.SelfLockDao;
import kr.selfcontrol.selflocklauncher.model.AppDetail;
import kr.selfcontrol.selflocklauncher.vo.PackageVo;

public class PackageMgrActivity extends AppCompatActivity implements BasicFragment.OnAppChanged, TabHost.OnTabChangeListener{

    HashMap<String,Fragment> fragmentList=new HashMap<String,Fragment>();

    SelfLockDao lockDao;
    private PackageManager manager;
    private List<AppDetail> apps = AppManager.getInstance().getApps();


    @Override
    public void onTabChanged(String tabId) {
        if(tabId.equals("Tab1")){
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.my_tab,fragmentList.get("AppList"))
                    .commit();
        } else if(tabId.equals("Tab2")){
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.my_tab,fragmentList.get("BlockingAppList"))
                    .commit();
        } else if(tabId.equals("Tab3")){
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.my_tab,fragmentList.get("BlockingTimeList"))
                    .commit();
        } else if(tabId.equals("Tab4")){
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.my_tab,fragmentList.get("SettingList"))
                    .commit();
        }
    }

    @Override
    public void onAppChanged(){
        updateApps();
        AppListFragment appFrag=(AppListFragment)fragmentList.get("AppList");
        if(appFrag!=null)
            appFrag.onAppChanged();
        BlockingAppFragment blkAppFrag=(BlockingAppFragment)fragmentList.get("BlockingAppList");
        if(blkAppFrag!=null)
            blkAppFrag.onAppChanged();
    }

    public TabHost.TabContentFactory makeTabView(){
        return new TabHost.TabContentFactory(){
            public View createTabContent(String tag) {
                return findViewById(R.id.my_tab);
            }
        };
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SelfLockDao.createInstance(getApplicationContext());
        lockDao=SelfLockDao.getInstance();
     //   messageBox("1","1");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_mgr);
        loadApps();

        if(Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 1234);
            }
        }

        TabHost tabHost=(TabHost)findViewById(android.R.id.tabhost);
        tabHost.setup();

        TabHost.TabSpec spec1=tabHost.newTabSpec("Tab1")
                .setContent(makeTabView())
                .setIndicator("App List");
        tabHost.addTab(spec1);
        TabHost.TabSpec spec2=tabHost.newTabSpec("Tab2")
                .setContent(makeTabView())
                .setIndicator("Block App");
        tabHost.addTab(spec2);
        TabHost.TabSpec spec3=tabHost.newTabSpec("Tab3")
                .setContent(makeTabView())
                .setIndicator("Time Lock");
        tabHost.addTab(spec3);
        TabHost.TabSpec spec4=tabHost.newTabSpec("Tab4")
                .setContent(makeTabView())
                .setIndicator("Setting");
        tabHost.addTab(spec4);


        tabHost.setOnTabChangedListener(this);

        AppListFragment fragment=new AppListFragment();
        fragment.setOnAppChanged(this);

        BlockingAppFragment blockingFragment=new BlockingAppFragment();
        blockingFragment.setOnAppChanged(this);

        BlockingTimeFragment timeimgFragment=new BlockingTimeFragment();

        SettingFragment settingFragment=new SettingFragment();

        fragmentList.put("AppList", fragment);
        fragmentList.put("BlockingAppList", blockingFragment);
        fragmentList.put("BlockingTimeList", timeimgFragment);
        fragmentList.put("SettingList", settingFragment);

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.my_tab,fragment)
                .commit();
    }

    public void messageBox(String title,String msg){
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        // Some stuff to do when ok got clicked
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        // Some stuff to do when cancel got clicked
                    }
                })
                .show();
    }
    public void onArticleSelected(String articleUri){
        // DetailFragment 를 가져와서 이벤트를 처리합니다.
        AppListFragment detailFrag = null;//(AppListFragment)getFragmentManager().findFragmentById(0);
        detailFrag.getTag();
    }
    public void updateApps(){
        HashSet<PackageVo> exist=new HashSet<>();
        Log.d("update", "updateApps");
        List<PackageVo> blockList=lockDao.readPackageList();
        for(AppDetail app:apps){
            app.packageVo=null;
        }
        if(blockList!=null){
            for(AppDetail app:apps) {
                for (PackageVo blkApp : blockList) {
                    if (app.name.equals(blkApp.key)) {
                        exist.add(blkApp);
                        app.packageVo = blkApp;
                        if (!app.isBlocked()) {
                            lockDao.deletePackageVo(app.name);
                        }
                    }
                }
            }
        }
        for(PackageVo blkApp : blockList){
            if(!exist.contains(blkApp))
            {
                Log.d("delete", blkApp.key);
                if(!blkApp.isBlocked()) {
                    lockDao.deletePackageVo(blkApp.key);
                } else{
                    AppDetail app=new AppDetail();
                    app.label=blkApp.key;
                    app.name=blkApp.key;
                    app.packageVo=blkApp;
                    apps.add(app);
                }
            }
        }
    }
	public void loadApps(){
        HashSet<String> exist=new HashSet<>();

        manager = getPackageManager();

        apps.clear();
        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = manager.queryIntentActivities(i, 0);

        for(ResolveInfo ri : resolveInfos){
            if(ri.activityInfo.packageName.contains("kr.selfcontrol.selflocklauncher"))
                continue;
            if(!exist.contains(ri.activityInfo.packageName)) {

                AppDetail app = new AppDetail();
                app.label = ri.activityInfo.loadLabel(manager);//ri.loadLabel(manager);
                app.name = ri.activityInfo.packageName;//ri.activityInfo.packageName;
                app.icon = ri.activityInfo.loadIcon(manager);// ri.activityInfo.loadIcon(manager);
                apps.add(app);
                exist.add(ri.activityInfo.packageName);
            }
        }

        List<PackageInfo> availableActivities=null;
        try{
            availableActivities=manager.getInstalledPackages(PackageManager.GET_ACTIVITIES);
        } catch(Exception exc){
            try {
                availableActivities = manager.getInstalledPackages(0);
            }catch(Exception exc2){

            }
        }
        if(availableActivities!=null) {
            for (PackageInfo ri : availableActivities) {
                if (exist.contains(ri.packageName)) {

               /* if(ri.activities!=null && (ri.packageName.contains("kakao") || ri.packageName.contains("setting"))) {
                    for (ActivityInfo info : ri.activities) {
                        AppDetail app = new AppDetail();
                        app.label = info.loadLabel(manager);//ri.loadLabel(manager);
                        app.name = info.name;//ri.activityInfo.packageName;
                        app.icon = ri.applicationInfo.loadIcon(manager);// ri.activityInfo.loadIcon(manager);
                        apps.add(app);
                    }
                }*/
                }
                if (ri.packageName.equals("android")) {
                    continue;
                }

                if (ri.packageName.contains("installer") || ri.applicationInfo.loadLabel(manager).toString().contains("installer") || ri.applicationInfo.loadLabel(manager).toString().contains("manager")) {
                    if (!exist.contains(ri.packageName)) {
                        AppDetail app = new AppDetail();
                        app.label = ri.applicationInfo.loadLabel(manager);//ri.loadLabel(manager);
                        app.name = ri.packageName;//ri.activityInfo.packageName;
                        app.icon = ri.applicationInfo.loadIcon(manager);// ri.activityInfo.loadIcon(manager);
                        apps.add(app);
                        exist.add(ri.packageName);
                    }
                }
            }
        }
        updateApps();
	}

}