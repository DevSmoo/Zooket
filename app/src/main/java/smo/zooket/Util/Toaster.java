package smo.zooket.Util;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import smo.zooket.R;

/**
 * Created by Smo on 5/7/2017.
 */
public class Toaster {
    public static void toast(Context context, String message, int duration) {
        View layout = LayoutInflater.from(context).inflate(R.layout.toaster_view, null);
        TextView text = (TextView) layout.findViewById(R.id.text_message);
        text.setText(message);
        try{
            Toast toast = new Toast(context);
            toast.setView(layout);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.setDuration(duration);
            toast.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void toast(Context context, int messageResId, int duration) {
        toast(context, context.getString(messageResId), duration);
    }

    public static void toast(Context context, String message) {
        toast(context, message, Toast.LENGTH_SHORT);
    }

    public static void toast(Context context, int messageResId) {
        toast(context, context.getString(messageResId), Toast.LENGTH_SHORT);
    }
}
