package cn.ckz.vxcamera;

import android.app.Application;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

/**
 * Created by hz-java on 2018/7/9.
 */

public class App extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        SpeechUtility.createUtility(App.this, SpeechConstant.APPID + "=59e44c6f");
    }
}
