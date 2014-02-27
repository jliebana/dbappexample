package com.example.dbappexample;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * 
 * @author jliebana
 * 
 */
public class MyClickableImageView extends ImageView {

	private static final String TAG = "MyClickableImageView";
	public static final String EBOOK_PATH = "EBOOK_PATH";

	private final GestureDetector gestureDetector;

	// path required for identifying element when double tapping it
	private String path;

	public MyClickableImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		gestureDetector = new GestureDetector(context, new GestureListener());
	}

	
	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		return gestureDetector.onTouchEvent(e);
	}

	private class GestureListener extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			Log.d(TAG, "Double tap detected");
			Context context = getContext();
			Intent intent = new Intent(context, DisplayImageActivity.class);
			intent.putExtra(EBOOK_PATH, path);
			context.startActivity(intent);
			return true;
		}
	}

}
