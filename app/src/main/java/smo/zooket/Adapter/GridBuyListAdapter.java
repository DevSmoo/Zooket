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

import smo.zooket.Models.SimpleOrder;
import smo.zooket.Models.SimpleSupermarket;
import smo.zooket.R;

/**
 * Created by Smo on 22/05/2017.
 */
public class GridBuyListAdapter extends BaseAdapter {
    List<SimpleOrder> orders;
    private LayoutInflater inflater = null;
    private Context mContext;

    public GridBuyListAdapter(Context mcontext, List<SimpleOrder> orders) {
        this.mContext = mcontext;
        this.orders = orders;
        inflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
    public void clear() {
        orders.clear();
        notifyDataSetChanged();
    }
    public void addItems(List<SimpleOrder> orders) {
        this.orders.addAll(orders);
        notifyDataSetChanged();
    }

    public void removeItem(SimpleOrder orders) {
        this.orders.remove(orders);
        notifyDataSetChanged();
    }
    public List<SimpleOrder> getOrders(){
        notifyDataSetChanged();
        return orders;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.buy_simple_order_adapter, parent, false);
        }
        SimpleOrder item = orders.get(position);
        ((TextView) convertView.findViewById(R.id.NameSupermarket)).setText(item.getSupermarketName());
        ((TextView) convertView.findViewById(R.id.Date)).setText(item.getDate());
        ((TextView) convertView.findViewById(R.id.Rahgiri)).setText(item.getRahgiriCode());

        if (item.getStatus().matches("Pending")){
            ((TextView) convertView.findViewById(R.id.Status)).setText("ثبت شده");
            ((TextView) convertView.findViewById(R.id.Status)).setTextColor(mContext.getResources().getColor(R.color.pending));
        }else if (item.getStatus().matches("Sended")){
            ((TextView) convertView.findViewById(R.id.Status)).setText("ارسال شده");
            ((TextView) convertView.findViewById(R.id.Status)).setTextColor(mContext.getResources().getColor(R.color.sended));
        }else if (item.getStatus().matches("Delivered")){
            ((TextView) convertView.findViewById(R.id.Status)).setText("تحویل گرفته شده");
            ((TextView) convertView.findViewById(R.id.Status)).setTextColor(mContext.getResources().getColor(R.color.deliverd));
        }

        return convertView;
    }
}
