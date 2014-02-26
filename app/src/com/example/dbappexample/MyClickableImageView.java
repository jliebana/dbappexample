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
	private static final String EBOOK_PATH = "EBOOK_PATH";

	private final GestureDetector gestureDetector;

	// id required for identifying element when double tapping it
	private int id;

	public MyClickableImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		gestureDetector = new GestureDetector(context, new GestureListener());
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
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
			Log.d(TAG, "Double tap detected for icon: " + id);
			Context context = getContext();
			Intent intent = new Intent(context, DisplayImageActivity.class);
			intent.putExtra(EBOOK_PATH, "PATH");// FIXME
			context.startActivity(intent);
			return true;
		}
	}

}
