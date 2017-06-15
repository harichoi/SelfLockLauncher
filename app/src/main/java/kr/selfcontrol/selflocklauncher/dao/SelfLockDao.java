package kr.selfcontrol.selflocklauncher.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kr.selfcontrol.selflocklauncher.model.TimeDetail;
import kr.selfcontrol.selflocklauncher.util.SelfControlUtil;
import kr.selfcontrol.selflocklauncher.vo.PackageVo;

/**
 * Created by owner2 on 2015-12-29.
 */
public class SelfLockDao  extends SQLiteOpenHelper {

    public SQLiteDatabase database;

    public SelfLockDao(Context context){
        super(context, "selfcontrol.db", null, 1);
    }

    private static SelfLockDao instance = null;

    public static void createInstance(Context context){
       // if(instance == null) {
            instance = new SelfLockDao(context);
       // }
    }
    public static SelfLockDao getInstance(){
        return instance;
    }


    @Override
    public void onCreate(SQLiteDatabase db){
        database=db;
        db.execSQL("CREATE TABLE package_list(" +
                "key varchar(200) not null" +
                ",date_unlock long" +
                ",primary key(key))");
        db.execSQL("CREATE TABLE activity_list(" +
                "pack varchar(200) not null" +
                ",key varchar(200) not null" +
                ",date_unlock long" +
                ",primary key(pack,key))");
        db.execSQL("CREATE TABLE setting(" +
                "key varchar(100) not null" +
                ",value varchar(100)" +
                ",primary key(key))");
        db.execSQL("CREATE TABLE time_list(" +
                "key varchar(50) not null" +
                ",value varchar(200)" +
                ",date_unlock long" +
                ",date_affect long" +
                ",primary key(key))");
        //db.execSQL("CREATE INDEX idx1 on block_list(key)");
        setSetting("haha",55555);
        Log.d("Created", "Created");


    }

    @Override
    public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){
    }

    public List<PackageVo> readPackageList(){
//        setTimeing(new TimeDetail("100|b|*|1-7|*"));
//        setTimeing(new TimeDetail("90|w|*|2-6|9-18:*:*"));
//        insertPakcageVo(new PackageVo("AAActivity", 0));

        Log.d("func","readPackageList");

        List<PackageVo> packageList=new ArrayList<>();
        List<HashMap<String,String>> listMap = readSql("select * from package_list");
        for(HashMap<String,String> map : listMap){
            PackageVo packageVo=new PackageVo();
            packageVo.key=map.get("key");
            packageVo.dateUnlock=Long.parseLong(map.get("date_unlock"));
            packageList.add(packageVo);
        }
        return packageList;
    }

    public PackageVo readPackageVo(String key){
        Log.d("func","readPackageVo");

        List<HashMap<String,String>> listMap = readSql("select * from package_list where key=?",key);
        for(HashMap<String,String> map : listMap){
            PackageVo packageVo=new PackageVo();
            packageVo.key=map.get("key");
            packageVo.dateUnlock=Long.parseLong(map.get("date_unlock"));
            return packageVo;
        }
        return null;
    }
    public void insertPakcageVo(PackageVo packageVo){
        Log.d("insertPackageVo",packageVo.key);
        PackageVo temp=readPackageVo(packageVo.key);
        if(temp==null) {
            Log.d("insertBlockVo","inserted");
            writeSql("insert into package_list(key,date_unlock) values (?,?)", packageVo.key,String.valueOf(packageVo.dateUnlock));
        } else {
            Log.d("insertBlockVo","updated");
            writeSql("update package_list set date_unlock=? where key=?",String.valueOf(packageVo.dateUnlock),packageVo.key);
        }
    }
    public void deletePackageVo(String key){
        Log.d("deletePackageVo",key);
        writeSql("delete from package_list where key=?",key);
    }
    public static final String SETTING_DELAY = "delay";
    public static final String SETTING_APP_LOCK_TIME = "applocktime";
    public static final String SETTING_DATE_UNLOCK = "dateunlock";
    public static final String SETTING_CHECK_ACTIVITY = "checkactivity";
    public static final String SETTING_TURN_ON_OFF = "turnonoff";
    public static final String SETTING_TIME_PRIFIX = "locktime";
    public void setSetting(String key,long time){
        Log.d("setSetting",key+":"+time);
        if(!hasSetting(key)){
            Log.d("setSetting","1");
            writeSql("insert into setting(key,value) values (?,?)",key,String.valueOf(time));
        } else {
            Log.d("setSetting","2");
            writeSql("update setting set value=? where key=?",String.valueOf(time),key);
        }
    }
    public long getSettingLong(String key){
        List<HashMap<String,String>> listMap = readSql("select * from setting where key=?",key);
        for(HashMap<String,String> map : listMap){
            Log.d("getSetting",key+":"+map.get("value"));
            return Long.parseLong(map.get("value"));
        }
        Log.d("getSetting",key+": null");
        return 0;
    }

    public void setSetting(String key,String value){
        Log.d("setSetting",key+":"+value);
        if(!hasSetting(key)){
            Log.d("setSetting","1");
            writeSql("insert into setting(key,value) values (?,?)",key,String.valueOf(value));
        } else {
            Log.d("setSetting","2");
            writeSql("update setting set value=? where key=?",String.valueOf(value),key);
        }
    }

    public boolean hasSetting(String col){

        List<HashMap<String,String>> listMap = readSql("select * from setting where key=?",col);
        for(HashMap<String,String> map : listMap){
            return true;
        }
        return false;
    }

    public boolean hasTimeing(String key){

        List<HashMap<String,String>> listMap = readSql("select * from time_list where key=?",key);
        for(HashMap<String,String> map : listMap){
            return true;
        }
        return false;
    }
    public void setTimeing(TimeDetail timeDetail){
        String key=SelfControlUtil.md5(timeDetail.name);
        if(!hasTimeing(key)){
            writeSql("insert into time_list(key,value,date_unlock,date_affect) values (?,?,?,?)",key,timeDetail.name,String.valueOf(timeDetail.dateUnlock),String.valueOf(timeDetail.dateAffect));
        } else {
            writeSql("update time_list set value=?, date_unlock=?, date_affect=? where key=?",timeDetail.name,String.valueOf(timeDetail.dateUnlock),String.valueOf(timeDetail.dateAffect),key);
        }
    }

    public void removeTimeing(TimeDetail timeDetail){
        String key=SelfControlUtil.md5(timeDetail.name);
        writeSql("delete from time_list where key=?",key);
    }
    public List<TimeDetail> getTimeing(){
        List<HashMap<String,String>> listMap = readSql("select * from time_list");
        List<TimeDetail> result=new ArrayList<>();
        for(HashMap<String,String> map : listMap){
            TimeDetail timeDetail = new TimeDetail.Builder(map.get("value"))
                    .setDateAffect(Long.parseLong(map.get("date_affect")))
                    .setDateUnlock(Long.parseLong(map.get("date_unlock")))
                    .build();
            result.add(timeDetail);
        }
        return result;
    }


    private boolean writeSql(String str,String ...args){
        if(database==null) {
            try {
                database = getWritableDatabase();
            } catch (Exception exc) {
                database.close();
                database=getWritableDatabase();
            }
        }
        // database=getWritableDatabase();
        database.execSQL(str, args);
        // database.close();
        return true;
    }
    private List<HashMap<String,String>> readSql(String str,String ...args){

        if(database==null) {
            try {
                Log.d("made","1");
                database = getWritableDatabase();
                Log.d("made","2");
            } catch (Exception exc) {
                Log.d("made","3");
                exc.printStackTrace();
                Log.d("made", "4");
                database.close();
                database=getWritableDatabase();
            }
        }
        Cursor cursor=database.rawQuery(str, args);
        List<HashMap<String,String>> result=new ArrayList<HashMap<String,String>>();
        while(cursor.moveToNext()) {
            HashMap<String,String> row=new HashMap<String,String>();
            for(int i=0 ; i<cursor.getColumnCount() ; i++){
                row.put(cursor.getColumnName(i),cursor.getString(i));
            }
            result.add(row);
        }
        cursor.close();
        //       database.close();
        return result;
    }
}
