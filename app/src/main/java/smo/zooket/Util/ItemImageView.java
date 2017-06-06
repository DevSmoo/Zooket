package smo.zooket.Util;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Smo on 5/6/2017.
 */
public class ItemImageView extends ImageView {
    public ItemImageView(Context context) {
        super(context);
    }

    public ItemImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ItemImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
    }
}
