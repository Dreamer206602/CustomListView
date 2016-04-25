package com.mx.listviewlib;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;

//import android.content.SharedPreferences;


/**
 * Created by boobooL on 2016/4/25 0025
 * Created 邮箱 ：boobooMX@163.com
 */
public class RefreshTime {
    public static final String  PRE_NAME="refresh_time";
    public static final String  SET_FRESHTIME="set_refresh_time";
    public static SharedPreferences preferences;
    private static SimpleDateFormat sDateFormat=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public static String getRefreshTime(Context context){
        preferences=context.getSharedPreferences(PRE_NAME, Context.MODE_PRIVATE);
        return preferences.getString(SET_FRESHTIME,sDateFormat.format(new Date()));
    }

    public static void setRefreshTime(Context context,Date date){
        preferences=context.getSharedPreferences(PRE_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=preferences.edit();
        editor.putString(SET_FRESHTIME,sDateFormat.format(date));
        editor.commit();
    }
}
