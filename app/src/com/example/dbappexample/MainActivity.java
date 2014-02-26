package com.example.dbappexample;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.android.AuthActivity;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AppKeyPair;

/**
 * Main Activity of the app.
 * 
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
	private ArrayList<Entry> epubsEntries = new ArrayList<Entry>();

	private ProgressBar progressbar;
	private ListView listView;

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
			listView = (ListView) findViewById(R.id.list_view);
			progressbar = (ProgressBar) findViewById(R.id.loading_progress_bar);
			listView.setAdapter(new ListAdapter(context, epubsEntries));

			mainLabel.setVisibility(View.GONE);
			progressbar.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
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
				if (epubsEntries.size() != 0) {
					epubsEntries.clear();
				}
				Entry dirent;
				try {
					LinkedList<String> dir = new LinkedList<String>();
					dir.add("/example"); // FIXME
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.sort_by_name:
				sortByName();
				return true;
			case R.id.sort_by_date:
				sortByDate();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void sortByName() {
		Object[] list = epubsEntries.toArray();
		Comparable[] values = new String[list.length];
		for (int i = 0; i < list.length; i++) {
			values[i] = ((Entry) list[i]).fileName();
		}
		quicksort(list, values, 0, values.length - 1);
		for (int i = 0; i < list.length; i++) {
			Log.d(TAG, ((Entry) list[i]).fileName());
		}
		epubsEntries = new ArrayList(Arrays.asList(list));
		listView.setAdapter(new ListAdapter(context, epubsEntries));
	}

	private void sortByDate() {
		Object[] list = epubsEntries.toArray();
		Comparable[] values = new Date[list.length];
		for (int i = 0; i < list.length; i++) {
			values[i] = new Date(((Entry) list[i]).clientMtime);
		}
		quicksort(list, values, 0, values.length - 1);
		for (int i = 0; i < list.length; i++) {
			Log.d(TAG, ((Entry) list[i]).fileName());
		}
		epubsEntries = new ArrayList(Arrays.asList(list));
		listView.setAdapter(new ListAdapter(context, epubsEntries));
	}

	/**
	 * The quicksort method is a recursive way to sort an array. Basically it
	 * selects a pivot (in this case the first element of the array), and two
	 * indices (i and j) which start from both extremes of the array. The method
	 * puts those values that are bigger than the pivot at right, and the
	 * smaller ones at the left. Once the indexes i and j cross, the pivot is
	 * located at the position of j and both sub-arrays (left and right) are
	 * sorted again. 
	 * 
	 * If the array is not sorted, the time is expected to be O(n log n).
	 * In the worst case, the time might be O(n^2)
	 * 
	 * This implementation is generic for any Comparable values
	 */
	public void quicksort(Object list[], Comparable values[], int left, int right) {

		Comparable pivot = values[left];
		int i = left;
		int j = right;
		Comparable aux;
		Object aux2;

		while (i < j) {
			while (values[i].compareTo(pivot) <= 0 && i < j)
				i++;
			while (values[j].compareTo(pivot) > 0)
				j--;
			if (i < j) {
				aux = values[i];
				values[i] = values[j];
				values[j] = aux;

				aux2 = list[i];
				list[i] = list[j];
				list[j] = aux2;
			}
		}
		// Now we need to relocate the pivot in the position of j
		aux = values[left];
		values[left] = values[j];
		values[j] = values[left];

		aux2 = list[left];
		list[left] = list[j];
		list[j] = aux2;

		if (left < j - 1)
			quicksort(list, values, left, j - 1);
		if (j + 1 < right)
			quicksort(list, values, j + 1, right);
	}

}
