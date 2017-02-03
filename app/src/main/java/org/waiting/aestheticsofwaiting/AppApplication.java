package org.waiting.aestheticsofwaiting;

import android.app.Application;

import com.tsengvn.typekit.Typekit;

/**
 * Created by d on 2017-02-02.
 * 폰트용 동영상
 */

public class AppApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Typekit.getInstance()
                .addNormal(Typekit.createFromAsset(this, "MY_FONT "));
    }
}
