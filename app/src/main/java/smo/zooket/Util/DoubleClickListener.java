package smo.zooket.Util;

import com.daimajia.slider.library.SliderTypes.BaseSliderView;

/**
 * Created by Smo on 5/7/2017.
 */
public abstract class DoubleClickListener implements BaseSliderView.OnSliderClickListener {

    private static final long DOUBLE_CLICK_TIME_DELTA = 300;

    long lastClickTime = 0;

    @Override
    public void onSliderClick(BaseSliderView v) {
        long clickTime = System.currentTimeMillis();
        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
            onDoubleClick(v);
        }
//        else {
//            onSingleClick(v);
//        }
        lastClickTime = clickTime;
    }

//    public abstract void onSingleClick(BaseSliderView v);

    public abstract void onDoubleClick(BaseSliderView v);
}