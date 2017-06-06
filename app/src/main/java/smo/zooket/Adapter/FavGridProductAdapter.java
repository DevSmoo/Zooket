package smo.zooket.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
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

import io.realm.Realm;
import io.realm.RealmConfiguration;
import smo.zooket.Models.FavSimpleProduct;
import smo.zooket.Models.SimpleProduct;
import smo.zooket.ProductPageActivity;
import smo.zooket.R;

/**
 * Created by Smo on 17/05/2017.
 */
public class FavGridProductAdapter extends BaseAdapter {

    List<FavSimpleProduct> simpleProducts;
    private LayoutInflater inflater = null;
    private Context mContext;
    Realm realm;


    public FavGridProductAdapter(Context mcontext, List<FavSimpleProduct> simpleProducts) {
        this.mContext = mcontext;
        this.simpleProducts = simpleProducts;
        inflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(mContext).deleteRealmIfMigrationNeeded().build();
        realm = Realm.getInstance(realmConfig);
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

    public void addItems(List<FavSimpleProduct> simpleProducts) {
        this.simpleProducts.addAll(simpleProducts);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.product_super_item, parent, false);
        }
        final FavSimpleProduct item = simpleProducts.get(position);
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
        final TextView counterTxt=(TextView) convertView.findViewById(R.id.counterTxt);

        buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                counterTxt.setText("1");
                buy_more_container.setVisibility(View.GONE);
                counter_container.setVisibility(View.VISIBLE);
            }
        });
        dec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int a=Integer.parseInt(counterTxt.getText().toString());
                if (a==1){
                    buy_more_container.setVisibility(View.VISIBLE);
                    counter_container.setVisibility(View.GONE);
                }else{
                    a--;
                    counterTxt.setText(String.valueOf(a));
                }
            }
        });
        inc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int a=Integer.parseInt(counterTxt.getText().toString());
                a++;
                counterTxt.setText(String.valueOf(a));
            }
        });
        showMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent MyIntent = new Intent(mContext, ProductPageActivity.class);
                MyIntent.putExtra("ID", item.getID());
                mContext.startActivity(MyIntent);
            }
        });

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
