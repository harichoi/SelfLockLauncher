package kr.selfcontrol.selflocklauncher.launcher;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import kr.selfcontrol.selflocklauncher.R;
import kr.selfcontrol.selflocklauncher.model.AppDetail;
import kr.selfcontrol.selflocklauncher.vo.PackageVo;

/**
 * Created by owner on 2015-12-18.
 */
public class BlockingAppFragment extends BasicFragment {

    Button blockButton;
    EditText editText;
    boolean needTimer;
    private Timer timer;
    TimerTask timerTask=new TimerTask() {
        @Override
        public void run() {

            if(!needTimer){
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
    void update(){
        mOnAppChanged.onAppChanged();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public List<AppDetail> rApps;
    private PackageManager manager;
    private ListView list;
    private LayoutInflater inflater;
    boolean firstUpdated=false;
    BaseAdapter adapter;

    @Override
    public void onAppChanged(){
        if(!firstUpdated) return;
        Log.d("update","onAppChanged()");
        rAppsSet();
        adapter. notifyDataSetChanged();
    }
    void rAppsSet(){
        rApps.clear();
        for(AppDetail app : apps){
            if(!app.isBlocked()){
                rApps.add(app);
            }
        }
        for(AppDetail app : apps){
            if(app.isBlocked() && !app.isUnlocking()){
                rApps.add(app);
            }
        }
        needTimer=false;
        for(AppDetail app : apps){
            if(app.isBlocked() && app.isUnlocking()){
                rApps.add(app);
                needTimer=true;
            }
        }

        if(needTimer && timer==null) {
            try {
                timer = new Timer();
                timer.schedule(timerTask, 0, 1000);
            }catch(Exception exc){
            }
        }
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(firstUpdated)
            checkIfChanged();
        this.inflater=inflater;
        View view;
        view = inflater.inflate(R.layout.package_blocking, null);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAppChanged();
            }
        });
        list = (ListView)view.findViewById(R.id.appList);
        blockButton=(Button)view.findViewById(R.id.block_button);
        editText=(EditText)view.findViewById(R.id.text);

        blockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editText.getText().toString().trim().isEmpty()){
                    return;
                }
                lockDao.insertPakcageVo(new PackageVo(editText.getText().toString(), 0));
                editText.setText("");
                update();
            }
        });
        rApps=new ArrayList<AppDetail>();
        rAppsSet();
        adapter = new BlockingAppListAdapter(inflater, rApps);
        list.setAdapter(adapter);
        firstUpdated=true;

        return view;
    }

    public String getTimeToString(long time){
        time=time/1000;
        StringBuilder sb=new StringBuilder();
        if((int)(time/3600/24)>0){
            sb.append((int)time/3600/24+"days ");
        }
        time=time%(3600*24);
        if((int)(time/3600)>0){
            sb.append((int)time/3600+"hours ");
        }

        time=time%(3600);
        if((int)(time/60)>0){
            sb.append((int)time/60+"minitues ");
        }

        time=time%(60);
        if((int)(time)>0){
            sb.append((int)time+"seconds ");
        }
        return sb.toString();
    }
    public class BlockingAppListAdapter extends BaseAdapter
    {
        private LayoutInflater inflater = null;
        List<AppDetail> appList;

        public BlockingAppListAdapter(LayoutInflater inflater, List<AppDetail> appList){
            this.appList=appList;
            this.inflater=inflater;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if(convertView == null){

                viewHolder=new ViewHolder();
                convertView = inflater.inflate(R.layout.package_blocking_item, null);
                convertView.setTag(viewHolder);

                viewHolder.appIcon=(ImageView)convertView.findViewById(R.id.app_icon);
                viewHolder.appName=(TextView)convertView.findViewById(R.id.app_name);
                viewHolder.appExplain=(TextView)convertView.findViewById(R.id.app_explain);
                viewHolder.blockButton=(Button)convertView.findViewById(R.id.block_button);
                viewHolder.blockButton.setOnClickListener(buttonClickListener);
                viewHolder.blockButton.setTag(viewHolder);

            }
            viewHolder=(ViewHolder)convertView.getTag();
            AppDetail app=getItem(position);
            viewHolder.appDetail=app;
            viewHolder.appIcon.setImageDrawable(app.icon);
            viewHolder.appName.setText(app.label);
            viewHolder.isBlocked=app.isBlocked();
            viewHolder.isUnlocking=app.isUnlocking();
            if(!app.isBlocked()) {
                viewHolder.appExplain.setText(app.name);
                viewHolder.blockButton.setText("Block");
                convertView.setBackgroundColor(0xFFABABFF);
            } else if(!app.isUnlocking()){
                viewHolder.appExplain.setText(app.name);
                viewHolder.blockButton.setText("UnBlock");
                convertView.setBackgroundColor(0xFFFFABAB);
            } else {
                convertView.setBackgroundColor(0xFFABFFAB);
                viewHolder.blockButton.setText("Cancel");
                viewHolder.appExplain.setText(getTimeToString((app.packageVo.dateUnlock-System.currentTimeMillis())) + "초 남음");
            }
            return convertView;
        }
        @Override
        public int getCount() {
            return appList.size();
        }

        @Override
        public AppDetail getItem(int position) {
            return appList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private View.OnClickListener buttonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager = getActivity().getPackageManager();
                ViewHolder viewHolder=(ViewHolder) v.getTag();
                AppDetail app=viewHolder.appDetail;
                if (!app.isBlocked()) {
                    lockDao.insertPakcageVo(new PackageVo(app.name, 0));
                } else if (app.isUnlocking()) {
                    lockDao.insertPakcageVo(new PackageVo(app.name, 0));
                } else {
                    lockDao.insertPakcageVo(new PackageVo(app.name,System.currentTimeMillis()+lockDao.getSettingLong("applocktime")));
                }
                update();
            }
        };
        class ViewHolder{
            public AppDetail appDetail;
            public ImageView appIcon;
            public TextView appName;
            public TextView appExplain;
            public Button blockButton;
            public boolean isBlocked;
            public boolean isUnlocking;
        }
    };
}
