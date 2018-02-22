package kr.ds.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.NativeAppInstallAd;
import com.google.android.gms.ads.formats.NativeAppInstallAdView;
import com.google.android.gms.ads.formats.NativeContentAd;
import com.google.android.gms.ads.formats.NativeContentAdView;

import java.util.List;

import kr.ds.karaokesong.R;


/**
 * Created by Administrator on 2018-02-19.
 */

public class AdAdmobNativeAdvancedView extends View {

    private Context mContext;
    private View mAdmobView;
    private NativeExpressAdView mNativeExpressAdView;
    private LinearLayout mContainer;
    public static int TOP = 0;
    public static int CONTET = 1;

    public interface ResultListener {
        public <T> void OnLoad();
        public <T> void OnFail();
    }
    private ResultListener mResultListener;
    public AdAdmobNativeAdvancedView setCallBack(ResultListener resultListener){
        mResultListener = resultListener;
        return this;
    }
    public AdAdmobNativeAdvancedView(Context context) {
        super(context);
        mContext = context;
    }
    public AdAdmobNativeAdvancedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }
    public AdAdmobNativeAdvancedView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }
    public AdAdmobNativeAdvancedView setContainer(View v){
        mContainer = (LinearLayout) v;
        return this;
    }
    public AdAdmobNativeAdvancedView setLayout(final int resource){

        AdLoader.Builder builder = new AdLoader.Builder(mContext, "ca-app-pub-3957553723261794/3266503779");
        AdLoader adLoader = builder.forAppInstallAd(new NativeAppInstallAd.OnAppInstallAdLoadedListener() {
            @Override
            public void onAppInstallAdLoaded(NativeAppInstallAd ad) {
                View v;
                if(resource == TOP){
                    v = LayoutInflater.from(mContext).inflate(R.layout.admob_app_install_top, null, false);
                }else{
                    v = LayoutInflater.from(mContext).inflate(R.layout.admob_app_install, null, false);
                }

               NativeAppInstallAdView nativeAppInstallAdView = (NativeAppInstallAdView)v.findViewById(R.id.native_app_install_adview);
               populateAppInstallAdView(ad, nativeAppInstallAdView);
               mContainer.addView(nativeAppInstallAdView);
                Log.i("TEST", "onAppInstallAdLoaded");
                if(mResultListener != null){
                    mResultListener.OnLoad();
                }

            }
        }).forContentAd(new NativeContentAd.OnContentAdLoadedListener() {
            @Override
            public void onContentAdLoaded(NativeContentAd ad) {
                View v;
                if(resource == TOP){
                    v = LayoutInflater.from(mContext).inflate(R.layout.admob_content_top, null, false);
                }else{
                    v = LayoutInflater.from(mContext).inflate(R.layout.admob_content, null, false);
                }
                NativeContentAdView nativeContentAdView = (NativeContentAdView)v.findViewById(R.id.native_content_adview);
                populateContentAdView(ad, nativeContentAdView);
                mContainer.addView(nativeContentAdView);
                Log.i("TEST", "onContentAdLoaded");
                if(mResultListener != null){
                    mResultListener.OnLoad();
                }

            }
        }).withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
                // A native ad failed to load. Call this method again to load
                // the next ad in the items list.
                Log.e("MainActivity", "The previous native ad failed to load. Attempting to"
                        + " load another.");
                //loadNativeAd(adLoadCount + 1);
                if(mResultListener != null){
                    mResultListener.OnFail();
                }

            }
        }).build();

        // Load the Native Express ad.
        adLoader.loadAd(new AdRequest.Builder().build());
        return this;
    }

    private void populateAppInstallAdView(NativeAppInstallAd nativeAppInstallAd,
                                          NativeAppInstallAdView adView) {
        // Some assets are guaranteed to be in every NativeAppInstallAd.

        MediaView mediaView = (MediaView) adView.findViewById(R.id.appinstall_media);
        adView.setMediaView(mediaView);
        adView.setHeadlineView(adView.findViewById(R.id.appinstall_headline));
        adView.setBodyView(adView.findViewById(R.id.appinstall_body));
        adView.setCallToActionView(adView.findViewById(R.id.appinstall_call_to_action));
        adView.setIconView(adView.findViewById(R.id.appinstall_app_icon));
        adView.setPriceView(adView.findViewById(R.id.appinstall_price));
        adView.setStarRatingView(adView.findViewById(R.id.appinstall_stars));
        adView.setStoreView(adView.findViewById(R.id.appinstall_store));



        ((ImageView) adView.getIconView()).setImageDrawable(nativeAppInstallAd.getIcon()
                .getDrawable());
        ((TextView) adView.getHeadlineView()).setText(nativeAppInstallAd.getHeadline());
        ((TextView) adView.getBodyView()).setText(nativeAppInstallAd.getBody());
        ((Button) adView.getCallToActionView()).setText(nativeAppInstallAd.getCallToAction());

        // These assets aren't guaranteed to be in every NativeAppInstallAd, so it's important to
        // check before trying to display them.
        if (nativeAppInstallAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAppInstallAd.getPrice());
        }

        if (nativeAppInstallAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAppInstallAd.getStore());
        }

        if (nativeAppInstallAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAppInstallAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        // Assign native ad object to the native view.
        adView.setNativeAd(nativeAppInstallAd);

    }



    /**
     * Populates a {@link NativeContentAdView} object with data from a given
     * {@link NativeContentAd}.
     *
     * @param nativeContentAd the object containing the ad's assets
     * @param adView          the view to be populated
     */
    private void populateContentAdView(NativeContentAd nativeContentAd,
                                       NativeContentAdView adView) {
        // Some assets are guaranteed to be in every NativeContentAd.

        adView.setHeadlineView(adView.findViewById(R.id.contentad_headline));
        adView.setImageView(adView.findViewById(R.id.contentad_image));
        adView.setBodyView(adView.findViewById(R.id.contentad_body));
        adView.setCallToActionView(adView.findViewById(R.id.contentad_call_to_action));
        adView.setLogoView(adView.findViewById(R.id.contentad_logo));
        adView.setAdvertiserView(adView.findViewById(R.id.contentad_advertiser));

        ((TextView) adView.getHeadlineView()).setText(nativeContentAd.getHeadline());
        ((TextView) adView.getBodyView()).setText(nativeContentAd.getBody());
        ((TextView) adView.getCallToActionView()).setText(nativeContentAd.getCallToAction());
        ((TextView) adView.getAdvertiserView()).setText(nativeContentAd.getAdvertiser());

        List<NativeAd.Image> images = nativeContentAd.getImages();

        if (images.size() > 0) {
            ((ImageView) adView.getImageView()).setImageDrawable(images.get(0).getDrawable());
        }

        // Some aren't guaranteed, however, and should be checked.
        NativeAd.Image logoImage = nativeContentAd.getLogo();

        if (logoImage == null) {
            adView.getLogoView().setVisibility(View.INVISIBLE);
        } else {
            ((ImageView) adView.getLogoView()).setImageDrawable(logoImage.getDrawable());
            adView.getLogoView().setVisibility(View.VISIBLE);
        }

        // Assign native ad object to the native view.
        adView.setNativeAd(nativeContentAd);
    }


}
