package smo.zooket.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import smo.zooket.Models.DatabaseOrder;
import smo.zooket.Models.DatabaseSimpleProduct;
import smo.zooket.Models.SimpleProduct;
import smo.zooket.Models.SimpleSupermarket;
import smo.zooket.ProductListActivity;
import smo.zooket.ProductPageActivity;
import smo.zooket.R;
import smo.zooket.Util.Toaster;

/**
 * Created by Smo on 12/05/2017.
 */
public class GridProductAdapter extends BaseAdapter {

    List<SimpleProduct> simpleProducts;
    private LayoutInflater inflater = null;
    private Context mContext;
    private int SupermarketID;
    private String SupermarketName;
    TextView counterTxt;

    public GridProductAdapter(Context mcontext, List<SimpleProduct> simpleProducts,int SupermarketID,String SupermarketNam) {
        this.mContext = mcontext;
        this.simpleProducts = simpleProducts;
        this.SupermarketID=SupermarketID;
        this.SupermarketName=SupermarketNam;
        inflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void clear() {
        simpleProducts.clear();
    }

    @Override
    public int getCount() {
        return simpleProducts.size();
    }

    @Override
    public Object getItem(int position) {
        return simpleProducts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addItems(List<SimpleProduct> simpleProducts) {
        this.simpleProducts.addAll(simpleProducts);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.product_super_item, parent, false);
        }
        final SimpleProduct item = simpleProducts.get(position);
        //((TextView) convertView.findViewById(R.id.title)).setText(item.getName());

        TextView name = (TextView) convertView.findViewById(R.id.name);
        final TextView mainPrice = (TextView) convertView.findViewById(R.id.main_price);
        TextView superPrice = (TextView) convertView.findViewById(R.id.super_price);

        LinearLayout buy=(LinearLayout) convertView.findViewById(R.id.buy);
        LinearLayout showMore=(LinearLayout)convertView.findViewById(R.id.showmore);
        final LinearLayout buy_more_container=(LinearLayout) convertView.findViewById(R.id.buy_descrip_container);

        final LinearLayout counter_container=(LinearLayout)convertView.findViewById(R.id.CounterContainer);
        LinearLayout dec=(LinearLayout) convertView.findViewById(R.id.decContainer);
        LinearLayout inc=(LinearLayout)convertView.findViewById(R.id.incContainer);
        counterTxt=(TextView) convertView.findViewById(R.id.counterTxt);
       // buy_more_container.setVisibility(View.VISIBLE);
      //  counter_container.setVisibility(View.GONE);


        name.setText(item.getName());
        mainPrice.setText(item.getMainPrice()+" تومان");
        superPrice.setText(item.getSuperPrice()+" تومان");

        int main=Integer.parseInt(item.getMainPrice());
        int superM=Integer.parseInt(item.getSuperPrice());
        if (superM<main){
            mainPrice.setPaintFlags(mainPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            mainPrice.setText(item.getMainPrice()+" تومان");
        }

        int height=((LinearLayout)convertView.findViewById(R.id.Container)).getLayoutParams().height;
        ImageView thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);
        //thumbnail.getLayoutParams().height = thumbnail.getLayoutParams().width;
        thumbnail.getLayoutParams().height = height;
        thumbnail.getLayoutParams().width = thumbnail.getLayoutParams().width;


        if (item.getMainImg() != null && !item.getMainImg().isEmpty()) {
            if (item.getMainImg().startsWith("http")) {
                Picasso.with(mContext)
                        .load(item.getMainImg())
                        .placeholder(R.drawable.loading)
                        .into(thumbnail);
            } else {
                Picasso.with(mContext)
                        .load(Uri.fromFile(new File(item.getMainImg())))
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
