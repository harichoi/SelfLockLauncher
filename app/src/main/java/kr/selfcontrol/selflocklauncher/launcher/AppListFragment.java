package kr.selfcontrol.selflocklauncher.launcher;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

import kr.selfcontrol.selflocklauncher.R;
import kr.selfcontrol.selflocklauncher.model.AppDetail;

/**
 * Created by owner on 2015-12-18.
 */
public class AppListFragment extends BasicFragment {

    private PackageManager manager;
    private GridView list;
    boolean firstUpdated=false;
    private LayoutInflater inflater;
    BaseAdapter adapter;

    List<AppDetail> rApps=new ArrayList<AppDetail>();

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void onAppChanged(){
        if(!firstUpdated) return;
        rApps.clear();
        for(AppDetail app : apps){
            if(!app.isBlocked()){
                rApps.add(app);
            } else{
                Log.d("BLOKED2",app.name.toString());
            }
        }
        adapter.notifyDataSetChanged();
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(firstUpdated)
            checkIfChanged();
        if(rApps != null) {
            rApps.clear();
            for (AppDetail app : apps) {
                if (!app.isBlocked()) {
                    rApps.add(app);
                }
            }
        }
        this.inflater=inflater;

        View view;
        view = inflater.inflate(R.layout.package_list, null);


        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAppChanged();
            }
        });
        list = (GridView)view.findViewById(R.id.appList);

        adapter = new AppListAdapter(inflater, rApps);
        list.setAdapter(adapter);

        firstUpdated=true;
        return view;
    }


    public class AppListAdapter extends BaseAdapter
    {
        private LayoutInflater inflater = null;
        List<AppDetail> appList;

        public AppListAdapter(LayoutInflater inflater, List<AppDetail> appList){
            this.appList=appList;
            this.inflater=inflater;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if(convertView == null){

                viewHolder=new ViewHolder();
                convertView = inflater.inflate(R.layout.package_list_item, null);
                convertView.setTag(viewHolder);
                convertView.setOnClickListener(buttonClickListener);
                viewHolder.appIcon=(ImageView)convertView.findViewById(R.id.item_app_icon);
                viewHolder.appLabel=(TextView)convertView.findViewById(R.id.item_app_label);

            }

            viewHolder=(ViewHolder)convertView.getTag();
            AppDetail app=getItem(position);
            viewHolder.appIcon.setImageDrawable(app.icon);
            viewHolder.appLabel.setText(app.label);
            viewHolder.appDetail=app;
            if(app.isBlocked())
                viewHolder.appLabel.setText("BLOCKED");
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
                try {
                    manager = getActivity().getPackageManager();
                    ViewHolder viewHolder = (ViewHolder) v.getTag();
                    Intent i = manager.getLaunchIntentForPackage(viewHolder.appDetail.name + "");
                    getActivity().startActivity(i);
                }catch(Exception exc){}
            }
        };
        class ViewHolder{
            public AppDetail appDetail;
            public ImageView appIcon;
            public TextView appLabel;
        }
    };
}
