package kr.selfcontrol.selflocklauncher.launcher;

import android.app.Fragment;
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
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import kr.selfcontrol.selflocklauncher.R;
import kr.selfcontrol.selflocklauncher.dao.SelfLockDao;
import kr.selfcontrol.selflocklauncher.manager.TimeManager;
import kr.selfcontrol.selflocklauncher.model.AppDetail;
import kr.selfcontrol.selflocklauncher.model.TimeDetail;
import kr.selfcontrol.selflocklauncher.util.SelfControlUtil;
import kr.selfcontrol.selflocklauncher.vo.PackageVo;

/**
 * Created by owner on 2015-12-18.
 */
public class BlockingTimeFragment extends Fragment {

    SelfLockDao lockDao = SelfLockDao.getInstance();
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public List<TimeDetail> timeList = new ArrayList<>();
    private ListView list;
    private LayoutInflater inflater;
    boolean firstUpdated=false;
    BaseAdapter adapter;

    void update(){
        List<TimeDetail> timeListTemp = lockDao.getTimeing();
        timeList.clear();
        for(TimeDetail temp : timeListTemp) {
            if(temp.isAffecting() || temp.isBlocked()) {
                timeList.add(temp);
            } else {
                lockDao.removeTimeing(temp);
            }
        }
        try {
            Collections.sort(timeList, new TimeManager.CompareTimeDetail());
        }catch(Exception exc){}
        timeListSet();
        if(adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    void timeListSet(){
        needTimer=false;
        for(TimeDetail app : timeList){
            if(app.isBlocked() && app.isUnlocking() || app.isAffecting()){
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
        this.inflater=inflater;
        View view;
        view = inflater.inflate(R.layout.package_timeing, null);

        list = (ListView)view.findViewById(R.id.appList);
        blockButton=(Button)view.findViewById(R.id.block_button);
        editText=(EditText)view.findViewById(R.id.text);

        blockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editText.getText().toString().trim().isEmpty()){
                    editText.setText("100|b|*|1-7|*");
                    return;
                }
                if(!TimeManager.getInstance().isValid(editText.getText().toString())){
                    editText.setText("100|b|2016/3/20|1-7|11:11:11");
                    return;
                }
                boolean isBlocked=editText.getText().toString().split("\\|")[1].equals("b");
                TimeDetail timeDetail=new TimeDetail.Builder(editText.getText().toString())
                        .setDateAffect(System.currentTimeMillis() + (isBlocked?10000:lockDao.getSettingLong("applocktime")))
                        .setDateUnlock(0)
                        .build();
                if(!lockDao.hasTimeing(SelfControlUtil.md5(timeDetail.name))) {
                    lockDao.setTimeing(timeDetail);
                }
                editText.setText("");
                update();
            }
        });
        update();
        adapter = new BlockingTimeListAdapter(inflater, timeList);
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
    public class BlockingTimeListAdapter extends BaseAdapter
    {
        private LayoutInflater inflater = null;
        List<TimeDetail> timeList;

        public BlockingTimeListAdapter(LayoutInflater inflater, List<TimeDetail> timeList){
            this.timeList = timeList;
            this.inflater = inflater;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if(convertView == null){

                viewHolder=new ViewHolder();
                convertView = inflater.inflate(R.layout.package_timeing_item, null);
                convertView.setTag(viewHolder);

                viewHolder.appName=(TextView)convertView.findViewById(R.id.app_name);
                viewHolder.appExplain=(TextView)convertView.findViewById(R.id.app_explain);
                viewHolder.blockButton=(Button)convertView.findViewById(R.id.block_button);
                viewHolder.blockButton.setOnClickListener(buttonClickListener);
                viewHolder.blockButton.setTag(viewHolder);

            }
            viewHolder=(ViewHolder)convertView.getTag();
            TimeDetail app=getItem(position);
            viewHolder.timeDetail=app;
            viewHolder.appName.setText(app.name);
            viewHolder.isBlocked=app.isBlocked();
            viewHolder.isUnlocking=app.isUnlocking();
            if(app.isAffecting()) {
                viewHolder.blockButton.setText("Cancel");
                convertView.setBackgroundColor(0xFFABFFFF);
                viewHolder.appExplain.setText(getTimeToString((app.dateAffect - System.currentTimeMillis())) + " 이후 적용");
            }
            if(!app.isBlocked()) {
                viewHolder.blockButton.setText("Block");
                convertView.setBackgroundColor(0xFFABABFF);
            } else if(!app.isUnlocking()){
                viewHolder.appExplain.setText(app.name);
                viewHolder.blockButton.setText("UnBlock");
                convertView.setBackgroundColor(0xFFFFABAB);
            } else {
                convertView.setBackgroundColor(0xFFABFFAB);
                viewHolder.blockButton.setText("Cancel");
                viewHolder.appExplain.setText(getTimeToString((app.dateUnlock-System.currentTimeMillis())) + "초 남음");
            }
            return convertView;
        }
        @Override
        public int getCount() {
            return timeList.size();
        }

        @Override
        public TimeDetail getItem(int position) {
            return timeList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private View.OnClickListener buttonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewHolder viewHolder=(ViewHolder) v.getTag();
                TimeDetail app=viewHolder.timeDetail;
                if(app.isAffecting()) {
                    lockDao.removeTimeing(app);
                } else if (!app.isBlocked()) {
                    app.dateUnlock=0;
                    lockDao.setTimeing(app);
                } else if (app.isUnlocking()) {
                    app.dateUnlock=0;
                    lockDao.setTimeing(app);
                } else {
                    app.dateUnlock = System.currentTimeMillis() + lockDao.getSettingLong("applocktime");
                    lockDao.setTimeing(app);
                }
                update();
            }
        };
        class ViewHolder{
            public TimeDetail timeDetail;
            public ImageView appIcon;
            public TextView appName;
            public TextView appExplain;
            public Button blockButton;
            public boolean isBlocked;
            public boolean isUnlocking;
        }
    };
}
