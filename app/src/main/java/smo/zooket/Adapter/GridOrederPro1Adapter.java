package smo.zooket.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import smo.zooket.Models.Order1Details;
import smo.zooket.R;

/**
 * Created by Smo on 17/05/2017.
 */
public class GridOrederPro1Adapter extends BaseAdapter {

    List<Order1Details> orders;
    private LayoutInflater inflater = null;
    private Context mContext;

    public GridOrederPro1Adapter(Context mcontext, List<Order1Details> orders) {
        this.mContext = mcontext;
        this.orders = orders;
        inflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    public void clear() {
        orders.clear();
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

    public void addItems(List<Order1Details> order) {
        this.orders.addAll(order);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.order_pro1_adapter, parent, false);
        }
        final Order1Details item = orders.get(position);

        TextView Name=(TextView)convertView.findViewById(R.id.NameSupermarket);
        TextView Count=(TextView)convertView.findViewById(R.id.CountBuy);
        TextView Total=(TextView)convertView.findViewById(R.id.TotalBuy);
        TextView Profit=(TextView)convertView.findViewById(R.id.ProfitBuy);

        Name.setText(item.getName());
        Count.setText(item.getCount());
        Total.setText(item.getTotal()+" تومان");
        Profit.setText(item.getProfit()+" تومان");

        return convertView;
    }
}
