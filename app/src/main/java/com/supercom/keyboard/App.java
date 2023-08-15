package com.supercom.keyboard;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.supercom.keyboard.keyboard.LanguageManager;

public class App extends Application {
private static Context context;
    private static String ACTION_SEND_WHITE_LOG = "com.supercom.puretrack.util.hardware.AppsSharedDataManager.log";

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        LanguageManager.getInstance().listenToLanguageReceived();
    }

    private static void log(String tag,String message, boolean isError){
        Intent intent =new Intent(ACTION_SEND_WHITE_LOG);
        intent.putExtra("tag",tag);
        intent.putExtra("message",message);
        intent.putExtra("isError",isError);
        getContext().sendBroadcast(intent);

        if(!isError) {
            Log.i("Supercom", "[" + tag + "] " + message);
        }else{
            Log.e("Supercom", "[" + tag + "] " + message);
        }
    }

    public static void log(String message, boolean isError){
        log("Keyboard",message,isError);
    }

    public static void log(String message){
        log("Keyboard",message,false);
    }
}
