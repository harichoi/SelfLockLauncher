package kr.selfcontrol.selflocklauncher.launcher;

import java.util.ArrayList;
import java.util.List;

import kr.selfcontrol.selflocklauncher.model.AppDetail;

/**
 * Created by owner2 on 2016-03-27.
 */
public class AppManager {
    private List<AppDetail> apps=new ArrayList<>();

    public static AppManager instance = new AppManager();

    public static AppManager getInstance(){
        return instance;
    }

    public List<AppDetail> getApps(){
        return apps;
    }
}
