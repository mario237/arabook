package com.mariodev.novelapp.helpers;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {

    final SharedPreferences mySharedPref;

    public SharedPref(Context context) {
        mySharedPref = context.getSharedPreferences("fileName",Context.MODE_PRIVATE);
    }

    public void setLightModeState(Boolean state){
        SharedPreferences.Editor editor = mySharedPref.edit();
        editor.putBoolean("LightMode",state);
        editor.apply();
    }

    public Boolean loadLightModeState(){
        return mySharedPref.getBoolean("LightMode" , false);
    }


    public void setPagerPosition(String storyId,Integer position){
        SharedPreferences.Editor editor = mySharedPref.edit();
        editor.putInt(storyId,position);
        editor.apply();
    }

    public Integer loadPagerPosition(String storyId){
        return mySharedPref.getInt(storyId , 0);
    }


}
