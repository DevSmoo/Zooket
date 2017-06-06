package smo.zooket.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import smo.zooket.Models.Factor;
import smo.zooket.R;

/**
 * Created by Smo on 5/6/2017.
 */
public class FactorItemAdapter extends BaseAdapter {

    List<Factor> factors;
    private LayoutInflater inflater = null;
    private Context mContext;

    public FactorItemAdapter(Context mcontext, List<Factor> factors) {
        this.mContext = mcontext;
        this.factors = factors;
        inflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void clear() {
        factors.clear();
    }

    @Override
    public int getCount() {
        return factors.size();
    }

    @Override
    public Object getItem(int position) {
        return factors.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addItems(List<Factor> factor) {
        this.factors.addAll(factor);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.factor_view, parent, false);
        }
        Factor item=factors.get(position);
        ((TextView) convertView.findViewById(R.id.serveName)).setText(item.getUserName());
        ((TextView) convertView.findViewById(R.id.date)).setText(item.getDateReserve());
        ((TextView) convertView.findViewById(R.id.factorCode)).setText(item.getFactorCode());

        return convertView;
    }
}
