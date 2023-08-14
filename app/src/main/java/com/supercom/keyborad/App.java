package com.supercom.keyborad;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.supercom.keyborad.latin.RichInputMethodManager;
import com.supercom.keyborad.latin.Subtype;
import com.supercom.keyborad.latin.utils.SubtypeLocaleUtils;

public class App extends Application {

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String receivedData = intent.getStringExtra("data_key");
            setupLanguage(receivedData);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter("com.supercom.pureprotect");
        registerReceiver(receiver, filter);
    }

    public void setupLanguage(String lang) {
        Log.d("Testing","LanguageReceived");
        final Subtype subtype = SubtypeLocaleUtils.getDefaultSubtype(
                lang,
                getApplicationContext().getResources()
        );
        RichInputMethodManager.init(getApplicationContext());
        RichInputMethodManager mRichImm = RichInputMethodManager.getInstance();
        mRichImm.addSubtype(subtype);
    }
}
