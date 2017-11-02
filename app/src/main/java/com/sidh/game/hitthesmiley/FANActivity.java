package com.sidh.game.hitthesmiley;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;

public class FANActivity extends Activity {

    View adView;
    NativeAd nativeAd;
    ProgressBar fanAdProgress;
    RelativeLayout nativeAdContainer;

    SharedPreferences sharedPreferences;
    Editor editor;

    boolean onPauseCalled = false;
    CountDownTimer fanCountDown;
    ImageButton fan_close;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fan_nativead);

        sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        editor = sharedPreferences.edit();
        editor.commit();

        fanAdProgress = (ProgressBar) findViewById(R.id.fan_ad_progress);
        nativeAdContainer = (RelativeLayout) findViewById(R.id.nativeAdContainer);
        nativeAdContainer.setBackgroundColor(Color.WHITE);

        fan_close = (ImageButton) findViewById(R.id.ib_fan_close);
        fan_close.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    setResult(7001);
                    finish();
                    return true;
                }

                return v.performClick();
            }
        });
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (onPauseCalled) {
            onPauseCalled = false;
        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        onPauseCalled = true;
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        if (adView == null) {
            initNativeAd();
        }
    }

    private void initNativeAd() {
//		AdSettings.addTestDevice("c6a4ed5bbfe3add6581c29f19af79b11");
        if (getIntent().getStringExtra("caller_type").equalsIgnoreCase("game_over")) {
            nativeAd = new NativeAd(FANActivity.this,
                    "437262813112086_439021869602847");
        } else if (getIntent().getStringExtra("caller_type").equalsIgnoreCase("back_pressed")) {
            nativeAd = new NativeAd(FANActivity.this,
                    "437262813112086_439028882935479");
        }
        nativeAd.setAdListener(new AdListener() {

            public void onError(Ad arg0, AdError arg1) {
                fanAdProgress.setVisibility(View.INVISIBLE);
                callFailureCondition();
            }

            public void onAdLoaded(Ad ad) {
                fanAdProgress.setVisibility(View.INVISIBLE);
                nativeAdContainer.setVisibility(View.VISIBLE);
                if (nativeAd == null || nativeAd != ad) {
                    // Race condition, load() called
                    // again
                    // before last ad
                    // was displayed
                    return;
                }
                try {

                    inflateAd(nativeAd, adView, FANActivity.this);

                } catch (Exception e) {

                    e.printStackTrace();
                    callFailureCondition();
                }
                fanCountDown = new CountDownTimer(30000, 1000) {
                    int count = 0;

                    @Override
                    public void onTick(long millisUntilFinished) {
                        // TODO Auto-generated method stub
                        count++;
                        if (count == 3)
                            fan_close.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onFinish() {
                        // TODO Auto-generated method stub
                        fanAdProgress.setVisibility(View.INVISIBLE);
                        setResult(7001);
                        finish();
                    }
                }.start();
                // Unregister last ad
                // nativeAd.unregisterView();
            }

            public void onAdClicked(Ad arg0) {

            }

            @Override
            public void onLoggingImpression(Ad ad) {

            }

        });

        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        adView = inflater.inflate(R.layout.ad_unit, nativeAdContainer);

        nativeAd.loadAd();
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private void inflateAd(NativeAd nativeAd, View adView, Context context) {
        // Create native UI using the ad metadata.

        try {

            ImageView nativeAdIcon = (ImageView) adView
                    .findViewById(R.id.nativeAdIcon);
            TextView nativeAdTitle = (TextView) adView
                    .findViewById(R.id.nativeAdTitle);
            TextView nativeAdBody = (TextView) adView
                    .findViewById(R.id.nativeAdBody);
            MediaView nativeAdImage = (MediaView) adView
                    .findViewById(R.id.nativeAdImage);
            TextView nativeAdSocialContext = (TextView) adView
                    .findViewById(R.id.nativeAdSocialContext);
            Button nativeAdCallToAction = (Button) adView
                    .findViewById(R.id.nativeAdCallToAction);
            GradientDrawable gd = new GradientDrawable();
            gd.setColor(Color.parseColor("#5FC4ED"));
            gd.setCornerRadius(10);
            if (Build.VERSION.SDK_INT >= 16) {
                nativeAdCallToAction.setBackground(gd);
            } else {
                nativeAdCallToAction.setBackgroundDrawable(gd);
            }
            RatingBar nativeAdStarRating = (RatingBar) adView
                    .findViewById(R.id.nativeAdStarRating);
//			LinearLayout layout_rating = (LinearLayout) adView
//					.findViewById(R.id.layout_rating);

            // Setting the Text
            nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
            nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
            nativeAdCallToAction.setVisibility(View.VISIBLE);
            nativeAdTitle.setText(nativeAd.getAdTitle());
            nativeAdBody.setText(nativeAd.getAdBody());

            // Downloading and setting the ad icon.
            NativeAd.Image adIcon = nativeAd.getAdIcon();
            NativeAd.downloadAndDisplayImage(adIcon, nativeAdIcon);

            // Downloading and setting the cover image.
            NativeAd.Image adCoverImage = nativeAd.getAdCoverImage();

            nativeAdImage.setNativeAd(nativeAd);
            //NativeAd.downloadAndDisplayImage(adCoverImage, nativeAdImage);

            NativeAd.Rating rating = nativeAd.getAdStarRating();
            if (rating != null) {
                nativeAdStarRating.setVisibility(View.VISIBLE);
                nativeAdStarRating.setNumStars((int) rating.getScale());
                nativeAdStarRating.setRating((float) rating.getValue());
            } else {
                nativeAdStarRating.setVisibility(View.INVISIBLE);
            }

            // Wire up the View with the native ad, the whole nativeAdContainer
            // will
            // be clickable
            nativeAd.registerViewForInteraction(adView);

            // Or you can replace the above call with the following function to
            // specify the clickable areas.
            // nativeAd.registerViewForInteraction(nativeAdContainer,
            // Arrays.asList(nativeAdCallToAction, nativeAdImage));
        } catch (Exception e) {

            e.printStackTrace();
            callFailureCondition();
        }

    }

    private void callFailureCondition() {
        setResult(7001);
        finish();
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (fanCountDown != null) {
            fanCountDown.cancel();
        }
    }
}
