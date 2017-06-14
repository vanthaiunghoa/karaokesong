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
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import kr.ds.view.VisualizerView;
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
        SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String strDate = fm.format(calendar.getTime());

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
    private TimerTask mTimerTask;
    private Timer mTimer;
    private YouTubePlayer mYouTubePlayer;
    private boolean isPlaying = false;

    private TextView mTextViewTitle;

    private ScrollListView mListView;
    private ListAdapter mListAdapter;
    private ArrayList<ListHandler> mData;

    private boolean isVisualizerView = false;
    private boolean isVisualizerView2 = false;


    private Recorder recorder;
    private boolean isRecored = false;

    private LinearLayout mLinearLayoutShare;
    private LinearLayout mLinearLayoutBookMark;
    private LinearLayout mLinearLayoutLike;

    private BookMarkDB mBookMarkDB;
    private Cursor mCursor;
    private ImageView mImageViewBookMark;

    private RecordDB mRecordDB;
    private String mUrlFile = "";
    private TextView mTopName;
    private TextView mTextViewTime;
    private ImageButton mButton;
    private ImageButton mButtonStop;

    private Boolean isRecordStart = false;

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
        setLog();
        setContentView(R.layout.activty_sub);
        mTopName = (TextView) findViewById(R.id.textView_top_name);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
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
        (mButtonStop = (ImageButton) findViewById(R.id.button_stop)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isRecordStart && recorder != null){
                    try {
                        mButton.setImageResource(R.drawable.play);
                        Toast.makeText(getApplicationContext(), "녹음 정지 되었습니다.", Toast.LENGTH_SHORT).show();
                        isRecordStart = false;
                        isRecored = false;
                        recorder.stopRecording();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        (mButton = (ImageButton) findViewById(R.id.button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isRecordStart){
                    mButton.setImageResource(R.drawable.resume);
                    setupRecorder();//파일 새로 시작!!
                    isRecordStart = true;
                    isRecored = true;
                    Calendar calendar = new GregorianCalendar(Locale.KOREA);
                    SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    mRecordDB.open();
                    mRecordDB.createNote(mSavedata.getDd_uid(), mSavedata.getImage(), mSavedata.getTitle(), mSavedata.getVideo_id(), mUrlFile, fm.format(calendar.getTime()));
                    Toast.makeText(getApplicationContext(), "녹음 시작 되었습니다.", Toast.LENGTH_SHORT).show();
                    mCursor.close();
                    mRecordDB.close();
                    recorder.startRecording();
                }else {
                    if (isRecordStart) {//처음에는 실행안되고 두번째 누를때 실행
                        if (recorder == null) {
                            return;
                        }
                        if (isRecored) {//녹음중일때
                            Toast.makeText(getApplicationContext(), "녹음 일시정지 되었습니다.", Toast.LENGTH_SHORT).show();
                            mButton.setImageResource(R.drawable.play);
                            isRecored = false;
                            recorder.pauseRecording();
                        } else {//다시시작일때
                            Toast.makeText(getApplicationContext(), "녹음 다시시작 되었습니다.", Toast.LENGTH_SHORT).show();
                            mButton.setImageResource(R.drawable.resume);
                            isRecored = true;
                            recorder.resumeRecording();
                        }
                    }
                }
            }
        });

        mListView = (ScrollListView) findViewById(R.id.listView);
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

        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                Update();
            }
        };
        mTimer = new Timer();
        mTimer.schedule(mTimerTask, 0, 500);


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
        if(!DsObjectUtils.isEmpty(mSavedata.getTitle())){
            mTextViewTitle.setText(mSavedata.getTitle());
        }

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

        mBookMarkDB.open();
        mCursor = mBookMarkDB.BookMarkConfirm(mSavedata.getDd_uid());
        if(mCursor.getCount() > 0){
            mImageViewBookMark.setImageResource(R.drawable.icon_book_on);
        }else{
            mImageViewBookMark.setImageResource(R.drawable.icon_book_off);
        }
        mCursor.close();
        mBookMarkDB.close();

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

    protected void Update() {
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

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        mYouTubePlayer = youTubePlayer;
        if(!DsObjectUtils.isEmpty(mSavedata.getVideo_id())){
            mYouTubePlayer.loadVideo(mSavedata.getVideo_id());
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
            }

            @Override
            public void onLoaded(String s) {
            }

            @Override
            public void onAdStarted() {
            }

            @Override
            public void onVideoStarted() {
            }

            @Override
            public void onVideoEnded() {
            }
            @Override
            public void onError(YouTubePlayer.ErrorReason errorReason) {

            }
        });
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mTimer != null){
            mTimer.cancel();
            mTimer = null;
        }
        if(isRecored) {
            if (recorder != null) {
                try {
                    recorder.stopRecording();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void SendMMS() {
        if(!DsObjectUtils.isEmpty(mSavedata.getTitle()) && !DsObjectUtils.isEmpty(mSavedata.getDd_uid())) {
            try {
                Intent NextIntent = new Intent(Intent.ACTION_SEND);
                NextIntent.setType("text/plain");
                NextIntent.putExtra(Intent.EXTRA_SUBJECT, mSavedata.getTitle());
                NextIntent.putExtra(Intent.EXTRA_TEXT, "반갑습니다.^^ 무료노래방 입니다.\n\n 어플다운:\n" + "https://play.google.com/store/apps/details?id=kr.ds.karaokesong");
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
    protected void onPause() {
        super.onPause();
        if (isRecordStart) {//처음에는 실행안되고 두번째 누를때 실행
            if (recorder == null) {
                return;
            }
            if (isRecored) {//녹음중일때
                Toast.makeText(getApplicationContext(), "녹음 일시정지 되었습니다.", Toast.LENGTH_SHORT).show();
                mButton.setImageResource(R.drawable.play);
                isRecored = false;
                recorder.pauseRecording();
            }
        }
    }
}
