package smo.zooket.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import smo.zooket.Models.GpsSupermarket;
import smo.zooket.Models.SimpleSupermarket;
import smo.zooket.R;

/**
 * Created by Smo on 5/6/2017.
 */
public class GridItemAdapter extends BaseAdapter {

    List<GpsSupermarket> simpleSupermarkets;
    private LayoutInflater inflater = null;
    private Context mContext;

    public GridItemAdapter(Context mcontext, List<GpsSupermarket> simpleSupermarkets) {
        this.mContext = mcontext;
        this.simpleSupermarkets = simpleSupermarkets;
        inflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void clear() {
        simpleSupermarkets.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return simpleSupermarkets.size();
    }

    @Override
    public Object getItem(int position) {
        return simpleSupermarkets.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addItems(List<GpsSupermarket> simpleSupermarkets) {
        this.simpleSupermarkets.addAll(simpleSupermarkets);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.super_grid_item, parent, false);
        }
        GpsSupermarket item = simpleSupermarkets.get(position);
                ((TextView) convertView.findViewById(R.id.title)).setText(item.getName());

        if (item.getDistance()!=null && item.getDistance()!=""){
            ((TextView) convertView.findViewById(R.id.province)).setText(" متر فاصله از شما"+item.getDistance());
        }else{
            ((TextView) convertView.findViewById(R.id.province)).setText(item.getProvince());
        }

        ImageView thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);
      // thumbnail.getLayoutParams().height = thumbnail.getLayoutParams().width;

        if (item.getThumbnail() != null && !item.getThumbnail().isEmpty()) {
            if (item.getThumbnail().startsWith("http")) {
                Picasso.with(mContext)
                        .load(item.getThumbnail())
                        .placeholder(R.drawable.loading)
                        .into(thumbnail);
            } else {
                Picasso.with(mContext)
                        .load(Uri.fromFile(new File(item.getThumbnail())))
                        .placeholder(R.drawable.loading)
                        .into(thumbnail);
            }
        } else {
            Picasso.with(mContext).load(R.drawable.noimage).placeholder(R.drawable.loading).into(thumbnail);
        }
        return convertView;
    }

}
