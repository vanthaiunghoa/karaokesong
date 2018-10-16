package kr.ds.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;
import com.nhaarman.listviewanimations.appearance.simple.AlphaInAnimationAdapter;

import java.util.ArrayList;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;
import kr.ds.adapter.ChannelAdapter;
import kr.ds.config.Config;
import kr.ds.data.BaseResultListener;
import kr.ds.data.ChannelData;
import kr.ds.handler.ChannelHandler;
import kr.ds.karaokesong.ChannelListActivity;
import kr.ds.karaokesong.R;
import kr.ds.karaokesong.SubActivity;
import kr.ds.widget.AdAdmobNativeAdvancedView;


/**
 * Created by Administrator on 2016-08-31.
 */
public class ChannelFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    private ArrayList<ChannelHandler> mData;
    private ArrayList<ChannelHandler> mMainData;
    private int mNumber = 10;
    private int mPage = 1;
    private int startPage = 0;
    private int endPage = 0;

    private View mView;
    private ProgressBar mProgressBar;
    private ListView mListView;
    private ChannelData mChannelData;
    private ChannelAdapter mChannelAdapter;
    private int mCurrentScrollState;
    private Boolean mIsTheLoding = false;
    private SwipeRefreshLayout mSwipeLayout;

    private final static int LIST = 0;
    private final static int ONLOAD = 1;
    private final static int REFRESH = 2;
    private Context mContext;

    private String mParam = "";
    private FabSpeedDial mFabSpeedDial;

    private String mList_type = "1";

    private LinearLayout mLinearLayoutNative;
    private AdAdmobNativeAdvancedView mAdAdmobNativeAdvancedView;
    private boolean isNative = false;


    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        super.onAttach(activity);
        mContext = getActivity();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        mView = inflater.inflate(R.layout.fragment_channel, null);
        mListView = (ListView)mView.findViewById(R.id.listView);
        mLinearLayoutNative = (LinearLayout) inflater.inflate(R.layout.native_container, null);
        if(Config.isAd) {
            mAdAdmobNativeAdvancedView = new AdAdmobNativeAdvancedView(mContext);
            mAdAdmobNativeAdvancedView.setContainer(mLinearLayoutNative).setLayout(AdAdmobNativeAdvancedView.CONTET).setCallBack(new AdAdmobNativeAdvancedView.ResultListener() {
                @Override
                public <T> void OnLoad() {
                    mListView.addHeaderView(mLinearLayoutNative);
                    isNative = true;
                }

                @Override
                public <T> void OnFail() {
                    mListView.removeHeaderView(mLinearLayoutNative);
                    isNative = false;
                }
            });
        }else{
            mListView.removeHeaderView(mLinearLayoutNative);
            isNative = false;
        }

        mFabSpeedDial = (FabSpeedDial) mView.findViewById(R.id.fab_speed_dial);
        mFabSpeedDial.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                //TODO: Start some activity
                switch (menuItem.getItemId()){
                    case R.id.action_1:
                        mList_type = "1";
                        mParam = "?list_type="+mList_type;
                        mProgressBar.setVisibility(View.VISIBLE);
                        setList();
                        break;
                    case R.id.action_2:
                        mList_type = "2";
                        mParam = "?list_type="+mList_type;
                        mProgressBar.setVisibility(View.VISIBLE);
                        setList();
                        break;
                }
                return false;
            }
        });


        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                int header_position;
                if(isNative){
                    header_position = position-1;
                }else{
                    header_position = position;
                }
                Intent intent = new Intent(mContext, ChannelListActivity.class);
                intent.putExtra("data", mData.get(header_position));
                startActivity(intent);

            }
        });

        mProgressBar = (ProgressBar)mView.findViewById(R.id.progressBar);
        mSwipeLayout = (SwipeRefreshLayout) mView.findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeResources(R.color.colorPrimary);
        return mView;
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
        mParam = "?list_type="+mList_type;
        mProgressBar.setVisibility(View.VISIBLE);
        setList();
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // TODO Auto-generated method stub
                mCurrentScrollState = scrollState;
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                // TODO Auto-generated method stub
                int topRowVerticalPosition = (mListView == null || mListView.getChildCount() == 0) ? 0 : mListView.getChildAt(0).getTop();
                if(mData != null ){
                    mSwipeLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
                }else{
                    mSwipeLayout.setEnabled(false);
                }
                boolean loadMore = firstVisibleItem + visibleItemCount >= totalItemCount;
                if(!mIsTheLoding && loadMore &&  mCurrentScrollState != SCROLL_STATE_IDLE){
                    mIsTheLoding = true;
                    onLoadMore();
                }
            }
        });
    }

    public void setList(){

        new ChannelData().clear().setCallBack(new BaseResultListener() {
            @Override
            public <T> void OnComplete() {

            }
            @Override
            public <T> void OnComplete(Object data) {
                mProgressBar.setVisibility(View.GONE);
                mIsTheLoding = false;
                if(data != null){
                    mPage = 1;

                    mMainData = (ArrayList<ChannelHandler>) data;
                    if(mMainData.size() - ((mPage-1)*mNumber) > 0){
                        if(mMainData.size() >= mPage * mNumber){
                            startPage = (mPage-1) * mNumber;
                            endPage = mPage * mNumber;
                        }else{
                            startPage = (mPage-1) * mNumber;
                            endPage = mMainData.size();
                        }
                        mData  = new ArrayList<>();
                        for(int i=startPage; i< endPage; i++){
                            mData.add(mMainData.get(i));
                        }
                        mChannelAdapter = new ChannelAdapter(mContext, mData);
                        AlphaInAnimationAdapter mAlphaInAnimationAdapter = new AlphaInAnimationAdapter(mChannelAdapter);
                        mAlphaInAnimationAdapter.setAbsListView(mListView);
                        mListView.setAdapter(mAlphaInAnimationAdapter);
                    }
                }else{
                    mListView.setAdapter(null);
                }
            }

            @Override
            public void OnMessage(String str) {

            }
        }).setUrl(Config.URL+ Config.URL_XML+ Config.URL_CHANNEL).setParam(mParam).getView();
    }

    public void setListRefresh(){
        new ChannelData().clear().setCallBack(new BaseResultListener() {
            @Override
            public <T> void OnComplete() {

            }
            @Override
            public <T> void OnComplete(Object data) {
                mSwipeLayout.setRefreshing(false);
                mIsTheLoding = false;
                if(data != null){
                    mPage = 1;

                    mMainData = (ArrayList<ChannelHandler>) data;
                    if(mMainData.size() - ((mPage-1)*mNumber) > 0){
                        if(mMainData.size() >= mPage * mNumber){
                            startPage = (mPage-1) * mNumber;
                            endPage = mPage * mNumber;
                        }else{
                            startPage = (mPage-1) * mNumber;
                            endPage = mMainData.size();
                        }
                        mData  = new ArrayList<>();
                        for(int i=startPage; i< endPage; i++){
                            mData.add(mMainData.get(i));
                        }
                        mChannelAdapter = new ChannelAdapter(mContext, mData);
                        AlphaInAnimationAdapter mAlphaInAnimationAdapter = new AlphaInAnimationAdapter(mChannelAdapter);
                        mAlphaInAnimationAdapter.setAbsListView(mListView);
                        mListView.setAdapter(mAlphaInAnimationAdapter);
                    }
                }else{
                    mListView.setAdapter(null);
                }
            }

            @Override
            public void OnMessage(String str) {

            }
        }).setUrl(Config.URL+ Config.URL_XML+ Config.URL_CHANNEL).setParam(mParam).getView();
    }

    public void setListOnLoad(){
        mPage++;
        if(mMainData.size() - ((mPage-1)*mNumber) < 0){
            mIsTheLoding = true;
        }else{
            if(mMainData.size() >= mPage * mNumber){
                startPage = (mPage-1) * mNumber;
                endPage = mPage * mNumber;
            }else{
                startPage = (mPage-1) * mNumber;
                endPage = mMainData.size();
            }
            for(int i=startPage; i< endPage; i++){
                mData.add(mMainData.get(i));
            }
            mChannelAdapter.notifyDataSetChanged();
            mIsTheLoding = false;
        }
        mProgressBar.setVisibility(View.GONE);
    }
    @Override
    public void onRefresh() {
        setListRefresh();
        // TODO Auto-generated method stub
    }

    public void onLoadMore(){

        mProgressBar.setVisibility(View.VISIBLE);
        setListOnLoad();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();



    }

    @Override
    public void Tab(int tab) {

    }
}
