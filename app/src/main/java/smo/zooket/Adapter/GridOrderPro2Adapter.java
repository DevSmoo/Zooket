package smo.zooket.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import smo.zooket.Models.OrderTemp;
import smo.zooket.R;

/**
 * Created by Smo on 17/05/2017.
 */
public class GridOrderPro2Adapter extends BaseAdapter {
    List<OrderTemp> orders;
    private LayoutInflater inflater = null;
    private Context mContext;
    int TotalPrice = 0;

    public GridOrderPro2Adapter(Context mcontext, List<OrderTemp> orders) {
        this.mContext = mcontext;
        this.orders = orders;
        inflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (OrderTemp o : orders) {
            TotalPrice = TotalPrice + (Integer.parseInt(o.getSuperPrice()) * Integer.parseInt(o.getCountsOrder()));
        }
    }

    public void clear() {
        orders.clear();
    }

    public String getTotalPrice() {
        return String.valueOf(TotalPrice);
    }

    @Override
    public int getCount() {
        return orders.size();
    }

    @Override
    public Object getItem(int position) {
        return orders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addItems(List<OrderTemp> order) {
        this.orders.addAll(order);
        notifyDataSetChanged();
    }

    public void delete(OrderTemp order){
    orders.remove(order);
    notifyDataSetChanged();
}

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.order_pro2_adapter, parent, false);
        }
        final OrderTemp item = orders.get(position);

        TextView Name = (TextView) convertView.findViewById(R.id.name);
        TextView Count = (TextView) convertView.findViewById(R.id.count);
        TextView Total = (TextView) convertView.findViewById(R.id.totalPrice);

        final TextView counterTxt = (TextView) convertView.findViewById(R.id.counterTxt);

        Name.setText(item.getOrder());
        Count.setText(item.getCountsOrder());
        counterTxt.setText(item.getCountsOrder());
        Total.setText(String.valueOf(Integer.parseInt(item.getSuperPrice()) * Integer.parseInt(item.getCountsOrder())));

        LinearLayout dec = (LinearLayout) convertView.findViewById(R.id.decContainer);
        LinearLayout inc = (LinearLayout) convertView.findViewById(R.id.incContainer);

        int height = ((LinearLayout) convertView.findViewById(R.id.Container)).getLayoutParams().height;
        ImageView thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);
        thumbnail.getLayoutParams().height = height;
        thumbnail.getLayoutParams().width = thumbnail.getLayoutParams().width;


        if (item.getProMainImg() != null && !item.getProMainImg().isEmpty()) {
            if (item.getProMainImg().startsWith("http")) {
                Picasso.with(mContext)
                        .load(item.getProMainImg())
                        .placeholder(R.drawable.loading)
                        .into(thumbnail);
            } else {
                Picasso.with(mContext)
                        .load(Uri.fromFile(new File(item.getProMainImg())))
                        .placeholder(R.drawable.loading)
                        .into(thumbnail);
            }
        } else {
            Picasso.with(mContext).load(R.drawable.noimage).placeholder(R.drawable.loading).into(thumbnail);
        }
        notifyDataSetChanged();

        return convertView;
    }
}