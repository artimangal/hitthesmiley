package com.sidh.game.hitthesmiley;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.share.ShareApi;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

public class FacebookShare {

	private static final String PERMISSION = "publish_actions";
	private boolean canPresentShareDialog;
	private CallbackManager callbackManager;
	private ShareDialog shareDialog;
	Activity mActivity;
	String title = "", description = "", contentUrl = "";
	String imageUrl = "";

	public FacebookShare(Activity a, String title, String description,
			String imageUrl, String contentUrl) {
		callbackManager = CallbackManager.Factory.create();
		mActivity = a;
		this.title = title;
		this.description = description;
		this.imageUrl = imageUrl;
		this.contentUrl = contentUrl;
	}

	public void shareWithFacebook() {
		Log.d("xyz", "share with facebook");
		shareDialog = new ShareDialog(mActivity);
		shareDialog.registerCallback(callbackManager, shareCallback);

		// Can we present the share dialog for regular links?
		canPresentShareDialog = ShareDialog.canShow(ShareLinkContent.class);
		performPublish(canPresentShareDialog);
	}

	private void performPublish(boolean allowNoToken) {
		Log.d("xyz", "performPublish " + allowNoToken);
		AccessToken accessToken = AccessToken.getCurrentAccessToken();
		Log.d("xyz", "accessToken " + accessToken);
		// if (accessToken != null) {
		// Log.d("xyz","accessToken "+accessToken+" hasPublishPermission() "+hasPublishPermission());
		// if (hasPublishPermission()) {
		// // We can do the action right away.
		// postStatusUpdate();
		// return;
		// } else {
		// // We need to get new permissions, then complete the action when
		// // we get called back.
		//
		// LoginManager.getInstance().logInWithPublishPermissions(
		// mActivity, Arrays.asList(PERMISSION));
		// return;
		// }
		// }

		if (allowNoToken) {
			// pendingAction = action;
			postStatusUpdate();
			// Toast.makeText(getActivity(), "Could not share",
			// Toast.LENGTH_SHORT)
			// .show();
		} else {
			Toast.makeText(mActivity, "Could not share", Toast.LENGTH_SHORT)
					.show();
		}
	}

	private boolean hasPublishPermission() {
		AccessToken accessToken = AccessToken.getCurrentAccessToken();
		return accessToken != null
				&& accessToken.getPermissions().contains(PERMISSION);
	}

	private void postStatusUpdate() {
		Profile profile = Profile.getCurrentProfile();
		Log.d("xyz", "postStatusUpdate " + imageUrl + " canPresentShareDialog "
				+ canPresentShareDialog);
		if (imageUrl != null) {
			ShareLinkContent linkContent = new ShareLinkContent.Builder()
					.setContentTitle(title).setContentDescription(description)
					.setImageUrl(Uri.parse(imageUrl))
					.setContentUrl(Uri.parse(contentUrl)).build();
			if (canPresentShareDialog) {
				shareDialog.show(linkContent);
			} else if (profile != null && hasPublishPermission()) {
				ShareApi.share(linkContent, shareCallback);
			} else {
				// pendingAction = PendingAction.POST_STATUS_UPDATE;
			}
		} else {
			ShareLinkContent linkContent = new ShareLinkContent.Builder()
					.setContentTitle(title).setContentDescription(description)
					.setContentUrl(Uri.parse(contentUrl)).build();
			if (canPresentShareDialog) {
				shareDialog.show(linkContent);
			} else if (profile != null && hasPublishPermission()) {
				ShareApi.share(linkContent, shareCallback);
			} else {
				// pendingAction = PendingAction.POST_STATUS_UPDATE;
			}
		}
	}

	private FacebookCallback<Sharer.Result> shareCallback = new FacebookCallback<Sharer.Result>() {
		public void onCancel() {
			// Log.d("HelloFacebook", "Canceled");
			showResult("Error", "Publish cancelled");
		}

		public void onError(FacebookException error) {
			// Log.d("HelloFacebook", String.format("Error: %s",
			// error.toString()));
			// String title = getString(R.string.error);
			// String alertMessage = error.getMessage();
			showResult("Error", "Publish cancelled");
		}

		public void onSuccess(Sharer.Result result) {
			// Log.d("HelloFacebook", "Success!");
			if (result.getPostId() != null) {
				// String title = getString(R.string.success);
				// String id = result.getPostId();
				// String alertMessage =
				// getString(R.string.successfully_posted_post, id);
				showResult("Success", "Publish Successfully");
			}
		}

		private void showResult(String title, String alertMessage) {
			// new AlertDialog.Builder(getActivity()).setTitle(title)
			// .setMessage(alertMessage).setPositiveButton("OK", null)
			// .show();
			Toast.makeText(mActivity, alertMessage, Toast.LENGTH_SHORT).show();
		}
	};

	public void handleActivityResult(int requestCode, int resultCode,
			Intent data) {
		// super.onActivityResult(requestCode, resultCode, data);
		Log.d("xyz", "handleActivityResult " + resultCode);
		if (resultCode == Activity.RESULT_OK) {

			callbackManager.onActivityResult(requestCode, resultCode, data);

		}
	}
}
