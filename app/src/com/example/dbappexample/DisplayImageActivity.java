package com.example.dbappexample;

import java.io.InputStream;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * 
 * @author jliebana
 * 
 */
public class DisplayImageActivity extends Activity {

	protected static final String TAG = "DisplayImageActivity";
	private ProgressBar progressbar;
	private TextView loadingLabel;

	private Bitmap coverImage = null;

	private String localPath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.display_image_layout);

		localPath = (String) getIntent().getExtras().get(MyClickableImageView.EBOOK_PATH);

		loadingLabel = (TextView) findViewById(R.id.display_image_label);
		loadingLabel.setText("Loading epub image...");
		loadEpubImage();
	}

	/**
	 * This handler will show the epub image once it's loaded
	 */
	Handler handler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			progressbar = (ProgressBar) findViewById(R.id.display_progress_bar);

			loadingLabel.setVisibility(View.GONE);
			progressbar.setVisibility(View.GONE);
			ImageView image = (ImageView) findViewById(R.id.big_image);
			image.setImageBitmap(coverImage);
			image.setVisibility(View.VISIBLE);

		}
	};

	/**
	 * This method will and load the epub image
	 */
	private void loadEpubImage() {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				Log.d(TAG, "Loading image");
				try {
					InputStream epubInputStream = openFileInput(localPath);
					Book book = (new EpubReader()).readEpub(epubInputStream);

					/* Log the book's coverimage property */
					coverImage = BitmapFactory.decodeStream(book.getCoverImage().getInputStream());
				} catch (Exception e) {
					Log.e(TAG, e.getMessage());
					e.printStackTrace();
				}

				handler.sendEmptyMessage(0);
			}
		});
		t.start();
	}

}
