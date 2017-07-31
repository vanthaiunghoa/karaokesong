package kr.ds.fragment;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.android.gms.vision.text.Text;
import com.nhaarman.listviewanimations.appearance.simple.AlphaInAnimationAdapter;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;
import kr.ds.adapter.RecordAdapter;
import kr.ds.config.Config;
import kr.ds.data.BaseResultListener;
import kr.ds.data.RecordData;
import kr.ds.db.BookMarkDB;
import kr.ds.db.RecordDB;
import kr.ds.handler.RecordHandler;
import kr.ds.karaokesong.R;
import kr.ds.karaokesong.SubActivity;
import kr.ds.utils.DsObjectUtils;


/**
 * Created by Administrator on 2016-08-31.
 */
public class RecordFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    private ArrayList<RecordHandler> mData = null;
    private ArrayList<RecordHandler> mMainData = null;
    private int mNumber = 10;
    private int mPage = 1;
    private int startPage = 0;
    private int endPage = 0;

    private View mView;
    private ProgressBar mProgressBar;
    private ListView mListView;
    private RecordData mRecordData;
    private RecordAdapter mRecordAdapter;
    private int mCurrentScrollState;
    private Boolean mIsTheLoding = false;
    private SwipeRefreshLayout mSwipeLayout;

    private final static int LIST = 0;
    private final static int ONLOAD = 1;
    private final static int REFRESH = 2;
    private Context mContext;

    private Button mButton;
    private MediaPlayer mMediaPlayer;
    private RecordDB mRecordDB;

    private TextView mTextViewTitle, mTextViewTime;
    private ImageButton mImageButtonPause;
    private ImageButton mImageButtonStop;

    private TextView mTextViewTopName;


    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        super.onAttach(activity);
        mContext = getActivity();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_record_list, null);
        mTextViewTopName = (TextView)mView.findViewById(R.id.textView_top_name);
        mTextViewTime = (TextView) mView.findViewById(R.id.textView_time);
        mTextViewTitle = (TextView) mView.findViewById(R.id.textView_title);
        mTextViewTitle.setSelected(true);
        (mImageButtonPause = (ImageButton)mView.findViewById(R.id.imagebutton_pause)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mMediaPlayer != null) {
                    if (mMediaPlayer.isPlaying()) {
                        mMediaPlayer.pause();
                        mImageButtonPause.setImageResource(R.drawable.btn_play);
                    }else{
                        mMediaPlayer.start();
                        mImageButtonPause.setImageResource(R.drawable.btn_pause);
                    }
                }

            }
        });
        (mImageButtonStop = (ImageButton)mView.findViewById(R.id.imagebutton_stop)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mMediaPlayer != null) {
                    mMediaPlayer.stop();
                    mMediaPlayer.reset();
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                    setLayoutReset();
                }

            }
        });


        mListView = (ListView)mView.findViewById(R.id.listView);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                try {
                    if(!DsObjectUtils.isEmpty(mData.get(position).getTitle())){
                        mTextViewTitle.setText(mData.get(position).getTitle());
                    }
                    if(mMediaPlayer != null) {
                        mMediaPlayer.stop();
                        mMediaPlayer.reset();
                        mMediaPlayer.release();
                        mMediaPlayer = null;
                    }
                    mImageButtonPause.setAlpha(1f);
                    mImageButtonStop.setAlpha(1f);
                    mMediaPlayer = new MediaPlayer();
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    FileInputStream fileInputStream = new FileInputStream(mData.get(position).getUrl_file());
                    FileDescriptor fileDescriptor = fileInputStream.getFD();
                    mMediaPlayer.setDataSource(fileDescriptor);

                    mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mMediaPlayer.start();
                        }
                    });
                    mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            if (mMediaPlayer != null) {
                                mMediaPlayer.stop();
                                mMediaPlayer.reset();
                                mMediaPlayer.release();
                                mMediaPlayer = null;
                                setLayoutReset();
                            }
                        }
                    });
                    mMediaPlayer.prepareAsync();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        mProgressBar = (ProgressBar)mView.findViewById(R.id.progressBar);
        mSwipeLayout = (SwipeRefreshLayout) mView.findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeResources(R.color.colorPrimary);
        return mView;
    }
    public void setLayoutReset(){
        mTextViewTitle.setText("");
        mImageButtonPause.setAlpha(0.2f);
        mImageButtonStop.setAlpha(0.2f);
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
                if (firstVisibleItem == 0 && topRowVerticalPosition >= 0) {
                    mSwipeLayout.setEnabled(true);
                } else {
                    mSwipeLayout.setEnabled(false);
                }
                boolean loadMore = firstVisibleItem + visibleItemCount >= totalItemCount;
                if(!mIsTheLoding && loadMore &&  mCurrentScrollState != SCROLL_STATE_IDLE){
                    mIsTheLoding = true;
                    onLoadMore();
                }
            }
        });
//        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                try {
//                    mRecordDB = new RecordDB(mContext);
//                    mRecordDB.open();
//                    mRecordDB.deleteNote(mData.get(position).getContents_id());
//                    mData.remove(position);
//                    mRecordAdapter.notifyDataSetChanged();
//                    mRecordDB.close();
//                    Toast.makeText(mContext, "내노래 삭제 되었습니다.", Toast.LENGTH_SHORT).show();
//                } catch (Exception e) {
//                    Toast.makeText(mContext, "오류가 발생 되었습니다. 계속 문제가 발생시 관리자에게 문의 해주시기 바랍니다.", Toast.LENGTH_SHORT).show();
//                }
//                return false;
//            }
//        });
    }

    public void setList(){

        new RecordData(mContext).clear().setCallBack(new BaseResultListener() {
            @Override
            public <T> void OnComplete() {

            }
            @Override
            public <T> void OnComplete(Object data) {
                mProgressBar.setVisibility(View.GONE);
                mIsTheLoding = false;
                if(data != null){
                    mPage = 1;

                    mMainData = (ArrayList<RecordHandler>) data;
                    getTitle(String.valueOf(mMainData.size()));
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
                        mRecordAdapter = new RecordAdapter(mContext, mData);
                        AlphaInAnimationAdapter mAlphaInAnimationAdapter = new AlphaInAnimationAdapter(mRecordAdapter);
                        mAlphaInAnimationAdapter.setAbsListView(mListView);
                        mListView.setAdapter(mAlphaInAnimationAdapter);
                    }
                }else{
                    getTitle("0");
                    mListView.setAdapter(null);
                }
            }

            @Override
            public void OnMessage(String str) {

            }
        }).setUrl("").setParam("").getView();
    }

    public void setListRefresh(){
        new RecordData(mContext).clear().setCallBack(new BaseResultListener() {
            @Override
            public <T> void OnComplete() {

            }
            @Override
            public <T> void OnComplete(Object data) {
                mSwipeLayout.setRefreshing(false);
                mIsTheLoding = false;
                if(data != null){
                    mPage = 1;

                    mMainData = (ArrayList<RecordHandler>) data;
                    getTitle(String.valueOf(mMainData.size()));
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
                        mRecordAdapter = new RecordAdapter(mContext, mData);
                        AlphaInAnimationAdapter mAlphaInAnimationAdapter = new AlphaInAnimationAdapter(mRecordAdapter);
                        mAlphaInAnimationAdapter.setAbsListView(mListView);
                        mListView.setAdapter(mAlphaInAnimationAdapter);
                    }
                }else{
                    getTitle("0");
                    mListView.setAdapter(null);
                }
            }

            @Override
            public void OnMessage(String str) {

            }
        }).setUrl("").setParam("").getView();
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
            mRecordAdapter.notifyDataSetChanged();
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
    public void getTitle(String count){
        mTextViewTopName.setText("내노래"+"("+count+")");
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i("TEST", "onDestroyView()");
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMediaPlayer != null) {
            if(mMediaPlayer.isPlaying()){
                mMediaPlayer.pause();
            }
        }
    }

    @Override
    public void Tab(int tab) {

        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            setLayoutReset();
        }

        if(tab == 6) {

        }

    }

}
