//package kr.ds.karaokesong;
//
//import android.content.ActivityNotFoundException;
//import android.content.BroadcastReceiver;
//import android.content.Intent;
//import android.database.Cursor;
//import android.location.Geocoder;
//import android.location.Location;
//import android.net.Uri;
//import android.os.Bundle;
//import android.support.annotation.Nullable;
//import android.support.v4.view.ViewPager;
//import android.support.v7.widget.Toolbar;
//import android.util.Log;
//import android.view.MenuItem;
//import android.view.View;
//import android.widget.AdapterView;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.ProgressBar;
//import android.widget.ScrollView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdView;
//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.GooglePlayServicesUtil;
//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.GoogleMapOptions;
//import com.google.android.gms.maps.LocationSource;
//import com.google.android.gms.maps.OnMapReadyCallback;
//import com.google.android.gms.maps.model.BitmapDescriptorFactory;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.MarkerOptions;
//import com.nhaarman.listviewanimations.appearance.simple.AlphaInAnimationAdapter;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.Locale;
//
//import kr.ds.adapter.SubListAdapter;
//import kr.ds.clipboard.ClipboardManager;
//import kr.ds.clipboard.OnChangeListner;
//import kr.ds.config.Config;
//import kr.ds.data.BaseResultListener;
//import kr.ds.data.SubData;
//import kr.ds.db.BookMarkDB;
//import kr.ds.dialog.LocationDialogFragment;
//import kr.ds.dialog.MarkerInfoAlertDialogFragment;
//import kr.ds.fragment.WorkaroundMapFragment;
//import kr.ds.handler.ListHandler;
//import kr.ds.handler.SubHandler;
//import kr.ds.handler.TravelListHandler;
//import kr.ds.map.FusedLocationService;
//import kr.ds.receiver.DsGpsStateReceiver;
//import kr.ds.utils.DsDebugUtils;
//import kr.ds.utils.DsMapUtils;
//import kr.ds.utils.DsObjectUtils;
//import kr.ds.utils.SharedPreference;
//import kr.ds.widget.ContentViewPager;
//import kr.ds.widget.ScrollListView;
//import kr.ds.widget.ViewpagerNavibar;
//
///**
// * Created by Administrator on 2016-11-21.
// */
//public class SubActivity extends BaseActivity implements OnMapReadyCallback, View.OnClickListener {
//    private Toolbar mToolbar;
//    private ContentViewPager mContetContentViewPager;
//
//    private ListHandler mSavedata;
//    private LinearLayout mLinearLayoutNavibar;
//    private ViewpagerNavibar mViewpagerNavibar;
//
//    private String mParam = "";
//    private String mTravelParam = "";
//    private ArrayList<SubHandler> mData;
//    private ArrayList<TravelListHandler> mTravelData;
//    private ArrayList mRanData = new ArrayList<>();
//
//    private TextView mTextView1;
//    private TextView mTextView2;
//    private TextView mTextView3;
//    private TextView mTextView4;
//    private TextView mTextView5;
//    private TextView mTextView6;
//    private TextView mTextView7;
//    private TextView mTextView8;
//
//    private SubListAdapter mSubListAdapter;
//    private ScrollListView mListView;
//    private ScrollView mScrollView;
//
//    private GoogleMap mGoogleMap;
//    private WorkaroundMapFragment mMapFragment;
//    private double mLat = 0;
//    private double mLon = 0;
//    private int mGoogleZoom = 16;
//
//    private LinearLayout mLinearLayoutMap;
//
//    private LinearLayout mLinearLayoutShare;
//    private LinearLayout mLinearLayoutBookMark;
//    private LinearLayout mLinearLayoutLocation;
//
//    private ImageView mImageViewBookMark;
//
//    private BookMarkDB mBookMarkDB;
//    private Cursor mCursor;
//
//
//    private double mMyLat = 0;
//    private double mMyLon = 0;
//    private FusedLocationService mFusedLocationService;
//    private BroadcastReceiver mGpsReveiver;
//
//    private ProgressBar mProgressBar;
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        mBookMarkDB = new BookMarkDB(getApplicationContext());
//
//        if(savedInstanceState != null){
//            mSavedata = (ListHandler) savedInstanceState.getParcelable("data");
//        }else{
//            mSavedata = (ListHandler) getIntent().getParcelableExtra("data");
//        }
//        mFusedLocationService = new FusedLocationService(getApplicationContext());
//
//        setContentView(R.layout.activty_sub);
//
//        AdView mAdView = (AdView) findViewById(R.id.adView);
//        mAdView.loadAd(new AdRequest.Builder().build());
//
//        mProgressBar = (ProgressBar)findViewById(R.id.progressBar);
//        (mLinearLayoutShare = (LinearLayout) findViewById(R.id.linearLayout_share)).setOnClickListener(this);
//        (mLinearLayoutBookMark = (LinearLayout) findViewById(R.id.linearLayout_bookmark)).setOnClickListener(this);
//        (mLinearLayoutLocation = (LinearLayout) findViewById(R.id.linearLayout_location)).setOnClickListener(this);
//        mImageViewBookMark = (ImageView)findViewById(R.id.imageView_bookmark);
//
//        mContetContentViewPager = (ContentViewPager)findViewById(R.id.viewpager);
//        mLinearLayoutNavibar = (LinearLayout) findViewById(R.id.linearLayout_navibar);
//
//        mLinearLayoutMap = (LinearLayout) findViewById(R.id.linearLayout_map);
//        mTextView1 = (TextView)findViewById(R.id.textView1);
//        mTextView2 = (TextView)findViewById(R.id.textView2);
//        mTextView3 = (TextView)findViewById(R.id.textView3);
//        mTextView4 = (TextView)findViewById(R.id.textView4);
//        mTextView5 = (TextView)findViewById(R.id.textView5);
//        mTextView6 = (TextView)findViewById(R.id.textView6);
//        mTextView7 = (TextView)findViewById(R.id.textView7);
//        mTextView8 = (TextView)findViewById(R.id.textView8);
//
//
//
//        mBookMarkDB.open();
//        mCursor = mBookMarkDB.BookMarkConfirm(mSavedata.getDd_uid());
//        if(mCursor.getCount() > 0){
//            mImageViewBookMark.setImageResource(R.drawable.icon_book_on);
//        }else{
//            mImageViewBookMark.setImageResource(R.drawable.icon_book_off);
//        }
//        mCursor.close();
//        mBookMarkDB.close();
//
//        mListView = (ScrollListView) findViewById(R.id.listView);
//        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent NextIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mData.get(position).getLink()));
//                startActivity(NextIntent);
//            }
//        });
//
//        mScrollView = (ScrollView)findViewById(R.id.scrollView);
//        mToolbar = (Toolbar) findViewById(R.id.toolbar);
//        if (mToolbar != null) {
//            if(!DsObjectUtils.getInstance(getApplicationContext()).isEmpty(mSavedata.getName())) {
//                mToolbar.setTitle(mSavedata.getName());
//            }else{
//                mToolbar.setTitle(getResources().getString(R.string.app_name));
//            }
//            setSupportActionBar(mToolbar);
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        }
//        if(!DsObjectUtils.getInstance(getApplicationContext()).isEmpty(mSavedata.getDd_uid())){
//            mParam = "?dd_uid_fk="+mSavedata.getDd_uid();
//        }
//
//        subData();
//        setDataView();
//
//        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext()) == ConnectionResult.SUCCESS) {
//            GoogleMapOptions options = new GoogleMapOptions();
//            mMapFragment = (WorkaroundMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
//            mMapFragment.getMapAsync(this);
//            mMapFragment.setListener(new WorkaroundMapFragment.OnTouchListener() {
//                @Override
//                public void onTouch() {
//                    // TODO Auto-generated method stub
//                    mScrollView.requestDisallowInterceptTouchEvent(true);
//                }
//
//            });
//        }
//
//        mGpsReveiver = new DsGpsStateReceiver(new DsGpsStateReceiver.GpsStateListener() {
//            @Override
//            public void onChange() {
//
//            }
//        });
//        registerReceiver(mGpsReveiver, DsGpsStateReceiver.GPS_INTENT_FILTER);
//    }
//
//
//    public void agree(){
//        /**
//         * 내 위치동의 관련
//         */
//        if (!SharedPreference.getBooleanSharedPreference(getApplicationContext(), Config.PREFERENCE_LOCATION)) {
//            LocationDialogFragment mLocationDialogFragment = new LocationDialogFragment(new LocationDialogFragment.ResultListener() {
//                @Override
//                public void onSuccess() {
//                    SharedPreference.putSharedPreference(getApplicationContext(), Config.PREFERENCE_LOCATION, true);
//                    gps();
//                }
//                @Override
//                public void onCancle() {
//                    Toast.makeText(getApplicationContext(), "동의 하셔야 길찾기 이용 가능합니다.",Toast.LENGTH_SHORT).show();
//                }
//            });
//            mLocationDialogFragment.setView(getApplicationContext(), getResources().getString(R.string.location_agree));
//            mLocationDialogFragment.show(getSupportFragmentManager(), "dialog");
//        }else{
//            gps();
//        }
//    }
//
//    public void gps(){
//        if(!DsMapUtils.isGps(getApplicationContext()) ){//gps 상태
//            LocationDialogFragment mLocationDialogFragment = new LocationDialogFragment(new LocationDialogFragment.ResultListener() {
//                @Override
//                public void onSuccess() {
//                    Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                    intent.addCategory(Intent.CATEGORY_DEFAULT);
//                    startActivityForResult(intent, 0);
//                }
//                @Override
//                public void onCancle() {
//                    Toast.makeText(getApplicationContext(), "GPS 켜져야 길찾기 사용이 가능 합니다.",Toast.LENGTH_SHORT).show();
//                }
//            });
//            mLocationDialogFragment.setView(getApplicationContext(), getApplicationContext().getResources().getString(R.string.location_gps));
//            mLocationDialogFragment.show(getSupportFragmentManager(), "dialog ");
//        }else{
//            init();
//        }
//    }
//    public void init(){
//        if(DsMapUtils.isGps(getApplicationContext())){
//            mProgressBar.setVisibility(View.VISIBLE);
//            mFusedLocationService.setOnLocationChangedListener(new LocationSource.OnLocationChangedListener() {
//                private boolean firstCall = true;
//                @Override
//                public void onLocationChanged(final Location location) {
//                    mProgressBar.setVisibility(View.GONE);
//                    if (firstCall) {
//                        DsDebugUtils.Message(location.getLatitude() + "," + location.getLongitude() + "");
//                        mMyLat = location.getLatitude();
//                        mMyLon = location.getLongitude();
//                        firstCall = false;
//
//                        MarkerInfoAlertDialogFragment mMarkerInfoAlertDialogFragment = new MarkerInfoAlertDialogFragment();
//                        mMarkerInfoAlertDialogFragment.setCallback(new MarkerInfoAlertDialogFragment.ResultListener() {
//                            @Override
//                            public void onResult(int which_position) throws ActivityNotFoundException {
//                                if(which_position == 0) {
//                                    setCopyAddress();
//
//                                }else if(which_position == 1){
//                                    String sp = mMyLat+","+mMyLon;
//                                    String ep = mSavedata.getLat()+","+mSavedata.getLon();
//
//                                    try{
//                                        Intent NextIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("daummaps://route?sp="+sp+"&ep="+ep+"&by=CAR"));
//                                        startActivity(NextIntent);
//                                    }catch(ActivityNotFoundException e){
//                                        if(Config.TYPE == Config.MARKET){
//                                            Intent NextIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=net.daum.android.map"));
//                                            startActivity(NextIntent);
//                                        }
//                                    }catch(Exception e){
//
//                                    }
//                                }else if(which_position == 2){
//                                    try {
//                                        setCopyAddress();
//                                        Intent intent = getApplicationContext().getPackageManager().getLaunchIntentForPackage("com.skp.lbs.ptransit");
//                                        startActivity(intent);
//                                    } catch (Exception c) {
//                                        // TODO: handle exception
//                                        Intent NextIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.skp.lbs.ptransit"));
//                                        startActivity(NextIntent);
//                                    }
//                                }else if(which_position == 3){
//                                    try {
//                                        setCopyAddress();
//                                        Intent intent = getApplicationContext().getPackageManager().getLaunchIntentForPackage("kr.mappers.AtlanSmart");
//                                        startActivity(intent);
//                                    } catch (Exception c) {
//                                        // TODO: handle exception
//                                        Intent NextIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=kr.mappers.AtlanSmart"));
//                                        startActivity(NextIntent);
//                                    }
//                                }else if(which_position == 4){
//                                    try {
//                                        setCopyAddress();
//                                        Intent intent = getApplicationContext().getPackageManager().getLaunchIntentForPackage("com.mnsoft.mappyobn");
//                                        startActivity(intent);
//                                    } catch (Exception c) {
//                                        // TODO: handle exception
//                                        Intent NextIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.mnsoft.mappyobn"));
//                                        startActivity(NextIntent);
//                                    }
//                                }else if(which_position == 5){
//                                    try {
//                                        setCopyAddress();
//                                        Intent intent = getApplicationContext().getPackageManager().getLaunchIntentForPackage("com.mnsoft.lgunavi");
//                                        startActivity(intent);
//                                    } catch (Exception c) {
//                                        // TODO: handle exception
//                                        Intent NextIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.mnsoft.lgunavi"));
//                                        startActivity(NextIntent);
//                                    }
//                                }else if(which_position == 6){
//
//                                    try {
//                                        setCopyAddress();
//                                        Intent intent = getApplicationContext().getPackageManager().getLaunchIntentForPackage("com.thinkware.sundo.inavi3d");
//                                        startActivity(intent);
//                                    } catch (Exception c) {
//                                        // TODO: handle exception
//                                        Intent NextIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.thinkware.sundo.inavi3d"));
//                                        startActivity(NextIntent);
//                                    }
//                                }
//                            }
//                        });
//                        mMarkerInfoAlertDialogFragment.setTargetFragment(mMarkerInfoAlertDialogFragment, 0);
//                        mMarkerInfoAlertDialogFragment.show(getSupportFragmentManager(), "dialog");
//                    }
//                }
//            });
//        }
//    }
//    public void setCopyAddress(){
//        ClipboardManager manager;
//        manager = ClipboardManager.getInstance(getApplicationContext());
//        manager.setOnChangeListner(new OnChangeListner() {
//            @Override
//            public void change(String text) {
//                // TODO Auto-generated method stub
//                Toast.makeText(getApplicationContext(), text+" 복사 되었습니다.", Toast.LENGTH_SHORT).show();
//            }
//        });
//        manager.setText("주소", mSavedata.getAddress());
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        // TODO Auto-generated method stub
//        super.onActivityResult(requestCode, resultCode, data);
//        new DsDebugUtils().Message("onResult");
//        init();
//
//
//    }
//
//    public void setDataView(){
//        if(!DsObjectUtils.getInstance(getApplicationContext()).isEmpty(mSavedata.getName())){
//            mTextView1.setText(mSavedata.getName());
//        }else{
//            mTextView1.setVisibility(View.GONE);
//        }
//
//        if(!DsObjectUtils.getInstance(getApplicationContext()).isEmpty(mSavedata.getAddress())){
//            mTextView2.setText("주소 : "+mSavedata.getAddress());
//        }else{
//            mTextView2.setVisibility(View.GONE);
//        }
//        if(!DsObjectUtils.getInstance(getApplicationContext()).isEmpty(mSavedata.getOption_text())){
//            mTextView3.setText(mSavedata.getOption_text());
//
//        }else{
//            mTextView3.setVisibility(View.GONE);
//        }
//        if(!DsObjectUtils.getInstance(getApplicationContext()).isEmpty(mSavedata.getPrice_text())){
//            mTextView4.setText(mSavedata.getPrice_text());
//        }else{
//            mTextView4.setVisibility(View.GONE);
//        }
//
//        if(!DsObjectUtils.getInstance(getApplicationContext()).isEmpty(mSavedata.getHomepage())){
//            mTextView6.setText("홈페이지 바로가기");
//            mTextView6.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent NextIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mSavedata.getHomepage()));
//                    startActivity(NextIntent);
//
//                }
//            });
//        }else{
//            mTextView6.setVisibility(View.GONE);
//        }
//        if(!DsObjectUtils.getInstance(getApplicationContext()).isEmpty(mSavedata.getReservation_url())){
//            mTextView7.setText("예약페이지 바로가기");
//            mTextView7.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent NextIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mSavedata.getReservation_url()));
//                    startActivity(NextIntent);
//
//                }
//            });
//        }else{
//            mTextView7.setVisibility(View.GONE);
//        }
//
//        if(!DsObjectUtils.getInstance(getApplicationContext()).isEmpty(mSavedata.getTell())){
//            mTextView8.setText(mSavedata.getTell());
//        }else{
//            mTextView8.setVisibility(View.GONE);
//        }
//
//
//
//    }
//
//    public void subData(){
//        new SubData().clear().setCallBack(new BaseResultListener() {
//            @Override
//            public <T> void OnComplete() {
//            }
//            @Override
//            public <T> void OnComplete(Object data) {
//
//                if(data != null){
//                    mData = (ArrayList<SubHandler>) data;
//
//                    if(mData.size() > 0) {
//                        mContetContentViewPager.setVisibility(View.VISIBLE);
//                        mContetContentViewPager.setCallback(new ContentViewPager.ResultListener() {
//                            @Override
//                            public <T> void OnComplete(T data, int nums) {
//                                ViewPager mViewPager = (ViewPager) data;
//                                mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//                                    @Override
//                                    public void onPageSelected(int arg0) {
//                                        // TODO Auto-generated method stub
//                                        mViewpagerNavibar.setButton(arg0);
//                                    }
//
//                                    @Override
//                                    public void onPageScrolled(int arg0, float arg1, int arg2) {
//                                        // TODO Auto-generated method stub
//                                    }
//
//                                    @Override
//                                    public void onPageScrollStateChanged(int arg0) {
//                                        // TODO Auto-generated method stub
//                                    }
//                                });
//                                mViewpagerNavibar = new ViewpagerNavibar(getApplicationContext(), nums);
//                                mLinearLayoutNavibar.addView(mViewpagerNavibar);
//
//                            }
//                        });
//                        mContetContentViewPager.setView(mData);
//
//                        Collections.shuffle(mData);
//                        mSubListAdapter = new SubListAdapter(getApplicationContext(), mData);
//                        AlphaInAnimationAdapter mAlphaInAnimationAdapter = new AlphaInAnimationAdapter(mSubListAdapter);
//                        mAlphaInAnimationAdapter.setAbsListView(mListView);
//                        mListView.setAdapter(mAlphaInAnimationAdapter);
//
//                    }else{
//                        mContetContentViewPager.setVisibility(View.GONE);
//                    }
//                    mScrollView.smoothScrollTo(0,0);
//
//                }
//
//            }
//            @Override
//            public void OnMessage(String str) {
//            }
//        }).setUrl(Config.URL+ Config.URL_XML+ Config.URL_SUB_LIST).setParam(mParam).getView();
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case android.R.id.home:
//                finish();
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        outState.putParcelable("data", mSavedata);
//        super.onSaveInstanceState(outState);
//    }
//
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        mGoogleMap = googleMap;
//        mGoogleMap.getUiSettings().setMapToolbarEnabled(false);
//        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
//
//        if(!DsObjectUtils.isEmpty(mSavedata.getLat()) && !DsObjectUtils.isEmpty(mSavedata.getLon()) ){
//            mLat = Double.parseDouble(mSavedata.getLat());
//            mLon = Double.parseDouble(mSavedata.getLon());
//            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLat, mLon), mGoogleZoom));
//            mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(mLat, mLon)).icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon)));
//        }else{
//            mLinearLayoutMap.setVisibility(View.GONE);
//        }
//    }
//
//
//    private List<android.location.Address> getLatLon(String address) throws IOException {
//        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
//        List<android.location.Address> loc = geocoder.getFromLocationName(address,1);
//        if(loc != null){
//            return loc;
//        }else {
//            return null;
//        }
//    }
//
//    private void SendMMS() {
//        if(!DsObjectUtils.isEmpty(mSavedata.getName()) && !DsObjectUtils.isEmpty(mSavedata.getDd_uid())) {
//            try {
//                Intent NextIntent = new Intent(Intent.ACTION_SEND);
//                NextIntent.setType("text/plain");
//                NextIntent.putExtra(Intent.EXTRA_SUBJECT, mSavedata.getName());
//                NextIntent.putExtra(Intent.EXTRA_TEXT, "반갑습니다.^^ 고고 캠핑장 입니다.\n\n주소:\n" + mSavedata.getAddress()+ "\n\n" + "어플다운:\n" + "https://play.google.com/store/apps/details?id=kr.ds.camping");
//                startActivity(Intent.createChooser(NextIntent, mSavedata.getName() + "공유하기"));
//            } catch (Exception e) {
//                // TODO: handle exception
//                Log.i("TEST", e.toString() + "");
//            }
//        }else {
//            Toast.makeText(getApplicationContext(),"계속 문제가 발생시 관리자에게 문의해주시기 바랍니다.",Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()){
//            case R.id.linearLayout_share:
//                SendMMS();
//                break;
//            case R.id.linearLayout_bookmark:
//
//                if(!DsObjectUtils.isEmpty(mSavedata.getDd_uid())) {
//                    mBookMarkDB.open();
//                    mCursor = mBookMarkDB.BookMarkConfirm(mSavedata.getDd_uid());
//                    if (mCursor.getCount() == 0) {
//                        mBookMarkDB.createNote(mSavedata.getDd_uid());
//                        mImageViewBookMark.setImageResource(R.drawable.icon_book_on);
//                        Toast.makeText(getApplicationContext(), R.string.bookmark_save, Toast.LENGTH_SHORT).show();
//                    } else {
//                        mBookMarkDB.deleteNote(mSavedata.getDd_uid());
//                        mImageViewBookMark.setImageResource(R.drawable.icon_book_off);
//                        Toast.makeText(getApplicationContext(), R.string.bookmark_cancel, Toast.LENGTH_SHORT).show();
//                    }
//                    mCursor.close();
//                    mBookMarkDB.close();
//
//                    setResult(RESULT_OK);
//                }else{
//                    Toast.makeText(getApplicationContext(),"계속 문제가 발생시 관리자에게 문의해주시기 바랍니다.",Toast.LENGTH_SHORT).show();
//                }
//
//                break;
//            case R.id.linearLayout_location:
//                agree();
//                break;
//        }
//    }
//    @Override
//    protected void onDestroy() {
//        // TODO Auto-generated method stub
//        super.onDestroy();
//        if(mGpsReveiver != null) {
//            unregisterReceiver(mGpsReveiver);
//        }
//        if(mFusedLocationService != null) {
//            mFusedLocationService.onRemove();
//        }
//    }
//
//}
