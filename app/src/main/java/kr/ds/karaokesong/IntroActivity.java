package kr.ds.karaokesong;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

/**
 * Created by Administrator on 2016-11-08.
 */
public class IntroActivity extends BaseActivity{
    private InterstitialAd interstitialAd;
    private CountDownTimer mStartCountDownTimer;
    private boolean interstitialCanceled = false;
    private int mCount = 1;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_intro);
        OnNext();
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
