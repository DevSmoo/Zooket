package smo.zooket.Util.MaterialTab;

import android.content.res.Resources;
import android.util.TypedValue;

/**
 * Created by Smo on 5/7/2017.
 */
public class Utils {
    public static int dpToPx(Resources resources, float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }
}
