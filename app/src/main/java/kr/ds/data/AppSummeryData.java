package kr.ds.data;

import android.content.Context;

import org.json.JSONObject;

import java.util.concurrent.Callable;

import kr.ds.asynctask.DsAsyncTask;
import kr.ds.asynctask.DsAsyncTaskCallback;
import kr.ds.handler.AppHandler;
import kr.ds.httpclient.DsHttpClient;
import kr.ds.utils.DsObjectUtils;


/**
 * Created by Administrator on 2016-03-08.
 */
public class AppSummeryData extends BaseData {
    private Context mContext;
    private String URL = "";
    private String PARAM = "";
    private BaseResultListener mResultListener;
    public AppSummeryData(){
    }
    public AppHandler mAppHandler;
    @Override
    public BaseData clear() {

        if (mAppHandler != null) {
            mAppHandler = null;
        }
        mAppHandler = new AppHandler();
        return this;
    }

    @Override
    public BaseData setUrl(String url) {
        if(DsObjectUtils.isEmpty(URL)){
            URL = url;
        }
        return this;
    }

    @Override
    public BaseData setParam(String param) {
        PARAM = param;
        return this;
    }

    @Override
    public BaseData getView() {

        new DsAsyncTask<AppHandler>().setCallable(new Callable<AppHandler>() {
            @Override
            public AppHandler call() throws Exception {

                String content = new DsHttpClient().HttpGet(URL + PARAM, "utf-8");
                JSONObject jsonObject = new JSONObject(content);
                JSONObject summeryjsonObject = jsonObject.getJSONObject("summery");

                mAppHandler.setResult(summeryjsonObject.getString("result"));
                mAppHandler.setAd(summeryjsonObject.getBoolean("ad"));
                mAppHandler.setDev(summeryjsonObject.getBoolean("dev"));
                mAppHandler.setAdmob_ad(summeryjsonObject.getString("admob_ad"));
                mAppHandler.setAdmob_native(summeryjsonObject.getString("admob_native"));
                mAppHandler.setInstall(summeryjsonObject.getBoolean("install"));
                mAppHandler.setInstall_message(summeryjsonObject.getString("install_message"));
                mAppHandler.setInstall_url(summeryjsonObject.getString("install_url"));
                return mAppHandler;
            }
        }).setCallback(new DsAsyncTaskCallback<AppHandler>() {
            @Override
            public void onPreExecute() {
            }
            @Override
            public void onPostExecute(AppHandler result) {
                if (mResultListener != null) {
                    mResultListener.OnComplete(result);
                }
            }
            @Override
            public void onCancelled() {
            }
            @Override
            public void Exception(Exception e) {
                if (mResultListener != null) {
                    mResultListener.OnComplete(null);
                }
            }
        }).execute();
        return this;
    }

    @Override
    public <T> BaseData getViewPost(T post) {
        return this;
    }

    @Override
    public <T> BaseData setCallBack(T resultListener) {
        mResultListener = (BaseResultListener) resultListener;
        return this;
    }
}
