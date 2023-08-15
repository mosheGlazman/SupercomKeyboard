package com.supercom.keyboard.keyboard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.supercom.keyboard.App;
import com.supercom.keyboard.latin.RichInputMethodManager;
import com.supercom.keyboard.latin.Subtype;
import com.supercom.keyboard.latin.common.LocaleUtils;
import com.supercom.keyboard.latin.utils.SubtypeLocaleUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;

public class LanguageManager {

    private static String ACTION_ASK = "com.supercom.keyboard.language.ask";
    private static String ACTION_RECEIVED = "com.supercom.keyboard.language.received";
    private static String KEY_LANGUAGES = "LANGUAGES";

    private long lastAsking = 0;
    private long askingTimeout = 5000;
    private boolean receivedNewData = false;

    private static LanguageManager instance;
    RichInputMethodManager mRichImm;
    public static LanguageManager getInstance() {
        if (instance == null) {
            instance = new LanguageManager();
        }
        return instance;
    }

    private LanguageManager(){
        RichInputMethodManager.init(App.getContext());
        mRichImm = RichInputMethodManager.getInstance();
        askLanguageFromExternalApp();
    }

    public void askLanguageFromExternalApp() {
        if (System.currentTimeMillis() - lastAsking < (askingTimeout * 2)) {
            App.log("Ask language is running");
            return;
        }

        lastAsking = System.currentTimeMillis();
        receivedNewData = false;

        App.log("Listen to received language");
        IntentFilter filter = new IntentFilter(ACTION_RECEIVED);
        App.getContext().registerReceiver(receiver, filter);
        runTimeout();
        App.log("Send Ask language");
        App.getContext().sendBroadcast(new Intent(ACTION_ASK));  }

    private void runTimeout() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!receivedNewData && System.currentTimeMillis() - lastAsking < askingTimeout) {
                    threadSleep(100);
                }

                App.log("unregister listening to language received");
                App.getContext().unregisterReceiver(receiver);
            }
        }).start();
    }

    private BroadcastReceiver receiver = new LanguageReceiver();

    public void listenToLanguageReceived() {
        App.getContext().registerReceiver(new LanguageReceiver(), new IntentFilter(ACTION_RECEIVED));
    }

    public class LanguageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            App.log("receive language");
            receivedNewData=true;
            String receivedData = intent.getStringExtra(KEY_LANGUAGES);
            String[] array = receivedData.split(",");
            setupLanguages(array);
        }
    }

    private void threadSleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void setupLanguages(String[] langs) {
        if(langs == null || langs.length ==0){
            App.log("receive empty list");
            return;
        }

        String[] newLanguages = newLanguagesArray(langs);
        String[] oldLanguages = getCurrentLangs();

        ArrayList<String> languageToAdd=getNotContains(newLanguages, oldLanguages);
        ArrayList<String> languageToRemove=getNotContains(oldLanguages, newLanguages);

        if(languageToAdd.size() == 0 && languageToRemove.size()==0){
            App.log("current list is equals");
            return;
        }

        for (String lang : languageToAdd) {
            if(!isExistInLanguages(lang)){
                App.log("language " + lang+" is not exist");
                return;
            }
        }

        for (String lang : languageToAdd) {
            addLanguage(lang);
        }

        for (String lang : languageToRemove) {
            removeLang(lang);
        }
    }

    private ArrayList<String> getNotContains(String[] array, String[] contains) {
        ArrayList<String> res = new ArrayList<>();

        for (String newLang : array) {
            boolean found = false;
            for (String oldLang : contains) {
                if (newLang.equals(oldLang)) {
                    found = true;
                    break;
                }
            }
            if(!found){
                res.add(newLang);
            }
        }

        return res;
    }

    private String[] newLanguagesArray(String[] langs) {
        String receivedData = "";
        for(int i=0;i<langs.length;i++){
            langs[i]=  langs[i].replace("en","en_US");
            langs[i]=  langs[i].replace("us","en_US");
            receivedData+=langs[i]+",";
        }

        receivedData = receivedData.substring(0,receivedData.length()-1);
        App.log("receive language: " + receivedData);
        return langs;
    }

    private boolean isExistInLanguages(String lang) {

        for (String localeString : SubtypeLocaleUtils.getSupportedLocales()) {
            if (localeString.equals(lang)) {
                return true;
            }
        }

        String supportedLangs = "";
        for (String localeString : SubtypeLocaleUtils.getSupportedLocales()) {
            supportedLangs += localeString + ", ";
        }

        App.log("supported Languages " + supportedLangs, true);
        return false;
    }

    private String[] getCurrentLangs() {
        final Set<Subtype> enabledSubtypes = mRichImm.getEnabledSubtypes(false);
        String[] res  = new String[enabledSubtypes.size()];
        String current = "";

        int i=0;
        for (Subtype st : enabledSubtypes) {
            String lang = st.getLocale();
            res[i++] = lang;
            current+=lang+",";
        }
        if(current.length()>0){
            current = current.substring(0,current.length()-1);
        }
        App.log("current list: "+current);
        return res;
    }

    private void removeLang(String lang) {
            App.log("remove language: "+lang);
            final Set<Subtype> subtypes =
                    mRichImm.getEnabledSubtypesForLocale(lang);
            for (final Subtype subtype : subtypes) {
                mRichImm.removeSubtype(subtype);
            }
    }

    private void addLanguage(String lang) {
        App.log("add language: "+lang);
        final Subtype subtype = SubtypeLocaleUtils.getDefaultSubtype(lang, App.getContext().getResources());
        mRichImm.addSubtype(subtype);
    }
}
