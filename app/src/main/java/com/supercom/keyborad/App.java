package com.supercom.keyborad;

import android.app.Application;

import com.supercom.keyborad.latin.RichInputMethodManager;
import com.supercom.keyborad.latin.Subtype;
import com.supercom.keyborad.latin.settings.LanguagesSettingsFragment;
import com.supercom.keyborad.latin.utils.SubtypeLocaleUtils;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();


    }

    public void setLang(String lang){
        final Subtype subtype = SubtypeLocaleUtils.getDefaultSubtype(
                lang,
                getApplicationContext().getResources());
         RichInputMethodManager.init(getApplicationContext());
        RichInputMethodManager mRichImm = RichInputMethodManager.getInstance();
    }
}
