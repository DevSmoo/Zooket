package smo.zooket;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import in.srain.cube.views.GridViewWithHeaderAndFooter;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import smo.zooket.API.APIHandler;
import smo.zooket.Adapter.GridOrderPro2Adapter;
import smo.zooket.Adapter.NavigationDrawerAdapter;
import smo.zooket.Adapter.RecyclerOrderPro2Adapter;
import smo.zooket.Adapter.SwipeLayout;
import smo.zooket.Models.DatabaseOrder;
import smo.zooket.Models.DatabasePayWay;
import smo.zooket.Models.DatabaseUser;
import smo.zooket.Models.OrderTemp;
import smo.zooket.Util.JalaliCalendar;
import smo.zooket.Util.PersianCalendar;
import smo.zooket.Util.Toaster;
import smo.zooket.Util.Utils;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Order2Activity extends BaseActivity {
    Activity context;

    Realm realm;
    GridViewWithHeaderAndFooter grid;
    GridOrderPro2Adapter gridAdapter;
    LinearLayout loadingHolder;
    LinearLayout contentHolder;
    SwipeLayout swipeLayout;

    Toolbar toolbar;
    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    DrawerLayout mDrawerLayout;

    boolean doubleBackToExitPressedOnce;

    int ID=0;
String Name="";
    Boolean FactorRes;
    String Adr="";
    //for show by recycler
    RecyclerView recyclerView;
    RecyclerOrderPro2Adapter recyclerOrderPro2Adapter;

    String[] time;
    int Start;
    int End;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order2);

        context=this;

        //toolbar
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ((ImageButton)toolbar.findViewById(R.id.back)).setVisibility(View.VISIBLE);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle("");
        ((ImageButton)toolbar.findViewById(R.id.basketbutton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context,Order1Activity.class));
            }
        });

        //Initialization
        doubleBackToExitPressedOnce = false;

        //Attach DB
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(this).deleteRealmIfMigrationNeeded().build();
        realm = Realm.getInstance(realmConfig);

        //Navigation Drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.DrawerLayout);
        mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView);
        mRecyclerView.setHasFixedSize(true);
        final DatabaseUser realmUser = realm.where(DatabaseUser.class).findFirst();
        mAdapter = new NavigationDrawerAdapter(realmUser);
        setUser(realmUser);
        mRecyclerView.setAdapter(mAdapter);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.requestLayout();

        //Font
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/BYekan.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
        //Handling Navigation Drawer On Click
        final GestureDetector mGestureDetector = new GestureDetector(Order2Activity.this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });
        mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
                View child = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
                if (child != null && mGestureDetector.onTouchEvent(motionEvent)) {
                    int position = recyclerView.getChildPosition(child);
                    if (position != 0) {
                        SelectMenu(position);
                    } else if (position == 0) {
                        if (((TextView) child.findViewById(R.id.Email)).getText().toString().trim().isEmpty()) {
                            //Show Login Dialog
                            ShowLoginDialog();
                        } else {
                            //Go To Profile
                            Intent myIntent = new Intent(Order2Activity.this, ProfileActivity.class);
                            startActivity(myIntent);
                        }
                    }
                    return true;
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean b) {

            }
        });

        //Initialization
        ID = (int) getIntent().getExtras().get("ID");
        Name = (String) getIntent().getExtras().get("Name");


        grid = (GridViewWithHeaderAndFooter) findViewById(R.id.itemgrid);
        loadingHolder = (LinearLayout) findViewById(R.id.loadingHolder);
        contentHolder = (LinearLayout) findViewById(R.id.contentHolder);

        //Initialization
        grid.setSmoothScrollbarEnabled(true);


        //Pull To Refresh
        swipeLayout = (SwipeLayout)findViewById(R.id.pulltorefresh_container);
        swipeLayout.setEnabled(false);

        //Set Loading
        loadingHolder.findViewById(R.id.NoInternet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingHolder.findViewById(R.id.loadingmore).setVisibility(View.VISIBLE);
                loadingHolder.findViewById(R.id.NoInternet).setVisibility(View.GONE);

            }
        });

        //show by recycler
        recyclerView=(RecyclerView)findViewById(R.id.recycleProduct);
        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(manager);

        ((LinearLayout)findViewById(R.id.submit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseUser rUser = realm.where(DatabaseUser.class).findFirst();
                if (rUser!=null){
                    SubmitOrderDialog();
                }else{
                    ShowLoginDialog();
                }
            }
        });

        String ts = Long.toString(System.currentTimeMillis() / 1000L);
        String key = Utils.SHA1(ts);
        APIHandler.getApiInterface().geetJobTime(ts, key, ID, TodayWorkTime(), new Callback<String>() {
            @Override
            public void success(String s, Response response) {
                if (s!=null && s!=""){
                    time= s.split("-");
                    Start= Integer.parseInt(time[0] + "00");
                    End= Integer.parseInt(time[1] + "00");
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });


        new FetchAsyncTask1().execute();


    }

//region getghabli
    private AdapterView.OnItemClickListener ItemSelected = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            ((TextView)view.findViewById(R.id.count)).setText("jjjjjjjjjjjjjjjjjjjjjjjjj");
          //  Toast.makeText(Order2Activity.this,String.valueOf(parent.getChildCount()), Toast.LENGTH_SHORT).show();
            final OrderTemp item = (OrderTemp) grid.getAdapter().getItem(position);

            View convertView= grid.getAdapter().getView(position,view,parent);


            final TextView Count = (TextView) convertView.findViewById(R.id.count);
            final TextView Total = (TextView) convertView.findViewById(R.id.totalPrice);

            LinearLayout edit = (LinearLayout) convertView.findViewById(R.id.EditContainer);

            final LinearLayout counter_container = (LinearLayout) convertView.findViewById(R.id.CounterContainer);
            LinearLayout dec = (LinearLayout) convertView.findViewById(R.id.decContainer);
            LinearLayout inc = (LinearLayout) convertView.findViewById(R.id.incContainer);
            final TextView counterTxt = (TextView) convertView.findViewById(R.id.counterTxt);

            counterTxt.setText("2");

            RealmConfiguration realmConfig = new RealmConfiguration.Builder(context).deleteRealmIfMigrationNeeded().build();
            Realm realm = Realm.getInstance(realmConfig);
            DatabaseOrder results = realm.where(DatabaseOrder.class).equalTo("ProductID", item.getProductID()).findFirst();

            counterTxt.setText(results.getCountsOrder());

            edit.setVisibility(View.GONE);
            counter_container.setVisibility(View.VISIBLE);

            //region dec
            dec.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int a = Integer.parseInt(item.getCountsOrder());

                    if (a == 1) {
                        RemoveAsyncTask rem = new RemoveAsyncTask(item);
                        rem.execute();
                       // TotalPrice = TotalPrice - Integer.parseInt(item.getSuperPrice());
                        //Toaster.toast(mContext, String.valueOf(TotalPrice));
                        //orders.remove(item);
                      //  notifyDataSetChanged();
                        gridAdapter.delete(item);
                    } else {
                        a--;
                        RealmConfiguration realmConfig = new RealmConfiguration.Builder(context).deleteRealmIfMigrationNeeded().build();
                        Realm realm = Realm.getInstance(realmConfig);
                        realm.beginTransaction();
                        DatabaseOrder results = realm.where(DatabaseOrder.class).equalTo("ProductID", item.getProductID()).findFirst();
                        if (results != null) {
                            results.setCountsOrder(String.valueOf(a));
                        }
                        realm.commitTransaction();

                        counterTxt.setText(results.getCountsOrder());
                        Count.setText(results.getCountsOrder());
                        int tot = Integer.parseInt(Total.getText().toString());
                        Total.setText(String.valueOf(tot - Integer.parseInt(item.getSuperPrice())));
                       // TotalPrice = TotalPrice - Integer.parseInt(item.getSuperPrice());
                       // Toaster.toast(mContext, String.valueOf(TotalPrice));
                    }
                }
            });
            //endregion

            //region inc
            inc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int a = Integer.parseInt(item.getCountsOrder());
                    a++;
                    counterTxt.setText("lknbvc");
                    Count.setText("55555");
                    int tot = Integer.parseInt(Total.getText().toString());
                    Total.setText(String.valueOf(tot + Integer.parseInt(item.getSuperPrice())));
                  //  TotalPrice = TotalPrice + Integer.parseInt(item.getSuperPrice());
                  //  Toaster.toast(mContext, String.valueOf(TotalPrice));

                    RealmConfiguration realmConfig = new RealmConfiguration.Builder(context).deleteRealmIfMigrationNeeded().build();
                    Realm realm = Realm.getInstance(realmConfig);
                    realm.beginTransaction();
                    DatabaseOrder results = realm.where(DatabaseOrder.class).equalTo("ProductID", item.getProductID()).findFirst();
                    if (results != null) {
                        results.setCountsOrder(String.valueOf(a));
                    }
                    realm.commitTransaction();
                }
            });
            //endregion

        }
    };

    class FetchAsyncTask extends AsyncTask<Void, Void, Void> {
        boolean wasSuccessfull = false;

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            //Dismiss Dialog Or Show
            if (!wasSuccessfull) {
                contentHolder.setVisibility(View.GONE);
                loadingHolder.setVisibility(View.VISIBLE);
                loadingHolder.findViewById(R.id.NoInternet).setVisibility(View.VISIBLE);
                loadingHolder.findViewById(R.id.loadingmore).setVisibility(View.GONE);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            Realm r = null;
            try {
                RealmConfiguration realmConfig = new RealmConfiguration.Builder(context).deleteRealmIfMigrationNeeded().build();
                r = Realm.getInstance(realmConfig);

                final RealmResults<DatabaseOrder>  realmitems = r.where(DatabaseOrder.class).equalTo("SupermarketID", ID).findAll();

                final List<OrderTemp> items = new ArrayList<OrderTemp>();

                for (DatabaseOrder doo:realmitems){
                    OrderTemp o=new OrderTemp();
                    o.setSuperPrice(doo.getSuperPrice());
                    o.setSupermarketName(doo.getSupermarketName());
                    o.setSupermarketID(doo.getSupermarketID());
                    o.setCountsOrder(doo.getCountsOrder());
                    o.setOrder(doo.getOrder());
                    o.setProductID(doo.getProductID());
                    if (doo.getProMainImg() != null && !doo.getProMainImg().isEmpty()) {
                        o.setProMainImg(getFilesDir().getAbsolutePath() + doo.getProMainImg());
                    } else {
                        o.setProMainImg(null);
                    }
                    items.add(o);
                }


                if (items != null && items.size() != 0) {
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("itemSize=",String.valueOf(items.size()));
                            gridAdapter = new GridOrderPro2Adapter(context, items);
                            grid.setAdapter(gridAdapter);
                            grid.deferNotifyDataSetChanged();
                            loadingHolder.setVisibility(View.GONE);
                            contentHolder.setVisibility(View.VISIBLE);
                           // grid.setOnItemClickListener(ItemSelected);
                            grid.invalidateViews();
                        }
                    });
                } else if (items.size() == 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingHolder.setVisibility(View.GONE);
                            contentHolder.setVisibility(View.GONE);
                            findViewById(R.id.NothingFound).setVisibility(View.VISIBLE);
                        }
                    });
                }
                wasSuccessfull = true;
                r.close();
            } catch (Exception ex) {
                wasSuccessfull = false;
                if (r != null) {
                    r.close();
                }
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }
//endregion

    class FetchAsyncTask1 extends AsyncTask<Void, Void, Void> {
        boolean wasSuccessfull = false;

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            //Dismiss Dialog Or Show
            if (!wasSuccessfull) {
                contentHolder.setVisibility(View.GONE);
                loadingHolder.setVisibility(View.VISIBLE);
                loadingHolder.findViewById(R.id.NoInternet).setVisibility(View.VISIBLE);
                loadingHolder.findViewById(R.id.loadingmore).setVisibility(View.GONE);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            Realm r = null;
            try {
                final RealmConfiguration realmConfig = new RealmConfiguration.Builder(context).deleteRealmIfMigrationNeeded().build();
                r = Realm.getInstance(realmConfig);

                final RealmResults<DatabaseOrder>  realmitems = r.where(DatabaseOrder.class).equalTo("SupermarketID", ID).findAll();

                final List<OrderTemp> items = new ArrayList<OrderTemp>();

                for (DatabaseOrder doo:realmitems){
                    OrderTemp o=new OrderTemp();
                    o.setSuperPrice(doo.getSuperPrice());
                    o.setSupermarketName(doo.getSupermarketName());
                    o.setSupermarketID(doo.getSupermarketID());
                    o.setCountsOrder(doo.getCountsOrder());
                    o.setOrder(doo.getOrder());
                    o.setProductID(doo.getProductID());
                    if (doo.getProMainImg() != null && !doo.getProMainImg().isEmpty()) {
                        o.setProMainImg(getFilesDir().getAbsolutePath() + doo.getProMainImg());
                    } else {
                        o.setProMainImg(null);
                    }
                    items.add(o);
                }


                if (items != null && items.size() != 0) {
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("itemSize=",String.valueOf(items.size()));
                            recyclerOrderPro2Adapter = new RecyclerOrderPro2Adapter(context, items);
                            recyclerView.setAdapter(recyclerOrderPro2Adapter);
                            loadingHolder.setVisibility(View.GONE);
                            contentHolder.setVisibility(View.VISIBLE);
                        }
                    });
                } else if (items.size() == 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingHolder.setVisibility(View.GONE);
                            contentHolder.setVisibility(View.GONE);
                            findViewById(R.id.NothingFound).setVisibility(View.VISIBLE);
                        }
                    });
                }
                wasSuccessfull = true;
                r.close();
            } catch (Exception ex) {
                wasSuccessfull = false;
                if (r != null) {
                    r.close();
                }
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }
    private void RemoveImage(String image) throws Exception {
        File filepath = getFilesDir();
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
                RealmConfiguration realmConfig = new RealmConfiguration.Builder(context).deleteRealmIfMigrationNeeded().build();
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
                Toaster.toast(context, "باموفقیت حذف شد", Toast.LENGTH_SHORT);
            } else {
                Toaster.toast(context, "خطا! : لطفا دوباره تلاش کنید", Toast.LENGTH_SHORT);
            }
        }


    }

    public Boolean IsOpen() {

        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String[] temp = (df.format(date)).toString().split(" ");
        int Now = Integer.parseInt(temp[1].toString().split(":")[0] + temp[1].toString().split(":")[1]);

        if (Start < Now && End > Now) {
            return true;
        } else {
            return false;
        }

    }

    public int TodayWorkTime() {
        //DateRes=getIntent().getExtras().get("Date").toString();
        Date d = PersianCalendar.getMiladi(PersianCalendar.getCurrentShamsidate());
        String day = JalaliCalendar.getDayOfWeek(d);
        if (day == "شنبه") {
            return 0;
        } else if (day == "یکشنبه") {
            return 1;
        } else if (day == "دوشنبه") {
            return 2;
        } else if (day == "سه شنبه") {
            return 3;
        } else if (day == "چهارشنبه") {
            return 4;
        } else if (day == "پنج شنبه") {
            return 5;
        } else if (day == "جمعه") {
            return 6;
        } else {
            return 7;
        }
    }
    public void SubmitOrderDialog(){
        if (IsOpen()){
            if (recyclerOrderPro2Adapter!=null){
                if (recyclerOrderPro2Adapter.getItemCount()>0){
                    final Activity context = this;
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    final AlertDialog dialog = builder.create();
                    Rect displayRectangle = new Rect();
                    Window window = this.getWindow();
                    window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
                    LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    final View layout = inflater.inflate(R.layout.dialog_order, null);
                    layout.setMinimumWidth((int) (displayRectangle.width() * 1f));
                    layout.setMinimumHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                    ViewGroup.LayoutParams params = layout.findViewById(R.id.DetailsHolder).getLayoutParams();
                    params.width = (ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    layout.findViewById(R.id.DetailsHolder).setLayoutParams(params);


                    TextView date = (TextView) layout.findViewById(R.id.Date);
                    TextView SuperName = (TextView) layout.findViewById(R.id.SuperName);
                    TextView ClientName = (TextView) layout.findViewById(R.id.ClientName);
                    final TextView Address = (TextView) layout.findViewById(R.id.Address);

                    date.setText(PersianCalendar.getCurrentShamsidate());
                    SuperName.setText(Name);
                    final DatabaseUser realmUser = realm.where(DatabaseUser.class).findFirst();
                    ClientName.setText(realmUser.getName());
                    Address.setText(realmUser.getAddress());

                    final CheckBox NAddres=(CheckBox)layout.findViewById(R.id.NewAddress);
                    final EditText NewAddress = (EditText) layout.findViewById(R.id.AddressTxt);

                    NAddres.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (NAddres.isChecked()){
                                NewAddress.setEnabled(true);
                            }else{
                                NewAddress.setEnabled(false);
                            }
                        }
                    });


                    DatabasePayWay results = realm.where(DatabasePayWay.class).equalTo("SuperID", ID).findFirst();
                    final CheckBox CashWay=(CheckBox)layout.findViewById(R.id.CashWay);
                    final CheckBox ElecWay=(CheckBox)layout.findViewById(R.id.ElecWay);
                    final CheckBox PosWay=(CheckBox)layout.findViewById(R.id.PosWay);

                    if (!results.getPayCash()) {
                        ((LinearLayout)layout.findViewById(R.id.PosHolder)).setVisibility(View.GONE);
                    }
                    if (!results.getPayElec()) {
                        ((LinearLayout)layout.findViewById(R.id.ElecHolder)).setVisibility(View.GONE);
                    }
                    if (!results.getPayPos()) {
                        ((LinearLayout)layout.findViewById(R.id.CashHolder)).setVisibility(View.GONE);
                    }


                    CashWay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (CashWay.isChecked()){
                                ElecWay.setChecked(false);
                                PosWay.setChecked(false);
                            }
                        }
                    });
                    ElecWay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (ElecWay.isChecked()){
                                CashWay.setChecked(false);
                                PosWay.setChecked(false);
                            }
                        }
                    });
                    PosWay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (PosWay.isChecked()){
                                CashWay.setChecked(false);
                                ElecWay.setChecked(false);
                            }
                        }
                    });

                    final CheckBox Yes=(CheckBox)layout.findViewById(R.id.Yes);
                    final CheckBox No=(CheckBox)layout.findViewById(R.id.No);
                    Yes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (Yes.isChecked()){
                                No.setChecked(false);
                                FactorRes=true;
                            }
                        }
                    });
                    No.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (No.isChecked()){
                                Yes.setChecked(false);
                                FactorRes=false;
                            }
                        }
                    });

                    final String NameOrders=recyclerOrderPro2Adapter.getNameOrders();
                    final String OrdersCount=recyclerOrderPro2Adapter.getOrdersCount();
                    final String OrdersPrice=recyclerOrderPro2Adapter.getOrdersPrice();

                    ((TextView)layout.findViewById(R.id.TotalBuy)).setText(recyclerOrderPro2Adapter.getTotalPrice()+" تومان");

                    final String[] ArrayNameOrders=NameOrders.split("-");
                    String[] ArrayOrdersCount=OrdersCount.split("-");
                    String[] ArrayOrdersPrice=OrdersPrice.split("-");

                    LinearLayout proContainer = (LinearLayout) layout.findViewById(R.id.productsContainer);
                    for (int j=0;j<ArrayNameOrders.length;j++){
                        LinearLayout test1=new LinearLayout(context);
                        LinearLayout.LayoutParams lparams11 = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        test1.setLayoutParams(lparams11);
                        test1.setOrientation(LinearLayout.HORIZONTAL);
                        test1.setGravity(Gravity.RIGHT);
                        proContainer.addView(test1);

                        //region total
                        LinearLayout totalContainer=new LinearLayout(context);
                        LinearLayout.LayoutParams tlparams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT,1.0f);
                        totalContainer.setLayoutParams(tlparams);
                        totalContainer.setGravity(Gravity.RIGHT);
                        test1.addView(totalContainer);
                        TextView total=new TextView(context);
                        LinearLayout.LayoutParams lparamst = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        total.setLayoutParams(lparamst);
                        int totall=Integer.parseInt(ArrayOrdersCount[j])*Integer.parseInt(ArrayOrdersPrice[j]);
                        total.setText(String.valueOf(totall)+" تومان");
                        total.setGravity(View.FOCUS_RIGHT);
                        //t.setPadding(4,4,4,4);
                        //t.setTextColor(getResources().getColor(R.color.Black));
                        total.setTextSize(13f);
                        total.setTypeface(null, Typeface.BOLD);

                        totalContainer.addView(total);
                        //endregion

                        //region price
                        LinearLayout priceContainer=new LinearLayout(context);
                        LinearLayout.LayoutParams plparams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT,1.0f);
                        priceContainer.setLayoutParams(plparams);
                        priceContainer.setGravity(Gravity.RIGHT);
                        test1.addView(priceContainer);
                        TextView price=new TextView(context);
                        LinearLayout.LayoutParams lparamsp = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        price.setLayoutParams(lparamsp);
                        price.setText(ArrayOrdersPrice[j]+" تومان");
                        price.setGravity(View.FOCUS_RIGHT);
                        //t.setPadding(4,4,4,4);
                        //t.setTextColor(getResources().getColor(R.color.Black));
                        price.setTextSize(13f);
                        price.setTypeface(null, Typeface.BOLD);

                        priceContainer.addView(price);
                        //endregion

                        //region count
                        LinearLayout countContainer=new LinearLayout(context);
                        LinearLayout.LayoutParams clparams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT,1.0f);
                        countContainer.setLayoutParams(clparams);
                        countContainer.setGravity(Gravity.RIGHT);
                        test1.addView(countContainer);
                        TextView count=new TextView(context);
                        LinearLayout.LayoutParams lparamsc = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        count.setLayoutParams(lparamsc);
                        count.setText(ArrayOrdersCount[j]+" عدد");
                        count.setGravity(View.FOCUS_RIGHT);
                        //t.setPadding(4,4,4,4);
                        //t.setTextColor(getResources().getColor(R.color.Black));
                        count.setTextSize(13f);
                        count.setTypeface(null, Typeface.BOLD);

                        countContainer.addView(count);
                        //endregion

                        //region name
                        LinearLayout nameContainer=new LinearLayout(context);
                        LinearLayout.LayoutParams nlparams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT,1.0f);
                        nameContainer.setLayoutParams(nlparams);
                        nameContainer.setGravity(Gravity.RIGHT);
                        test1.addView(nameContainer);
                        TextView name=new TextView(context);
                        LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        name.setLayoutParams(lparams);
                        name.setText(ArrayNameOrders[j]);
                        name.setGravity(View.FOCUS_RIGHT);
                        //t.setPadding(4,4,4,4);
                        //t.setTextColor(getResources().getColor(R.color.Black));
                        name.setTextSize(13f);
                        name.setTypeface(null, Typeface.BOLD);

                        nameContainer.addView(name);
                        //endregion
                    }
                    final LinearLayout submit=(LinearLayout)layout.findViewById(R.id.sendholder);
                    submit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            submit.setEnabled(false);
                            if (!NAddres.isChecked()){
                                Adr=Address.getText().toString();
                                if (Address.getText().toString().isEmpty()) {
                                    Toaster.toast(context, "آدرس نباید خالی باشد لطفا آدرس جدید راوارد کنید!", Toast.LENGTH_SHORT);
                                    layout.findViewById(R.id.sendholder).setEnabled(true);
                                }else if (!CashWay.isChecked() && !PosWay.isChecked() && !ElecWay.isChecked()){
                                    Toaster.toast(context,"روش پرداخت انتخاب نشده است!",Toast.LENGTH_SHORT);
                                    layout.findViewById(R.id.sendholder).setEnabled(true);
                                }else if (!Yes.isChecked() && !No.isChecked()){
                                    Toaster.toast(context,"آیا مایل به دریافت فاکتور می باشید!",Toast.LENGTH_SHORT);
                                    layout.findViewById(R.id.sendholder).setEnabled(true);
                                }else {
                                    ((TextView) layout.findViewById(R.id.sendbutton)).setText("لطفا صبر کنید...");
                                    layout.findViewById(R.id.sendImage).setVisibility(View.GONE);
                                    layout.findViewById(R.id.sendprogressbar).setVisibility(View.VISIBLE);
                                    String ts = Long.toString(System.currentTimeMillis() / 1000L);
                                    String key = Utils.SHA1(ts);

                                    Boolean a= CashWay.isChecked();
                                    Boolean b=ElecWay.isChecked();
                                    Boolean c=PosWay.isChecked();
                                    APIHandler.getApiInterface().SaveOrder(ts, key, realmUser.getUsername(),
                                            ID, NameOrders, OrdersCount, "", OrdersPrice,Adr,
                                            a, b, c,
                                            FactorRes, new Callback<Boolean>() {
                                                @Override
                                                public void success(Boolean aBoolean, Response response) {
                                                    if (aBoolean){
                                                        realm.beginTransaction();
                                                        RealmResults<DatabaseOrder> results = realm.where(DatabaseOrder.class).equalTo("SupermarketID", ID).findAll();
                                                        results.clear();
                                                        realm.commitTransaction();
                                                        recyclerOrderPro2Adapter.clear();
                                                        Toaster.toast(context, "سفارش شما با موفقیت ثبت شد.", Toast.LENGTH_SHORT);
                                                        dialog.dismiss();
                                                    }else{
                                                        layout.findViewById(R.id.sendholder).setEnabled(true);
                                                        ((TextView) layout.findViewById(R.id.sendbutton)).setText("ثبت");
                                                        layout.findViewById(R.id.sendImage).setVisibility(View.VISIBLE);
                                                        layout.findViewById(R.id.sendprogressbar).setVisibility(View.GONE);
                                                        Toaster.toast(context,"خطا در برقراری ارتباط!",Toast.LENGTH_SHORT);
                                                    }
                                                }

                                                @Override
                                                public void failure(RetrofitError error) {
                                                    layout.findViewById(R.id.sendholder).setEnabled(true);
                                                    ((TextView) layout.findViewById(R.id.sendbutton)).setText("ثبت");
                                                    layout.findViewById(R.id.sendImage).setVisibility(View.VISIBLE);
                                                    layout.findViewById(R.id.sendprogressbar).setVisibility(View.GONE);
                                                    Toaster.toast(context,"خطا در برقراری ارتباط!",Toast.LENGTH_SHORT);
                                                }
                                            });


                                }
                            }else if(NAddres.isChecked()){
                                if (NewAddress.getText().toString().isEmpty()){
                                    Toaster.toast(context, "آدرس جدید را وارد کنید!", Toast.LENGTH_SHORT);
                                    layout.findViewById(R.id.sendholder).setEnabled(true);
                                }else if (!CashWay.isChecked() && !PosWay.isChecked() && !ElecWay.isChecked()){
                                    Toaster.toast(context,"روش پرداخت انتخاب نشده است!",Toast.LENGTH_SHORT);
                                    layout.findViewById(R.id.sendholder).setEnabled(true);
                                }else if (!Yes.isChecked() && !No.isChecked()){
                                    Toaster.toast(context,"آیا مایل به دریافت فاکتور می باشید!",Toast.LENGTH_SHORT);
                                    layout.findViewById(R.id.sendholder).setEnabled(true);
                                }else {
                                    Adr=NewAddress.getText().toString();
                                    ((TextView)layout.findViewById(R.id.sendbutton)).setText("لطفا صبر کنید...");
                                    layout.findViewById(R.id.sendImage).setVisibility(View.GONE);
                                    layout.findViewById(R.id.sendprogressbar).setVisibility(View.VISIBLE);
                                    String ts = Long.toString(System.currentTimeMillis() / 1000L);
                                    String key = Utils.SHA1(ts);

                                    Boolean a= CashWay.isChecked();
                                    Boolean b=ElecWay.isChecked();
                                    Boolean c=PosWay.isChecked();
                                    APIHandler.getApiInterface().SaveOrder(ts, key, realmUser.getUsername(),
                                            ID, NameOrders, OrdersCount, "", OrdersPrice,Adr,
                                            a, b, c,
                                            FactorRes, new Callback<Boolean>() {
                                                @Override
                                                public void success(Boolean aBoolean, Response response) {
                                                    if (aBoolean){
                                                        realm.beginTransaction();
                                                        RealmResults<DatabaseOrder> results = realm.where(DatabaseOrder.class).equalTo("SupermarketID", ID).findAll();
                                                        results.clear();
                                                        realm.commitTransaction();
                                                        recyclerOrderPro2Adapter.clear();
                                                        Toaster.toast(context, "سفارش شما با موفقیت ثبت شد.", Toast.LENGTH_SHORT);
                                                        dialog.dismiss();
                                                    }else{
                                                        Toaster.toast(context, "خطا در برقراری ارتباط !!!", Toast.LENGTH_SHORT);
                                                        findViewById(R.id.sendholder).setEnabled(true);
                                                        ((TextView) findViewById(R.id.sendbutton)).setText("ثبت نام");
                                                        findViewById(R.id.sendprogressbar).setVisibility(View.GONE);
                                                        findViewById(R.id.sendImage).setVisibility(View.VISIBLE);
                                                    }
                                                }
                                                @Override
                                                public void failure(RetrofitError error) {
                                                    Toast.makeText(context, "خطا در برقراری ارتباط !!!", Toast.LENGTH_SHORT).show();
                                                    findViewById(R.id.sendholder).setEnabled(true);
                                                    ((TextView) findViewById(R.id.sendbutton)).setText("ثبت نام");
                                                    findViewById(R.id.sendprogressbar).setVisibility(View.GONE);
                                                    findViewById(R.id.sendImage).setVisibility(View.VISIBLE);
                                                }
                                            });
                                }
                            }
                        }
                    });

                    dialog.setView(layout);
                    dialog.setCanceledOnTouchOutside(true);
                    dialog.show();

                }else{
                    Toaster.toast(context,"سبد خرید خالی است !",Toast.LENGTH_SHORT);
                }
            }else{
                Toaster.toast(context,"سبد خرید خالی است !",Toast.LENGTH_SHORT);
            }
        }else {
            Toaster.toast(context,"فروشگاه بسته است لطفا در زمان مقررمراجعه فرمائید!",Toast.LENGTH_SHORT);
        }
    }
    public void ToggleDrawer(View view) {
        if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            mDrawerLayout.closeDrawer(Gravity.RIGHT);
        } else {
            mDrawerLayout.openDrawer(Gravity.RIGHT);
        }
    }

    public void Back(View view) {
        onBackPressed();
    }

    public void SelectMenu(int position) {
        super.CallActivity(position);
    }

    @Override
    protected void onResume() {
        //Refresh Drawer
        DatabaseUser realmUser = realm.where(DatabaseUser.class).findFirst();
        mAdapter = new NavigationDrawerAdapter(realmUser);
        setUser(realmUser);
        mRecyclerView.setAdapter(mAdapter);
        mDrawerLayout.invalidate();
        super.onResume();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void UpdateUI() {
        DatabaseUser realmUser = realm.where(DatabaseUser.class).findFirst();
        mAdapter = new NavigationDrawerAdapter(realmUser);
        setUser(realmUser);
        mRecyclerView.setAdapter(mAdapter);
        mDrawerLayout.invalidate();
    }

}
