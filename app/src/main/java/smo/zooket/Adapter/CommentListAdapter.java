package smo.zooket.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import smo.zooket.Models.Comment;
import smo.zooket.R;

/**
 * Created by Smo on 5/6/2017.
 */
public class CommentListAdapter extends BaseAdapter {

    List<Comment> coments;
    private LayoutInflater inflater = null;
    private Context mContext;

    public CommentListAdapter(Context mcontext, List<Comment> coments) {
        this.mContext = mcontext;
        this.coments = coments;
        inflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return coments.size();
    }

    @Override
    public Object getItem(int position) {
        return coments.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addItems(List<Comment> coments) {
        this.coments.addAll(coments);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.comment_item, null);
        }
        Comment cm = coments.get(position);
        ((TextView) convertView.findViewById(R.id.Name)).setText(cm.getName());
        ((TextView) convertView.findViewById(R.id.Body)).setText(cm.getBody());
        ((TextView) convertView.findViewById(R.id.Time)).setText(cm.getDateTime());
        return convertView;
    }
}
