package kr.ds.karaokesong;

import android.content.Context;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.util.HashMap;

import kr.ds.config.Config;
import kr.ds.data.BaseResultListener;
import kr.ds.data.GcmData;
import kr.ds.utils.DsObjectUtils;
import kr.ds.utils.SharedPreference;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private Context mContext = MyFirebaseInstanceIDService.this;
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("TEST", "Refreshed token: " + refreshedToken);
        sendRegistrationToServer(refreshedToken);
    }
    private void sendRegistrationToServer(final String token) {

        // TODO: Implement this method to send token to your app server.
        String old_token = SharedPreference.getSharedPreference(mContext, Config.TOKEN);
        String android_id = "";
        if(!DsObjectUtils.isEmpty(SharedPreference.getSharedPreference(getApplicationContext(), Config.ANDROID_ID))) {
            android_id = SharedPreference.getSharedPreference(getApplicationContext(), Config.ANDROID_ID);
        }
        if(!token.matches(old_token)) {
            HashMap<String, String> mHashMap = new HashMap<String, String>();
            mHashMap.put("reg_id", token);
            mHashMap.put("old_reg_id", old_token);
            mHashMap.put("android_id", android_id);
            mHashMap.put("send", "Y");
            mHashMap.put("type", "regis");

            if (mHashMap != null) {
                new GcmData(getApplicationContext()).clear().setCallBack(new BaseResultListener() {

                    @Override
                    public void OnMessage(String str) {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public <T> void OnComplete() {
                        // TODO Auto-generated method stub
                        SharedPreference.putSharedPreference(mContext, Config.TOKEN, token);

                    }

                    @Override
                    public <T> void OnComplete(Object data) {

                    }


                }).setUrl(Config.URL + Config.URL_XML + Config.URL_GCM).getViewPost(mHashMap);
            }
        }





    }
}