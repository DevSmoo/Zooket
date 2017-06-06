package smo.zooket.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import smo.zooket.Models.DatabaseOrder;
import smo.zooket.Models.OrderTemp;
import smo.zooket.R;
import smo.zooket.Util.Toaster;

/**
 * Created by Smo on 21/05/2017.
 */
public class RecyclerOrderPro2Adapter extends RecyclerView.Adapter<RecyclerOrderPro2Adapter.ViewHolder>  {

    List<OrderTemp> orders;
    LayoutInflater inflater = null;
    Context mContext;
    int TotalPrice = 0;

    public RecyclerOrderPro2Adapter(Context mcontext, List<OrderTemp> orders) {
        this.mContext = mcontext;
        this.orders = orders;
        inflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (OrderTemp o : orders) {
            TotalPrice = TotalPrice + (Integer.parseInt(o.getSuperPrice()) * Integer.parseInt(o.getCountsOrder()));
        }
    }

    @Override
    public RecyclerOrderPro2Adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.order_pro2_adapter, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerOrderPro2Adapter.ViewHolder holder, final int position) {
        final OrderTemp item = orders.get(position);
        holder.Name.setText(item.getOrder());
        holder.Count.setText(item.getCountsOrder()+" عدد");
        holder.counterTxt.setText(item.getCountsOrder());
        holder.Total.setText(String.valueOf(Integer.parseInt(item.getSuperPrice()) * Integer.parseInt(item.getCountsOrder()))+" تومان");

        //region dec
        holder.dec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int a = Integer.parseInt(holder.counterTxt.getText().toString());

                if (a == 1) {
                    RemoveAsyncTask rem = new RemoveAsyncTask(item);
                    rem.execute();
                    // TotalPrice = TotalPrice - Integer.parseInt(item.getSuperPrice());
                    //Toaster.toast(mContext, String.valueOf(TotalPrice));
                    //orders.remove(item);
                    //  notifyDataSetChanged();
                    delete(item);
                } else {
                    a--;
                    holder.counterTxt.setText(String.valueOf(a));
                    holder.Count.setText(String.valueOf(a)+" عدد");
                    String[] tott=holder.Total.getText().toString().split(" تومان");
                    int tot = Integer.parseInt(tott[0]);
                    holder.Total.setText(String.valueOf(tot - Integer.parseInt(item.getSuperPrice()))+" تومان");
                    TotalPrice = TotalPrice - Integer.parseInt(item.getSuperPrice());
                    RealmConfiguration realmConfig = new RealmConfiguration.Builder(mContext).deleteRealmIfMigrationNeeded().build();
                    Realm realm = Realm.getInstance(realmConfig);
                    realm.beginTransaction();
                    DatabaseOrder results = realm.where(DatabaseOrder.class).equalTo("ProductID", item.getProductID()).findFirst();
                    if (results != null) {
                        results.setCountsOrder(String.valueOf(a));

                        OrderTemp t=new OrderTemp();
                        t.setProductID(results.getProductID());
                        t.setUserID(results.getUserID());
                        t.setSupermarketID(results.getSupermarketID());
                        t.setSupermarketName(results.getSupermarketName());
                        t.setOrder(results.getOrder());
                        t.setCountsOrder(results.getCountsOrder());
                        t.setClientPrice(results.getClientPrice());
                        t.setSuperPrice(results.getSuperPrice());
                        t.setProMainImg(orders.get(position).getProMainImg());
                        orders.set(position,t);
                    }
                    realm.commitTransaction();

                }
            }
        });
        //endregion

        //region inc
        holder.inc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int a = Integer.parseInt(holder.counterTxt.getText().toString());
                a++;
                holder.counterTxt.setText(String.valueOf(a));
                holder.Count.setText(String.valueOf(a)+" عدد");
                String[] tott=holder.Total.getText().toString().split(" تومان");
                int tot = Integer.parseInt(tott[0]);
                holder.Total.setText(String.valueOf(tot + Integer.parseInt(item.getSuperPrice()))+" تومان");
                  TotalPrice = TotalPrice + Integer.parseInt(item.getSuperPrice());
                //  Toaster.toast(mContext, String.valueOf(TotalPrice));

                RealmConfiguration realmConfig = new RealmConfiguration.Builder(mContext).deleteRealmIfMigrationNeeded().build();
                Realm realm = Realm.getInstance(realmConfig);
                realm.beginTransaction();
                DatabaseOrder results = realm.where(DatabaseOrder.class).equalTo("ProductID", item.getProductID()).findFirst();
                if (results != null) {
                    results.setCountsOrder(String.valueOf(a));

                    OrderTemp t=new OrderTemp();
                    t.setProductID(results.getProductID());
                    t.setUserID(results.getUserID());
                    t.setSupermarketID(results.getSupermarketID());
                    t.setSupermarketName(results.getSupermarketName());
                    t.setOrder(results.getOrder());
                    t.setCountsOrder(results.getCountsOrder());
                    t.setClientPrice(results.getClientPrice());
                    t.setSuperPrice(results.getSuperPrice());
                    t.setProMainImg(orders.get(position).getProMainImg());
                    orders.set(position,t);
                }
                realm.commitTransaction();
            }
        });
        //endregion


        int height=holder.container.getLayoutParams().height;
        holder.thumbnail.getLayoutParams().height = height;
        holder.thumbnail.getLayoutParams().width = holder.thumbnail.getLayoutParams().width;


        if (item.getProMainImg() != null && !item.getProMainImg().isEmpty()) {
            if (item.getProMainImg().startsWith("http")) {
                Picasso.with(mContext)
                        .load(item.getProMainImg())
                        .placeholder(R.drawable.loading)
                        .into(holder.thumbnail);
            } else {
                Picasso.with(mContext)
                        .load(Uri.fromFile(new File(item.getProMainImg())))
                        .placeholder(R.drawable.loading)
                        .into(holder.thumbnail);
            }
        } else {
            Picasso.with(mContext).load(R.drawable.noimage).placeholder(R.drawable.loading).into(holder.thumbnail);
        }
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public String getNameOrders() {
        notifyDataSetChanged();
        String s="";
        if (orders.size()>0){
            s=orders.get(0).getOrder();

            for (int i=1 ;i<orders.size();i++){
                s=s+"-"+orders.get(i).getOrder();
            }
        }
        return s;
    }
    public String getOrdersCount() {
        notifyDataSetChanged();
        String s="";
        if (orders.size()>0){
            s=orders.get(0).getCountsOrder();

            for (int i=1 ;i<orders.size();i++){
                s=s+"-"+orders.get(i).getCountsOrder();
            }
        }
        return s;
    }
    public String getOrdersPrice() {
        notifyDataSetChanged();
        String s="";
        if (orders.size()>0){
            s=orders.get(0).getSuperPrice();

            for (int i=1 ;i<orders.size();i++){
                s=s+"-"+orders.get(i).getSuperPrice();
            }
        }
        return s;
    }
    public String getTotalPrice() {
        return String.valueOf(TotalPrice);
    }

    public void clear() {
        orders.clear();
        notifyDataSetChanged();
    }

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

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView Name;
        TextView Count;
        TextView Total;

        TextView counterTxt;
        LinearLayout dec;
        LinearLayout inc;

        LinearLayout container;
        ImageView thumbnail;
        public ViewHolder(View convertView) {
            super(convertView);
            Name = (TextView) convertView.findViewById(R.id.name);
            Count = (TextView) convertView.findViewById(R.id.count);
            Total = (TextView) convertView.findViewById(R.id.totalPrice);

            counterTxt = (TextView) convertView.findViewById(R.id.counterTxt);
            dec = (LinearLayout) convertView.findViewById(R.id.decContainer);
            inc = (LinearLayout) convertView.findViewById(R.id.incContainer);

            container = (LinearLayout) convertView.findViewById(R.id.Container);
            thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);
        }
    }

    private void RemoveImage(String image) throws Exception {
        File filepath = mContext.getFilesDir();
        File file = new File(filepath, image);
        if (file.exists()) {
            file.delete();
        }
    }

    class RemoveAsyncTask extends AsyncTask<Void, Void, Void> {
        boolean wasSuccessfull = false;
        private AlertDialog dialog;
        private View layout;
        OrderTemp Pro;

        public RemoveAsyncTask(OrderTemp item) {
            this.Pro = item;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Realm realm = null;
            try {
                RealmConfiguration realmConfig = new RealmConfiguration.Builder(mContext).deleteRealmIfMigrationNeeded().build();
                realm = Realm.getInstance(realmConfig);

                realm.beginTransaction();
                RealmResults<DatabaseOrder> results = realm.where(DatabaseOrder.class).equalTo("ProductID", Pro.getProductID()).findAll();
                results.clear();
                realm.commitTransaction();
                wasSuccessfull = true;
                RemoveImage(Pro.getProMainImg());
                realm.close();
            } catch (Exception ex) {
                wasSuccessfull = false;
                if (realm != null) {
                    realm.close();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            //Dismiss Dialog Or Show
            if (wasSuccessfull) {
                Toaster.toast(mContext, "باموفقیت حذف شد", Toast.LENGTH_SHORT);
            } else {
                Toaster.toast(mContext, "خطا! : لطفا دوباره تلاش کنید", Toast.LENGTH_SHORT);
            }
        }


    }
}
