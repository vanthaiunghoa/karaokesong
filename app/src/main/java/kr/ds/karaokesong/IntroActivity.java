package kr.ds.karaokesong;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import kr.ds.config.Config;
import kr.ds.data.AppSummeryData;
import kr.ds.data.BaseResultListener;
import kr.ds.handler.AppHandler;
import kr.ds.utils.SharedPreference;
import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * Created by Administrator on 2016-11-08.
 */
public class IntroActivity extends BaseActivity {
    private InterstitialAd interstitialAd;
    private CountDownTimer mStartCountDownTimer;
    private boolean interstitialCanceled = false;
    private int mCount = 1;
    private AppHandler mAppHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_intro);
        ShortcutBadger.removeCount(getApplicationContext());
        SharedPreference.putSharedPreference(getApplicationContext(), "badger_count", 0);

        new AppSummeryData().clear().setCallBack(new BaseResultListener() {
            @Override
            public <T> void OnComplete() {
            }
            @Override
            public <T> void OnComplete(Object data) {

                if(data != null){
                    mAppHandler = (AppHandler) data;
                    Config.isAd = mAppHandler.isAd();
                    Config.isDev = mAppHandler.isDev();
                    Config.ADMOB_AD = mAppHandler.getAdmob_ad();
                    Config.ADMOB_NATIVE = mAppHandler.getAdmob_native();
                    Config.isInstall = mAppHandler.isInstall();
                    Config.INSTALL_MESSAGE = mAppHandler.getInstall_message();
                    Config.INSTALL_URL = mAppHandler.getInstall_url();
                    init();
                }else{
                    init();
                }
            }
            @Override
            public void OnMessage(String str) {

            }
        }).setUrl(Config.URL+ Config.URL_XML+ Config.URL_APP).setParam("").getView();


    }
    public void init(){
        if(Config.isAd){
            setAdMob();
        }else{
            OnNext();
        }
    }
    public void setAdMob(){
        interstitialAd = new InterstitialAd(getApplicationContext());
        interstitialAd.setAdUnitId(Config.ADMOB_AD);
        AdRequest adRequest = new AdRequest.Builder().build();

        interstitialAd.loadAd(adRequest);
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // TODO Auto-generated method stub
                super.onAdClosed();
                OnNext();
                Log.i("TEST", "onAdClosed");
            }
            @Override
            public void onAdLoaded() {
                // TODO Auto-generated method stub
                super.onAdLoaded();
                Log.i("TEST", "onAdLoaded");

            }
            @Override
            public void onAdFailedToLoad(int errorCode) {
                // TODO Auto-generated method stub
                super.onAdFailedToLoad(errorCode);
                OnNext();
            }
        });

        //10초 딜레이
        mStartCountDownTimer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // TODO Auto-generated method stub
                if(mCount > 1){
                    if (interstitialAd.isLoaded()) {
                        if(mStartCountDownTimer != null){
                            mStartCountDownTimer.cancel();
                        }
                        interstitialCanceled = true;
                        interstitialAd.show();
                    }
                }
                mCount++;
            }
            @Override
            public void onFinish() {
                // TODO Auto-generated method stub
                if(!interstitialCanceled) {
                    OnNext();
                }
            }
        };
        mStartCountDownTimer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mStartCountDownTimer != null){
            mStartCountDownTimer.cancel();
        }
    }

    public void OnNext(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}