package com.sidh.game.hitthesmiley;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.facebook.FacebookSdk;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.NativeAd;

public class MainMenu extends Activity {

	private boolean showingMainMenu;
	private GamePanel gamePanel;
	FacebookShare facebookShare;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu);
		FacebookSdk.sdkInitialize(getApplicationContext());
		showingMainMenu = true;
	}

	@Override
	public void onBackPressed() {
		if (!showingMainMenu) {
			showingMainMenu = true;
			// Stop game loop
			gamePanel.surfaceDestroyed(null);
			setContentView(R.layout.activity_main_menu);
			Intent intent = new Intent(MainMenu.this, FANActivity.class);
			intent.putExtra("caller_type", "back_pressed");
			startActivityForResult(intent, 7001);
		} else {
			// Quit
			super.onBackPressed();
		}
	}

	// Start game on click
	public void onClickStartGame(View v) {
		showingMainMenu = false;

		// High score
		HighScore.ctx = this.getBaseContext();
		HighScore.loadHighScore();

		setContentView(R.layout.game_layout);

		LinearLayout game_layout = (LinearLayout) findViewById(R.id.game_view);
		// Start and show game.
		gamePanel = new GamePanel(this);
		game_layout.addView(gamePanel);

		nativeAdContainer = (LinearLayout) findViewById(R.id.nativeAdContainer);
		initNativeAd();

		Button share_btn = (Button) findViewById(R.id.share_btn);
		share_btn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				facebookShare = new FacebookShare(MainMenu.this,
						"Check out my results!!", "", "", "");
				facebookShare.shareWithFacebook();
			}
		});
		share_btn.setVisibility(View.GONE);

	}

	NativeAd nativeAd;
	View adView;
	LinearLayout nativeAdContainer;

	private void initNativeAd() {
		// AdSettings.addTestDevice("c6a4ed5bbfe3add6581c29f19af79b11");
		nativeAdContainer.removeAllViews();
		nativeAdContainer.setVisibility(View.GONE);
		nativeAd = new NativeAd(MainMenu.this,
				"437262813112086_439028979602136");
		nativeAd.setAdListener(new AdListener() {

			public void onError(Ad arg0, AdError arg1) {
				Log.d("xyz","onError");
				// fanAdProgress.setVisibility(View.INVISIBLE);
				callFailureCondition();
			}

			public void onAdLoaded(Ad ad) {
				Log.d("xyz","onAdLoaded");
				// fanAdProgress.setVisibility(View.INVISIBLE);
				// nativeAdContainer.setVisibility(View.VISIBLE);
				if (nativeAd == null || nativeAd != ad) {
					// Race condition, load() called
					// again
					// before last ad
					// was displayed
					return;
				}
				try {
					nativeAdContainer.setVisibility(View.VISIBLE);
					inflateAd(nativeAd, adView, MainMenu.this);

				} catch (Exception e) {

					e.printStackTrace();
					callFailureCondition();
				}

				// Unregister last ad
				// nativeAd.unregisterView();
			}

			public void onAdClicked(Ad arg0) {

			}

			@Override
			public void onLoggingImpression(Ad ad) {
				Log.d("xyz","onLoggingImpression");
			}

		});

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		adView = inflater.inflate(R.layout.small_ad_unit, nativeAdContainer);

		nativeAd.loadAd();
	}

	private void inflateAd(NativeAd nativeAd, View adView, Context context) {
		// Create native UI using the ad metadata.

		try {

			ImageView nativeAdIcon = (ImageView) adView
					.findViewById(R.id.ad_icon);
			TextView nativeAdTitle = (TextView) adView
					.findViewById(R.id.ad_title);
			// TextView nativeAdBody = (TextView) adView
			// .findViewById(R.id.nativeAdBody);
			// ImageView nativeAdImage = (ImageView) adView
			// .findViewById(R.id.nativeAdImage);
			TextView nativeAdSocialContext = (TextView) adView
					.findViewById(R.id.ad_promotional_text);
			// Button nativeAdCallToAction = (Button) adView
			// .findViewById(R.id.ad_button);
			// GradientDrawable gd = new GradientDrawable();
			// gd.setColor(Color.parseColor("#5FC4ED"));
			// gd.setCornerRadius(10);
			// if (Build.VERSION.SDK_INT >= 16) {
			// // nativeAdCallToAction.setBackground(gd);
			// } else {
			// nativeAdCallToAction.setBackgroundDrawable(gd);
			// }
			RatingBar nativeAdStarRating = (RatingBar) adView
					.findViewById(R.id.ad_ratingBar);
			// LinearLayout layout_rating = (LinearLayout) adView
			// .findViewById(R.id.layout_rating);

			// Setting the Text
			nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
			// nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
			// nativeAdCallToAction.setVisibility(View.VISIBLE);
			nativeAdTitle.setText(nativeAd.getAdTitle());
			// nativeAdBody.setText(nativeAd.getAdBody());

			// Downloading and setting the ad icon.
			NativeAd.Image adIcon = nativeAd.getAdIcon();
			NativeAd.downloadAndDisplayImage(adIcon, nativeAdIcon);

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

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == 7001) {
			Log.d("xyz", "showing main menu " + showingMainMenu);
			if (!showingMainMenu && gamePanel != null && gamePanel.game != null) {
				gamePanel.game.ResetGame();
				initNativeAd();
			} else if (facebookShare != null && resultCode==RESULT_OK) {
				facebookShare.handleActivityResult(requestCode, resultCode, data);
			}
		}
	}

}
