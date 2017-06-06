package smo.zooket.Adapter;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

/**
 * Created by Smo on 5/6/2017.
 */
public class SwipeLayout extends SwipeRefreshLayout {

    private OnChildScrollUpListener mScrollListenerNeeded;
    private int mTouchSlop;
    private float mPrevX;
    private boolean mDeclined;

    public SwipeLayout(Context context) {
        super(context);
    }

    public SwipeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public void setOnChildScrollUpListener(OnChildScrollUpListener listener) {
        mScrollListenerNeeded = listener;
    }

    @Override
    public boolean canChildScrollUp() {
        return mScrollListenerNeeded == null ? false : mScrollListenerNeeded.canChildScrollUp();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPrevX = MotionEvent.obtain(event).getX();
                mDeclined = false; // New action
                break;
            case MotionEvent.ACTION_MOVE:
                final float eventX = event.getX();
                float xDiff = Math.abs(eventX - mPrevX);

                if (mDeclined || xDiff > mTouchSlop) {
                    mDeclined = true; // Memorize
                    return false;
                }
        }
        return super.onInterceptTouchEvent(event);
    }

    public static interface OnChildScrollUpListener {
        public boolean canChildScrollUp();
    }
}

