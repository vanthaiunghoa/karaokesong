package kr.ds.karaokesong;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.ads.AbstractAdListener;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.android.gms.ads.VideoController;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.nhaarman.listviewanimations.appearance.simple.AlphaInAnimationAdapter;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import kr.ds.adapter.ListAdapter;
import kr.ds.config.Config;
import kr.ds.data.BaseResultListener;
import kr.ds.data.ListData;
import kr.ds.data.LogData;
import kr.ds.db.BookMarkDB;
import kr.ds.db.RecordDB;
import kr.ds.handler.ListHandler;
import kr.ds.utils.DsObjectUtils;
import kr.ds.utils.SharedPreference;
import kr.ds.view.VisualizerView;
import kr.ds.widget.AdAdmobNativeView;
import kr.ds.widget.AdFaceBookNativeView;
import kr.ds.widget.ScrollListView;
import omrecorder.AudioChunk;
import omrecorder.AudioSource;
import omrecorder.OmRecorder;
import omrecorder.PullTransport;
import omrecorder.Recorder;

/**
 * Created by Administrator on 2017-06-12.
 */
public class SubActivity extends BaseActivity implements YouTubePlayer.OnInitializedListener, View.OnClickListener{
    private Toolbar mToolbar;
    private AudioSource mic() {
        return new AudioSource.Smart(MediaRecorder.AudioSource.MIC, AudioFormat.ENCODING_PCM_16BIT,
                AudioFormat.CHANNEL_IN_MONO, 44100);
    }
    private File file(String filename) {

        if (FileExists(Environment.getExternalStorageDirectory().getAbsolutePath()+"/karaokesong/") == false) {
            SetMkdirs(Environment.getExternalStorageDirectory().getAbsolutePath()+"/karaokesong/");
        }
        mUrlFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/karaokesong/", filename+".wav").getAbsolutePath();
        return new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/karaokesong/", filename+".wav");
    }

    private boolean FileExists(String url) {
        File files = new File(url);
        Boolean check;
        if (files.exists() == true) {
            check = true;
        } else {
            check = false;
        }
        return check;
    }

    private void SetMkdirs(String url) {
        try {
            File path = new File(url);
            if (!path.isDirectory()) {
                path.mkdirs();
            }
        } catch (Exception e) {
        }
    }

    private void animateVoice(final float maxPeak) {
        //mButton.animate().scaleX(1 + maxPeak).scaleY(1 + maxPeak).setDuration(10).start();
    }

    private void setupRecorder() {
        Calendar calendar = new GregorianCalendar(Locale.KOREA);
        SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String strDate = mSavedata.getTitle().trim()+"_"+fm.format(calendar.getTime());
        strDate = strDate.replace("/","");
        recorder = OmRecorder.wav(
                new PullTransport.Default(mic(), new PullTransport.OnAudioChunkPulledListener() {
                    @Override public void onAudioChunkPulled(AudioChunk audioChunk) {
                        animateVoice((float) (audioChunk.maxAmplitude() / 200.0));
                    }
                }), file(strDate));
    }



    private ListHandler mSavedata;
    public static final String API_KEY = "AIzaSyAkfPX3uF_hALFjYOUhwlhewgaqewl08XE";

    private VisualizerView mVisualizerView;
    private VisualizerView mVisualizerView2;
    private TimerTask mTimerTask, mTimerTask2;
    private Timer mTimer, mTimer2 = null;
    private YouTubePlayer mYouTubePlayer;
    private boolean isPlaying = false;

    private TextView mTextViewTitle;

    private ScrollListView mListView;
    private ListAdapter mListAdapter;
    private ArrayList<ListHandler> mData;

    private boolean isVisualizerView = false;
    private boolean isVisualizerView2 = false;


    private Recorder recorder;


    private LinearLayout mLinearLayoutShare;
    private LinearLayout mLinearLayoutBookMark;
    private LinearLayout mLinearLayoutLike;

    private BookMarkDB mBookMarkDB;
    private Cursor mCursor;
    private ImageView mImageViewBookMark;

    private RecordDB mRecordDB;
    private String mUrlFile = "";
    private TextView mTopName;

    private ImageButton mImageButtonRecord;
    private ImageButton mImageButtonPause;
    private ImageButton mImageButtonStop;

    private boolean isRecored = false;
    private Boolean isPause = false;


    private static int RECORD = 0;
    private static int PAUSE = 1;
    private static int STOP = 2;

    private TextView mTextViewTime;
    private int time = 0;

    //private LinearLayout mLinearLayoutNative;
    //private AdFaceBookNativeView mAdFaceBookNativeView;
    //private AdAdmobNativeView mAdAdmobNativeView;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBookMarkDB = new BookMarkDB(getApplicationContext());
        mRecordDB = new RecordDB(getApplicationContext());
        if(savedInstanceState != null){
            mSavedata = (ListHandler) savedInstanceState.getParcelable("data");
        }else{
            mSavedata = (ListHandler) getIntent().getParcelableExtra("data");
        }

        setContentView(R.layout.activty_sub);
//        mLinearLayoutNative = (LinearLayout) getLayoutInflater().inflate(R.layout.native_container, null);
//        mAdFaceBookNativeView = new AdFaceBookNativeView(getApplicationContext());
//        mAdFaceBookNativeView.setContainer(mLinearLayoutNative).setLayout(R.layout.native_facebook2).setCallBack(new AdFaceBookNativeView.ResultListener() {
//            @Override
//            public <T> void OnLoad() {
//
//            }
//            @Override
//            public <T> void OnFail() {
//                if(mLinearLayoutNative.getChildCount() > 0) {
//                    mLinearLayoutNative.removeAllViews();
//                }
//                mAdAdmobNativeView = new AdAdmobNativeView(getApplicationContext());
//                mAdAdmobNativeView.setContainer(mLinearLayoutNative).setLayout().setCallBack(new AdAdmobNativeView.ResultListener() {
//                    @Override
//                    public <T> void OnLoad() {
//
//                    }
//
//                    @Override
//                    public <T> void OnFail() {
//                        mLinearLayoutNative.setVisibility(View.GONE);
//                    }
//                });
//            }
//        });
        setLog();
        mTopName = (TextView) findViewById(R.id.textView_top_name);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        mTextViewTime = (TextView)findViewById(R.id.textView_time);

        if (mToolbar != null) {
            if(!DsObjectUtils.getInstance(getApplicationContext()).isEmpty(mSavedata.getTitle())) {
                mTopName.setText(mSavedata.getTitle());
            }else{
                mTopName.setText(getResources().getString(R.string.app_name));
            }
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mImageViewBookMark = (ImageView)findViewById(R.id.imageView_bookmark);
        (mLinearLayoutShare = (LinearLayout) findViewById(R.id.linearLayout_share)).setOnClickListener(this);
        (mLinearLayoutBookMark = (LinearLayout) findViewById(R.id.linearLayout_bookmark)).setOnClickListener(this);
        (mLinearLayoutLike = (LinearLayout) findViewById(R.id.linearLayout_like)).setOnClickListener(this);
        mTextViewTime = (TextView)findViewById(R.id.textView_time);

        (mImageButtonRecord = (ImageButton) findViewById(R.id.imagebutton_record)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isRecored ) {
                    if (!isPause){
                        if(mTimer2 == null){
                            mTimerTask2 = new TimerTask() {
                                @Override
                                public void run() {
                                    Time();
                                }
                            };
                            time = 0;
                            mTimer2 = new Timer();
                            mTimer2.schedule(mTimerTask2, 0, 1000);
                        }
                        isRecored = true;
                        setAlpha(RECORD);
                        setupRecorder();//파일 새로 시작!!
                        Calendar calendar = new GregorianCalendar(Locale.KOREA);
                        SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        mRecordDB.open();
                        mRecordDB.createNote(mSavedata.getDd_uid(), mSavedata.getImage(), mSavedata.getTitle(), mSavedata.getVideo_id(), mUrlFile, fm.format(calendar.getTime()));
                        Toast.makeText(getApplicationContext(), "녹음이 시작 되었습니다.", Toast.LENGTH_SHORT).show();
                        SharedPreference.putSharedPreference(getApplicationContext(), Config.RECORD_REFRASH, true);
                        mCursor.close();
                        mRecordDB.close();
                        if (recorder == null) {
                            return;
                        }
                        recorder.startRecording();
                    }else{
                        if(mTimer2 == null){//화면다시 시작할경우
                            mTimerTask2 = new TimerTask() {
                                @Override
                                public void run() {
                                    Time();
                                }
                            };
                            mTimer2 = new Timer();
                            mTimer2.schedule(mTimerTask2, 0, 1000);
                        }
                        isRecored = true;
                        isPause = false;
                        setAlpha(RECORD);
                        Toast.makeText(getApplicationContext(), "녹음이 다시시작 되었습니다.", Toast.LENGTH_SHORT).show();
                        if (recorder == null) {
                            return;
                        }
                        recorder.resumeRecording();
                    }
                }
            }
        });

        (mImageButtonPause = (ImageButton) findViewById(R.id.imagebutton_pause)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isRecored) {//녹음중일때
                    isRecored = false;
                    isPause = true;
                    setAlpha(PAUSE);
                    Toast.makeText(getApplicationContext(), "녹음 일시정지 되었습니다.", Toast.LENGTH_SHORT).show();
                    if (recorder == null) {
                        return;
                    }
                    recorder.pauseRecording();
                }
            }
        });

        (mImageButtonStop = (ImageButton) findViewById(R.id.imagebutton_stop)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecored || isPause) {
                    try {
                        if(mTimer2 != null){
                            mTimer2.cancel();
                            mTimer2 = null;
                            mTimerTask2.cancel();
                            mTimerTask2 = null;
                            mTextViewTime.setText("00:00:00");
                        }
                        isRecored = false;
                        isPause = false;
                        setAlpha(STOP);
                        Toast.makeText(getApplicationContext(), "내 노래에서 녹음된 곡을 확인 해주시기 바랍니다. 감사합니다.", Toast.LENGTH_LONG).show();
                        if(recorder == null) {
                            return;
                        }
                        recorder.stopRecording();
                        Log.i("TEST","stopRecording()");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mListView = (ScrollListView) findViewById(R.id.listView);
        //mListView.addHeaderView(mLinearLayoutNative);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(getApplicationContext(), SubActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("data", mData.get(position));
                startActivity(intent);
            }
        });
        mTextViewTitle = (TextView) findViewById(R.id.textView_title);
        mVisualizerView = (VisualizerView) findViewById(R.id.visualizer);
        ViewTreeObserver observer = mVisualizerView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mVisualizerView.setBaseY(mVisualizerView.getHeight());
                isVisualizerView = true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mVisualizerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    mVisualizerView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });

        mVisualizerView2 = (VisualizerView) findViewById(R.id.visualizer2);
        ViewTreeObserver observer2 = mVisualizerView2.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                isVisualizerView2 = true;
                mVisualizerView2.setBaseY(mVisualizerView2.getHeight());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mVisualizerView2.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    mVisualizerView2.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });
        FragmentManager fm = getFragmentManager();
        String tag = YouTubePlayerFragment.class.getSimpleName();
        YouTubePlayerFragment playerFragment = (YouTubePlayerFragment) fm.findFragmentByTag(tag);
        if (playerFragment == null) {
            FragmentTransaction ft = fm.beginTransaction();
            playerFragment = YouTubePlayerFragment.newInstance();
            ft.add(R.id.content_frame, playerFragment, tag);
            ft.commit();
        }
        playerFragment.initialize(API_KEY, this);

        new ListData().clear().setCallBack(new BaseResultListener() {
            @Override
            public <T> void OnComplete() {
            }
            @Override
            public <T> void OnComplete(Object data) {

                if(data != null){
                    mData = (ArrayList<ListHandler>) data;
                    mListAdapter = new ListAdapter(getApplicationContext(), mData);
                    AlphaInAnimationAdapter mAlphaInAnimationAdapter = new AlphaInAnimationAdapter(mListAdapter);
                    mAlphaInAnimationAdapter.setAbsListView(mListView);
                    mListView.setAdapter(mAlphaInAnimationAdapter);
                }else{
                    mListView.setAdapter(null);
                }
            }

            @Override
            public void OnMessage(String str) {

            }
        }).setUrl(Config.URL+ Config.URL_XML+ Config.URL_RECOM).setParam("").getView();

        setAlpha(STOP);
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                Update();
            }
        };
        mTimer = new Timer();
        mTimer.schedule(mTimerTask, 0, 500);

        mBookMarkDB.open();
        mCursor = mBookMarkDB.BookMarkConfirm(mSavedata.getDd_uid());
        if(mCursor.getCount() > 0){
            mImageViewBookMark.setImageResource(R.drawable.icon_book_on);
        }else{
            mImageViewBookMark.setImageResource(R.drawable.icon_book_off);
        }
        mCursor.close();
        mBookMarkDB.close();

        if(!DsObjectUtils.isEmpty(mSavedata.getTitle())){
            mTextViewTitle.setText(mSavedata.getTitle());
        }
    }
    public void setAlpha(int type){
        if(type == RECORD){
            mImageButtonRecord.setAlpha(0.2f);
            mImageButtonPause.setAlpha(1f);
            mImageButtonStop.setAlpha(1f);
        }else if(type == PAUSE){
            mImageButtonRecord.setAlpha(1f);
            mImageButtonPause.setAlpha(0.2f);
            mImageButtonStop.setAlpha(1f);
        }else if(type == STOP){
            mImageButtonRecord.setAlpha(1f);
            mImageButtonPause.setAlpha(0.2f);
            mImageButtonStop.setAlpha(0.2f);
        }
    }

    public void setLog(){
        new LogData(getApplicationContext()).clear().setCallBack(new BaseResultListener() {
            @Override
            public <T> void OnComplete() {

            }

            @Override
            public <T> void OnComplete(Object data) {

            }

            @Override
            public void OnMessage(String str) {

            }
        }).setUrl(Config.URL+ Config.URL_XML+ Config.URL_LOG).setParam("?dd_uid="+mSavedata.getDd_uid()).getView();
    }

    protected void Time() {
        if(isRecored) {
            time++;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String si = (String.valueOf(time / 3600).length() == 1) ? ("0" + (time / 3600)) : String.valueOf(time / 3600);
                    String minute = (String.valueOf(time % 3600 / 60).length() == 1) ? ("0" + (time % 3600 / 60)) : String.valueOf(time % 3600 / 60);
                    String second = (String.valueOf(time % 3600 % 60).length() == 1) ? ("0" + (time % 3600 % 60)) : String.valueOf(time % 3600 % 60);

                    mTextViewTime.setText(si + ":"+ minute + ":" + second);
                }
            });
        }
    }

    protected void Update() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(isVisualizerView && isVisualizerView2) {
                    if (isPlaying) {
                        mVisualizerView.receive(new Random().nextInt(100) + 1);
                        mVisualizerView2.receive(new Random().nextInt(100) + 1);
                    } else {
                        mVisualizerView.receive(0);
                        mVisualizerView2.receive(0);
                    }
                }
            }
        });
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        mYouTubePlayer = youTubePlayer;
        if(!DsObjectUtils.isEmpty(mSavedata.getVideo_id())){
            if (SharedPreference.getBooleanSharedPreference(getApplicationContext(), Config.YOUTUBE_AUTO_PLAY)) {
                mYouTubePlayer.loadVideo(mSavedata.getVideo_id());
            } else {
                mYouTubePlayer.cueVideo(mSavedata.getVideo_id());
            }
        }
        mYouTubePlayer.setPlaybackEventListener(new YouTubePlayer.PlaybackEventListener() {
            @Override
            public void onPlaying() {
                Log.i("TEST","onPlaying");
                isPlaying = true;
            }

            @Override
            public void onPaused() {
                Log.i("TEST","onStopped");
                isPlaying = false;
            }
            @Override
            public void onStopped() {
                Log.i("TEST","onStopped");
                isPlaying = false;
            }

            @Override
            public void onBuffering(boolean b) {
            }

            @Override
            public void onSeekTo(int i) {
            }
        });
        mYouTubePlayer.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
            @Override
            public void onLoading() {
                Log.i("TEST", "onLoading");
            }


            @Override
            public void onLoaded(String s) {
                Log.i("TEST", "onLoaded");
            }

            @Override
            public void onAdStarted() {
                Log.i("TEST", "onAdStarted");
            }

            @Override
            public void onVideoStarted() {//최초시작
                if(SharedPreference.getBooleanSharedPreference(getApplicationContext(), Config.YOUTUBE_PLAY_RECORD)) {
                    if(!isRecored ) {
                        if (!isPause) {
                            if (mTimer2 == null) {
                                mTimerTask2 = new TimerTask() {
                                    @Override
                                    public void run() {
                                        Time();
                                    }
                                };
                                time = 0;
                                mTimer2 = new Timer();
                                mTimer2.schedule(mTimerTask2, 0, 1000);
                            }
                            isRecored = true;
                            setAlpha(RECORD);
                            setupRecorder();//파일 새로 시작!!
                            Calendar calendar = new GregorianCalendar(Locale.KOREA);
                            SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            mRecordDB.open();
                            mRecordDB.createNote(mSavedata.getDd_uid(), mSavedata.getImage(), mSavedata.getTitle(), mSavedata.getVideo_id(), mUrlFile, fm.format(calendar.getTime()));
                            Toast.makeText(getApplicationContext(), "녹음 시작 되었습니다.", Toast.LENGTH_SHORT).show();
                            mCursor.close();
                            mRecordDB.close();
                            if (recorder == null) {
                                return;
                            }
                            recorder.startRecording();
                        }
                    }
                }
                Log.i("TEST", "onVideoStarted");
            }

            @Override
            public void onVideoEnded() {
                Log.i("TEST", "onVideoEnded");
                if(SharedPreference.getBooleanSharedPreference(getApplicationContext(), Config.YOUTUBE_PLAY_END_RECORD_END)) {
                    if (isRecored || isPause) {
                        try {
                            if(mTimer2 != null){
                                mTimer2.cancel();
                                mTimer2 = null;
                                mTimerTask2.cancel();
                                mTimerTask2 = null;
                                mTextViewTime.setText("00:00:00");
                            }
                            isRecored = false;
                            isPause = false;
                            setAlpha(STOP);
                            Toast.makeText(getApplicationContext(), "내 노래에서 녹음된 곡을 확인 해주시기 바랍니다. 감사합니다.", Toast.LENGTH_LONG).show();
                            if(recorder == null) {
                                return;
                            }
                            recorder.stopRecording();
                            Log.i("TEST","stopRecording()");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onError(YouTubePlayer.ErrorReason errorReason) {

            }
        });
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
    }

    private void SendMMS() {
        if(!DsObjectUtils.isEmpty(mSavedata.getTitle()) && !DsObjectUtils.isEmpty(mSavedata.getDd_uid())) {
            try {
                Intent NextIntent = new Intent(Intent.ACTION_SEND);
                NextIntent.setType("text/plain");
                NextIntent.putExtra(Intent.EXTRA_SUBJECT, mSavedata.getTitle());
                NextIntent.putExtra(Intent.EXTRA_TEXT, "반갑습니다.^^ 무료노래방 입니다.\n\n " +
                        "동영상:\n" + "https://www.youtube.com/watch?v="+mSavedata.getVideo_id() +
                        "\n\n어플다운:\n" + "https://play.google.com/store/apps/details?id=kr.ds.karaokesong" +
                "\n\n많은 다운 부탁드립니다.");
                startActivity(Intent.createChooser(NextIntent, mSavedata.getTitle() + "공유하기"));
            } catch (Exception e) {
                // TODO: handle exception
                Log.i("TEST", e.toString() + "");
            }
        }else {
            Toast.makeText(getApplicationContext(),"계속 문제가 발생시 관리자에게 문의해주시기 바랍니다.",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.linearLayout_share:
                SendMMS();
                break;
            case R.id.linearLayout_bookmark:
                if(!DsObjectUtils.isEmpty(mSavedata.getDd_uid())) {
                    mBookMarkDB.open();
                    mCursor = mBookMarkDB.BookMarkConfirm(mSavedata.getDd_uid());
                    if (mCursor.getCount() == 0) {
                        mBookMarkDB.createNote(mSavedata.getDd_uid());
                        mImageViewBookMark.setImageResource(R.drawable.icon_book_on);
                        Toast.makeText(getApplicationContext(), R.string.bookmark_save, Toast.LENGTH_SHORT).show();
                    } else {
                        mBookMarkDB.deleteNote(mSavedata.getDd_uid());
                        mImageViewBookMark.setImageResource(R.drawable.icon_book_off);
                        Toast.makeText(getApplicationContext(), R.string.bookmark_cancel, Toast.LENGTH_SHORT).show();
                    }
                    mCursor.close();
                    mBookMarkDB.close();

                    setResult(RESULT_OK);
                }else{
                    Toast.makeText(getApplicationContext(),"계속 문제가 발생시 관리자에게 문의해주시기 바랍니다.",Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.linearLayout_like:

                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("data", mSavedata);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (isRecored) {//녹음중일때
            if(mTimer2 != null){
                mTimer2.cancel();
                mTimer2 = null;
                mTimerTask2.cancel();
                mTimerTask2 = null;
            }
            isRecored = false;
            isPause = true;
            setAlpha(PAUSE);
            Toast.makeText(getApplicationContext(), "녹음 일시정지 되었습니다.", Toast.LENGTH_SHORT).show();
            if (recorder == null) {
                return;
            }
            recorder.pauseRecording();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mYouTubePlayer != null) {
            mYouTubePlayer.release();
        }
        if(mTimer != null){
            mTimer.cancel();
            mTimer = null;
            mTimerTask.cancel();
            mTimerTask = null;
        }
        if (isRecored || isPause) {
            try {
                if(mTimer2 != null){
                    mTimer2.cancel();
                    mTimer2 = null;
                    mTimerTask2.cancel();
                    mTimerTask2 = null;
                    mTextViewTime.setText("00:00:00");
                }
                isRecored = false;
                isPause = false;
                setAlpha(STOP);
                Toast.makeText(getApplicationContext(), "녹음 중 뒤로가기 하여 녹음 정지 되었습니다. 내 노래에서 녹음된 곡을 확인 해주시기 바랍니다. 감사합니다.", Toast.LENGTH_SHORT).show();
                if(recorder == null) {
                    return;
                }
                recorder.stopRecording();
                Log.i("TEST","stopRecording()");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
