package kr.selfcontrol.selflocklauncher.launcher;

import android.app.Fragment;

import java.util.List;

import kr.selfcontrol.selflocklauncher.dao.SelfLockDao;
import kr.selfcontrol.selflocklauncher.model.AppDetail;

/**
 * Created by owner on 2015-12-19.
 */
public abstract class BasicFragment extends Fragment {
    public SelfLockDao lockDao = SelfLockDao.getInstance();
    public List<AppDetail> apps = AppManager.getInstance().getApps();
    public OnAppChanged mOnAppChanged;
    public void checkIfChanged(){
        if(apps==null) return;
        if(mOnAppChanged==null) return;
    }
    public interface OnAppChanged{
        public void onAppChanged();
    }

    public void onAppChanged(){
    }
    public void setOnAppChanged(OnAppChanged obj){
        mOnAppChanged=obj;
    }

}
