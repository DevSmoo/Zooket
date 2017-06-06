package smo.zooket.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import smo.zooket.Models.DatabaseOrder;
import smo.zooket.Models.SimpleProduct;
import smo.zooket.ProductPageActivity;
import smo.zooket.R;
import smo.zooket.Util.Toaster;

public class RecyclerProductAdapter extends RecyclerView.Adapter<RecyclerProductAdapter.ViewHolder> {

    List<SimpleProduct> simpleProducts;
    LayoutInflater inflater = null;
    Context mContext;
    int SupermarketID;
    String SupermarketName;

    public RecyclerProductAdapter(Context mcontext, List<SimpleProduct> simpleProducts,int SupermarketID,String SupermarketNam) {
        this.mContext = mcontext;
        this.simpleProducts = simpleProducts;
        this.SupermarketID=SupermarketID;
        this.SupermarketName=SupermarketNam;
        inflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.product_super_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final SimpleProduct item = simpleProducts.get(position);
        holder.name.setText(item.getName());
        holder.mainPrice.setText(item.getMainPrice()+" تومان");
        holder.superPrice.setText(item.getSuperPrice()+" تومان");

        int main=Integer.parseInt(item.getMainPrice());
        int superM=Integer.parseInt(item.getSuperPrice());
        if (superM<main){
            holder.mainPrice.setPaintFlags(holder.mainPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.mainPrice.setText(item.getMainPrice()+" تومان");
        }

        //region buy
        holder.buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.counterTxt.setText("1");

                RealmConfiguration realmConfig = new RealmConfiguration.Builder(mContext).deleteRealmIfMigrationNeeded().build();
                Realm realm = Realm.getInstance(realmConfig);
                DatabaseOrder results = realm.where(DatabaseOrder.class).equalTo("ProductID", item.getID()).findFirst();
                if (results==null) {
                    SaveAsyncTask save = new SaveAsyncTask(item);
                    save.execute();
                }else{
                    realm.beginTransaction();
                    DatabaseOrder result = realm.where(DatabaseOrder.class).equalTo("ProductID", item.getID()).findFirst();
                    holder.counterTxt.setText(result.getCountsOrder());
                    if (result!=null) {
                        result.setCountsOrder(holder.counterTxt.getText().toString());
                    }
                    realm.commitTransaction();
                }
                holder.buy_more_container.setVisibility(View.GONE);
                holder.counter_container.setVisibility(View.VISIBLE);
            }
        });
//endregion

        //region dec
        holder.dec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int a=Integer.parseInt(holder.counterTxt.getText().toString());

                if (a==1){
                    RemoveAsyncTask rem=new RemoveAsyncTask(item);
                    rem.execute();

                    holder.buy_more_container.setVisibility(View.VISIBLE);
                    holder.counter_container.setVisibility(View.GONE);
                }else{
                    a--;
                    holder.counterTxt.setText(String.valueOf(a));

                    RealmConfiguration realmConfig = new RealmConfiguration.Builder(mContext).deleteRealmIfMigrationNeeded().build();
                    Realm realm = Realm.getInstance(realmConfig);
                    realm.beginTransaction();
                    DatabaseOrder results = realm.where(DatabaseOrder.class).equalTo("ProductID", item.getID()).findFirst();
                    if (results!=null) {
                        results.setCountsOrder(holder.counterTxt.getText().toString());
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
                int a=Integer.parseInt(holder.counterTxt.getText().toString());
                a++;
                holder.counterTxt.setText(String.valueOf(a));

                RealmConfiguration realmConfig = new RealmConfiguration.Builder(mContext).deleteRealmIfMigrationNeeded().build();
                Realm realm = Realm.getInstance(realmConfig);
                realm.beginTransaction();
                DatabaseOrder results = realm.where(DatabaseOrder.class).equalTo("ProductID", item.getID()).findFirst();
                if (results!=null) {
                    results.setCountsOrder(String.valueOf(a));
                }
                realm.commitTransaction();
            }
        });
        //endregion

        //region showmore
        holder.showMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent MyIntent = new Intent(mContext, ProductPageActivity.class);
                MyIntent.putExtra("ID", item.getID());
                MyIntent.putExtra("SuperID", SupermarketID);
                MyIntent.putExtra("SuperName", SupermarketName);
                mContext.startActivity(MyIntent);
            }
        });
//endregion


        int height=holder.container.getLayoutParams().height;
        holder.thumbnail.getLayoutParams().height = height;
        holder.thumbnail.getLayoutParams().width = holder.thumbnail.getLayoutParams().width;


        if (item.getMainImg() != null && !item.getMainImg().isEmpty()) {
            if (item.getMainImg().startsWith("http")) {
                Picasso.with(mContext)
                        .load(item.getMainImg())
                        .placeholder(R.drawable.loading)
                        .into(holder.thumbnail);
            } else {
                Picasso.with(mContext)
                        .load(Uri.fromFile(new File(item.getMainImg())))
                        .placeholder(R.drawable.loading)
                        .into(holder.thumbnail);
            }
        } else {
            Picasso.with(mContext).load(R.drawable.noimage).placeholder(R.drawable.loading).into(holder.thumbnail);
        }
    }

    @Override
    public int getItemCount() {
        return simpleProducts.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public Object getItem(int position) {
        return simpleProducts.get(position);
    }

    public void addItems(List<SimpleProduct> simpleProducts) {
        this.simpleProducts.addAll(simpleProducts);
        notifyDataSetChanged();
    }

    public void clear() {
        simpleProducts.clear();
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView mainPrice;
        TextView superPrice;

        LinearLayout buy;
        LinearLayout showMore;
        LinearLayout buy_more_container;

        LinearLayout counter_container;
        LinearLayout dec;
        LinearLayout inc;
        TextView counterTxt;
        ImageView thumbnail;
        LinearLayout container;
        public ViewHolder(View convertView) {

            super(convertView);
            name = (TextView) convertView.findViewById(R.id.name);
            mainPrice = (TextView) convertView.findViewById(R.id.main_price);
            superPrice = (TextView) convertView.findViewById(R.id.super_price);

            buy=(LinearLayout) convertView.findViewById(R.id.buy);
            showMore=(LinearLayout)convertView.findViewById(R.id.showmore);
            buy_more_container=(LinearLayout) convertView.findViewById(R.id.buy_descrip_container);

            counter_container=(LinearLayout)convertView.findViewById(R.id.CounterContainer);
            dec=(LinearLayout) convertView.findViewById(R.id.decContainer);
            inc=(LinearLayout)convertView.findViewById(R.id.incContainer);
            counterTxt=(TextView) convertView.findViewById(R.id.counterTxt);
            thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);

            container=(LinearLayout)convertView.findViewById(R.id.Container);
        }
    }

    private void RemoveImage(String image) throws Exception {
        File filepath = mContext.getFilesDir();
        File file = new File(filepath, image);
        if (file.exists()) {
            file.delete();
        }
    }
    private void SaveImage(String image) throws Exception {
        URL url = new URL(image);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
//        urlConnection.setDoOutput(true);
        urlConnection.connect();
        File filepath = mContext.getFilesDir();
        String filename = image.substring(image.lastIndexOf("/") + 1);
        File file = new File(filepath, filename);
        if (file.createNewFile()) {
            file.createNewFile();
        }
        FileOutputStream fileOutput = new FileOutputStream(file);
        InputStream inputStream = urlConnection.getInputStream();
        byte[] buffer = new byte[1024];
        int bufferLength = 0;
        while ((bufferLength = inputStream.read(buffer)) > 0) {
            fileOutput.write(buffer, 0, bufferLength);
        }
        fileOutput.close();
    }

    class SaveAsyncTask extends AsyncTask<SimpleProduct, Void, Void> {
        SimpleProduct sitem;
        boolean wasSuccessfull = false;

        public SaveAsyncTask(SimpleProduct item) {
            this.sitem=item;
        }
        Realm realm;
        @Override
        protected Void doInBackground(SimpleProduct... params) {
            try {
                RealmConfiguration realmConfig = new RealmConfiguration.Builder(mContext).deleteRealmIfMigrationNeeded().build();
                realm = Realm.getInstance(realmConfig);
                realm.beginTransaction();
                DatabaseOrder databaseOrder=realm.createObject(DatabaseOrder.class);
                databaseOrder.setSupermarketID(SupermarketID);
                databaseOrder.setSupermarketName(SupermarketName);
                databaseOrder.setClientPrice(sitem.getMainPrice());
                databaseOrder.setSuperPrice(sitem.getSuperPrice());
                databaseOrder.setProductID(sitem.getID());
                databaseOrder.setOrder(sitem.getName());
                databaseOrder.setCountsOrder("1");
                if (sitem.getMainImg() != null && sitem.getMainImg() != "") {
                    try {
                        SaveImage(sitem.getMainImg());
                        databaseOrder.setProMainImg(sitem.getMainImg().substring(sitem.getMainImg().lastIndexOf("/")));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                realm.commitTransaction();
                wasSuccessfull = true;
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
                Toaster.toast(mContext, "با موفقیت ثبت شد", Toast.LENGTH_SHORT);
            } else {
                Toaster.toast(mContext, "خطا! : لطفا دوباره تلاش کنید", Toast.LENGTH_SHORT);
            }
        }
    }
    class RemoveAsyncTask extends AsyncTask<Void, Void, Void> {
        boolean wasSuccessfull = false;
        private AlertDialog dialog;
        private View layout;
        SimpleProduct Pro;
        public RemoveAsyncTask(SimpleProduct item) {
            this.Pro=item;
        }
        @Override
        protected Void doInBackground(Void... params) {
            Realm realm = null;
            try {
                RealmConfiguration realmConfig = new RealmConfiguration.Builder(mContext).deleteRealmIfMigrationNeeded().build();
                realm = Realm.getInstance(realmConfig);

                realm.beginTransaction();
                RealmResults<DatabaseOrder> results = realm.where(DatabaseOrder.class).equalTo("ProductID", Pro.getID()).findAll();
                results.clear();
                realm.commitTransaction();
                wasSuccessfull = true;
                RemoveImage(Pro.getMainImg());
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
