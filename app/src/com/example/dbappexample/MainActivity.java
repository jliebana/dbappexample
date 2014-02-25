package com.example.dbappexample;

import java.util.ArrayList;
import java.util.LinkedList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.android.AuthActivity;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AppKeyPair;
/**
 *  Main Activity of the app.
 * @author jliebana
 *
 */
public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";

	private Context context;

	// Dropbox API related:
	final static private String APP_KEY = "349qwp042wp9rkd";
	final static private String APP_SECRET = "jjk53e3zervhbam";
	private DropboxAPI<AndroidAuthSession> mDBApi;
	// Names of preferences
	final static private String ACCOUNT_PREFS_NAME = "prefs";
	final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
	final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";

	private TextView mainLabel;
	private final ArrayList<Entry> epubsEntries = new ArrayList<Entry>();

	private ProgressBar progressbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		// We create a new AuthSession so that we can use the Dropbox API.
		AndroidAuthSession session = buildSession();
		mDBApi = new DropboxAPI<AndroidAuthSession>(session);
		// We check that the key is right
		checkAppKeySetup();

		setContentView(R.layout.main_activity);
		mainLabel = (TextView) findViewById(R.id.main_label);
		mainLabel.setText("Connecting to dropbox...");

		if (!mDBApi.getSession().isLinked()) {
			// We check that if there is no session, we need to create a new one
			mDBApi.getSession().startOAuth2Authentication(MainActivity.this);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		AndroidAuthSession session = mDBApi.getSession();

		// We need to check that Dropbox authentication started with
		// .startOAuth2Authentication() completes properly.
		if (session.authenticationSuccessful()) {
			try {
				// Mandatory call to complete the auth
				session.finishAuthentication();
				// Store it locally in our app for later use
				storeAuth(session);

			} catch (IllegalStateException e) {
				Log.i(TAG, "Error authenticating", e);
			}
		}
		else {
			mainLabel.setText("Not authenticated");
		}

		if (mDBApi.getSession().isLinked()) {
			mainLabel.setText("Successfully connected to Dropbox!\nDownloading files...");
			downloadEpubsEntries();
		}

	}

	/**
	 * This handler will draw the epub icons once they are downloaded
	 */
	Handler handler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			GridView gridView = (GridView) findViewById(R.id.gridView1);
			progressbar = (ProgressBar) findViewById(R.id.loading_progress_bar);
			gridView.setAdapter(new GridItemAdapter(context, epubsEntries));
			
			mainLabel.setVisibility(View.GONE);
			progressbar.setVisibility(View.GONE);
			gridView.setVisibility(View.VISIBLE);
		}
	};

	/**
	 * Method in charge of download all the epub metadata entries available in
	 * the user's dropbox folder. The search is done over all the directories.
	 */
	private void downloadEpubsEntries() {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {

				Entry dirent;
				try {
					LinkedList<String> dir = new LinkedList<String>();
					dir.add("/");
					while (dir.size() != 0) {
						String currentDir = new String(dir.pop());
						Log.d(TAG, "Current dir: " + currentDir);
						dirent = mDBApi.metadata(currentDir, 0, null, true, null);
						for (Entry currentEntry : dirent.contents) {
							if (currentEntry.isDir) {
								dir.push(new String(currentEntry.path));
								Log.d(TAG, "DIR: " + currentEntry.path);
							}
							else {
								Log.d(TAG, "DATA: " + currentEntry.path);
								if (currentEntry.path.endsWith("epub")) {
									Log.d(TAG, "EPUB: " + currentEntry.path);
									epubsEntries.add(currentEntry);
								}
							}
						}
					}
					Log.d(TAG, "DONE");
				} catch (DropboxException e) {
					Log.e(TAG, "Error retrieving files");
					e.printStackTrace();
				}
				handler.sendEmptyMessage(0);
			}
		});
		t.start();
	}

	/**
	 * Method which will build a session, trying to retrieve a previous stored
	 * session
	 * 
	 * @return
	 */
	private AndroidAuthSession buildSession() {
		AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);

		AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
		// We try to retrieve previously saved session
		loadAuth(session);
		return session;
	}

	/**
	 * This method checks that the key in this class and the one in the manifest
	 * match. It is not mandatory to use it but recommended.
	 */
	private void checkAppKeySetup() {
		// Check if the app has set up its manifest properly.
		Intent testIntent = new Intent(Intent.ACTION_VIEW);
		String scheme = "db-" + APP_KEY;
		String uri = scheme + "://" + AuthActivity.AUTH_VERSION + "/test";
		testIntent.setData(Uri.parse(uri));
		PackageManager pm = getPackageManager();
		if (0 == pm.queryIntentActivities(testIntent, 0).size()) {
			Log.d(TAG, "Wrong key on manifest");
			finish();
		}
	}

	/**
	 * This method tries to restore a previously saved session to avoid asking
	 * permission to the user. In case it is available, it is added to the
	 * session
	 * 
	 * @param session
	 */
	private void loadAuth(AndroidAuthSession session) {
		SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
		String key = prefs.getString(ACCESS_KEY_NAME, null);
		String secret = prefs.getString(ACCESS_SECRET_NAME, null);
		if (key == null || secret == null || key.length() == 0 || secret.length() == 0)
			return;
		session.setOAuth2AccessToken(secret);
	}

	/**
	 * This methods stores the session in the preferences to be able to use it
	 * any other time
	 * 
	 * @param session
	 */
	private void storeAuth(AndroidAuthSession session) {
		// Store the OAuth 2 access token, if there is one.
		String oauth2AccessToken = session.getOAuth2AccessToken();
		SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
		Editor edit = prefs.edit();
		edit.putString(ACCESS_KEY_NAME, "oauth2:");
		edit.putString(ACCESS_SECRET_NAME, oauth2AccessToken);
		edit.commit();

	}
}
