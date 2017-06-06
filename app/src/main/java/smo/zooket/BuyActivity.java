package smo.zooket;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mohamadamin.persianmaterialdatetimepicker.date.DatePickerDialog;

import java.util.List;

import in.srain.cube.views.GridViewWithHeaderAndFooter;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import smo.zooket.API.APIHandler;
import smo.zooket.Adapter.GridBuyListAdapter;
import smo.zooket.Adapter.GridItemAdapter;
import smo.zooket.Adapter.GridOrederPro1Adapter;
import smo.zooket.Adapter.NavigationDrawerAdapter;
import smo.zooket.Adapter.SwipeLayout;
import smo.zooket.Models.Attributes;
import smo.zooket.Models.DatabasePayWay;
import smo.zooket.Models.DatabaseUser;
import smo.zooket.Models.Order;
import smo.zooket.Models.SimpleOrder;
import smo.zooket.Models.SimpleSupermarket;
import smo.zooket.Util.PersianCalendar;
import smo.zooket.Util.Toaster;
import smo.zooket.Util.Utils;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class BuyActivity extends BaseActivity {
    Activity context;

    Realm realm;
    GridViewWithHeaderAndFooter grid;
    GridBuyListAdapter gridAdapter;
    LinearLayout loadingHolder;
    LinearLayout contentHolder;
    SwipeLayout swipeLayout;

    Toolbar toolbar;
    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    DrawerLayout mDrawerLayout;

    boolean doubleBackToExitPressedOnce;

    View LoadingFooter;
    LayoutInflater layoutInflater;
    boolean LoadingStatus = false;
    boolean Load = false;
    int currepage;
    DatabaseUser realmUser;

    String[] ArrayNameOrders;
    String[] ArrayOrdersCount;
    String[] ArrayOrdersPrice;

    int Year,Month,Day;
    int Year1,Month1,Day1;
    String[] today;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy);
        context=this;
        currepage=1;
        Load = true;


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
        realmUser = realm.where(DatabaseUser.class).findFirst();
        setUser(realmUser);
        mAdapter = new NavigationDrawerAdapter(realmUser);
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
        final GestureDetector mGestureDetector = new GestureDetector(BuyActivity.this, new GestureDetector.SimpleOnGestureListener() {
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
                            Intent myIntent = new Intent(BuyActivity.this, ProfileActivity.class);
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


        layoutInflater= (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        grid = (GridViewWithHeaderAndFooter) findViewById(R.id.itemgrid);
        loadingHolder = (LinearLayout) findViewById(R.id.loadingHolder);
        contentHolder = (LinearLayout) findViewById(R.id.contentHolder);
        LoadingFooter = layoutInflater.inflate(R.layout.loading, null);
        grid.addFooterView(LoadingFooter);
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
        LoadingFooter.findViewById(R.id.nointernet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoadingFooter.setVisibility(View.VISIBLE);
                LoadingFooter.findViewById(R.id.loadingmore).setVisibility(View.VISIBLE);
                LoadingFooter.findViewById(R.id.nointernet).setVisibility(View.GONE);
                LoadingFooter.findViewById(R.id.finished).setVisibility(View.GONE);
                GetAllBuy();
            }
        });

        //Set Endless Scrolling
        grid.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int lastInScreen = firstVisibleItem + visibleItemCount;
                if ((lastInScreen == totalItemCount) && !(LoadingStatus)) {
                    if (gridAdapter == null) {
                        loadingHolder.findViewById(R.id.NoInternet).setVisibility(View.GONE);
                        loadingHolder.findViewById(R.id.loadingmore).setVisibility(View.VISIBLE);
                        loadingHolder.setVisibility(View.VISIBLE);
                    }
                    LoadingStatus = true;
                    GetAllBuy();
                }
            }
        });

        //Allow Scrolling
        grid.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

    }

    private AdapterView.OnItemClickListener ItemSelected = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            SimpleOrder item = (SimpleOrder) grid.getAdapter().getItem(position);
            if (item != null) {
                FactorDialog(item.getID());
            }
        }
    };
    public void SearchDialog(View view){
        final Activity context = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.create();
        Rect displayRectangle = new Rect();
        Window window = this.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.dialog_search_factor, null);
        layout.setMinimumWidth((int) (displayRectangle.width() * 0.75f));
        layout.setMinimumHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        ViewGroup.LayoutParams params = layout.findViewById(R.id.DetailsHolder).getLayoutParams();
        params.width = (ViewGroup.LayoutParams.WRAP_CONTENT);
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        layout.findViewById(R.id.DetailsHolder).setLayoutParams(params);


        final EditText SuperName = (EditText) layout.findViewById(R.id.SuperName);
        final EditText FromDate = (EditText) layout.findViewById(R.id.FromDate);
        final EditText ToDate = (EditText) layout.findViewById(R.id.ToDate);

        FromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (FromDate.getText().toString().isEmpty()){
                    today=PersianCalendar.getCurrentShamsidate().toString().split("/");
                    Year=Integer.parseInt(today[0]);
                    if (Integer.parseInt(today[1])==0){
                        Month=12;
                    }else{
                        Month=Integer.parseInt(today[1])-1;
                    }
                    Day=Integer.parseInt(today[2]);
                }else{
                    today=FromDate.getText().toString().trim().split("/");
                    Year=Integer.parseInt(today[0]);
                    if (Integer.parseInt(today[1])==0){
                        Month=12;
                    }else{
                        Month=Integer.parseInt(today[1])-1;
                    }
                    Day=Integer.parseInt(today[2]);
                }
                DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                                FromDate.setText(String.valueOf(year)+"/"+String.valueOf(monthOfYear+1)+"/"+String.valueOf(dayOfMonth));
                            }
                        },Year,Month,Day
                );
                datePickerDialog.show(getFragmentManager(), "Datepickerdialog");
            }
        });

        ToDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ToDate.getText().toString().isEmpty()){
                    today=PersianCalendar.getCurrentShamsidate().toString().split("/");
                    Year1=Integer.parseInt(today[0]);
                    if (Integer.parseInt(today[1])==0){
                        Month1=12;
                    }else{
                        Month1=Integer.parseInt(today[1])-1;
                    }
                    Day1=Integer.parseInt(today[2]);
                }else{
                    today=ToDate.getText().toString().trim().split("/");
                    Year1=Integer.parseInt(today[0]);
                    if (Integer.parseInt(today[1])==0){
                        Month1=12;
                    }else{
                        Month1=Integer.parseInt(today[1])-1;
                    }
                    Day1=Integer.parseInt(today[2]);
                }

                DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                                ToDate.setText(String.valueOf(year)+"/"+String.valueOf(monthOfYear+1)+"/"+String.valueOf(dayOfMonth));
                            }
                        },Year1,Month1,Day1
                );
                datePickerDialog.show(getFragmentManager(), "Datepickerdialog");
            }
        });

        final LinearLayout submit=(LinearLayout)layout.findViewById(R.id.sendholder);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit.setEnabled(false);
                ((TextView)layout.findViewById(R.id.sendbutton)).setText("لطفا صبر کنید...");
                layout.findViewById(R.id.sendImage).setVisibility(View.GONE);
                layout.findViewById(R.id.sendprogressbar).setVisibility(View.VISIBLE);
                String ts = Long.toString(System.currentTimeMillis() / 1000L);
                String key = Utils.SHA1(ts);
                APIHandler.getApiInterface().SearchFactors(ts, realmUser.getUsername(), key, FromDate.getText().toString(), ToDate.getText().toString(), SuperName.getText().toString(), new Callback<List<SimpleOrder>>() {
                    @Override
                    public void success(List<SimpleOrder> orders, Response response) {
                        if (orders != null && orders.size() > 0) {
                            if (gridAdapter == null) {
                                gridAdapter = new GridBuyListAdapter(context, orders);
                                grid.setAdapter(gridAdapter);
                                grid.setOnItemClickListener(ItemSelected);
                            } else {
                                gridAdapter.clear();
                                gridAdapter = new GridBuyListAdapter(context, orders);
                                grid.setAdapter(gridAdapter);
                                grid.setOnItemClickListener(ItemSelected);
                            }
                            grid.invalidateViews();
                            LoadingStatus = false;
                            dialog.dismiss();
                        } else {
                            Toaster.toast(context, "خطا در برقراری ارتباط !!!", Toast.LENGTH_SHORT);
                            findViewById(R.id.sendholder).setEnabled(true);
                            ((TextView) findViewById(R.id.sendbutton)).setText("ثبت نام");
                            findViewById(R.id.sendprogressbar).setVisibility(View.GONE);
                            findViewById(R.id.sendImage).setVisibility(View.VISIBLE);
                        }
                    }
                    @Override
                    public void failure(RetrofitError error) {
                        Toaster.toast(context, "خطا در برقراری ارتباط !!!", Toast.LENGTH_SHORT);
                        findViewById(R.id.sendholder).setEnabled(true);
                        ((TextView) findViewById(R.id.sendbutton)).setText("ثبت نام");
                        findViewById(R.id.sendprogressbar).setVisibility(View.GONE);
                        findViewById(R.id.sendImage).setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        dialog.setView(layout);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }
    public void FactorDialog(int FactorID){

        final Activity context = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.create();
        Rect displayRectangle = new Rect();
        Window window = this.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.dialog_factor, null);
        layout.setMinimumWidth((int) (displayRectangle.width() * 0.75f));
        layout.setMinimumHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        ViewGroup.LayoutParams params = layout.findViewById(R.id.DetailsHolder).getLayoutParams();
        params.width = (ViewGroup.LayoutParams.WRAP_CONTENT);
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        layout.findViewById(R.id.DetailsHolder).setLayoutParams(params);


        final TextView Date = (TextView) layout.findViewById(R.id.Date);
        final TextView SuperName = (TextView) layout.findViewById(R.id.SuperName);
        final TextView ClientName = (TextView) layout.findViewById(R.id.ClientName);
        final TextView Address = (TextView) layout.findViewById(R.id.Address);
        final TextView TotalBuy = (TextView) layout.findViewById(R.id.TotalBuy);
        final TextView PayWay = (TextView) layout.findViewById(R.id.PayWay);
        final TextView RecieveFactor = (TextView) layout.findViewById(R.id.RecieveFactor);
        final TextView Rahgiri = (TextView) layout.findViewById(R.id.Rahgiri);
        final TextView Status = (TextView) layout.findViewById(R.id.Status);


        final DatabaseUser realmUser = realm.where(DatabaseUser.class).findFirst();

        final LinearLayout proContainer = (LinearLayout) layout.findViewById(R.id.productsContainer);

        String ts = Long.toString(System.currentTimeMillis() / 1000L);
        String key = Utils.SHA1(ts);
        APIHandler.getApiInterface().GetFactorDetails(ts, key, FactorID, new Callback<Order>() {
            @Override
            public void success(Order order, Response response) {
                if (order != null) {
                    ArrayNameOrders=order.getOrders().split("-");
                    ArrayOrdersCount=order.getCountsOrders().split("-");
                    ArrayOrdersPrice=order.getSuperPrice().split("-");

                    Date.setText(order.getDate());
                    SuperName.setText(order.getSupermarketName());
                    ClientName.setText(realmUser.getName());
                    Address.setText(order.getNewAddress());
                    int tot=0;
                    for (int j=0;j<ArrayOrdersPrice.length;j++){
                        int t = Integer.parseInt(ArrayOrdersCount[j]) * Integer.parseInt(ArrayOrdersPrice[j]);
                        tot+=t;
                    }
                    TotalBuy.setText(String.valueOf(tot)+" تومان");
                    PayWay.setText(order.getPayWay());
                    RecieveFactor.setText(order.getFactor());
                    Rahgiri.setText(order.getRahgiriCode());

                    if (order.getStatus().matches("Pending")){
                        Status.setText("ثبت شده");
                        Status.setTextColor(getResources().getColor(R.color.pending));
                    }else if (order.getStatus().matches("Sended")){
                        Status.setText("ارسال شده");
                        Status.setTextColor(getResources().getColor(R.color.sended));
                    }else if (order.getStatus().matches("Delivered")){
                        Status.setText("تحویل گرفته شده");
                        Status.setTextColor(getResources().getColor(R.color.deliverd));
                    }


                    for (int j=0;j<ArrayNameOrders.length;j++) {
                        LinearLayout test1 = new LinearLayout(context);
                        LinearLayout.LayoutParams lparams11 = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        test1.setLayoutParams(lparams11);
                        test1.setOrientation(LinearLayout.HORIZONTAL);
                        test1.setGravity(Gravity.RIGHT);
                        proContainer.addView(test1);

                        //region total
                        LinearLayout totalContainer = new LinearLayout(context);
                        LinearLayout.LayoutParams tlparams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                        totalContainer.setLayoutParams(tlparams);
                        totalContainer.setGravity(Gravity.RIGHT);
                        test1.addView(totalContainer);
                        TextView total = new TextView(context);
                        LinearLayout.LayoutParams lparamst = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        total.setLayoutParams(lparamst);
                        int totall = Integer.parseInt(ArrayOrdersCount[j]) * Integer.parseInt(ArrayOrdersPrice[j]);
                        total.setText(String.valueOf(totall) + " تومان");
                        total.setGravity(View.FOCUS_RIGHT);
                        //t.setPadding(4,4,4,4);
                        //t.setTextColor(getResources().getColor(R.color.Black));
                        total.setTextSize(13f);
                        total.setTypeface(null, Typeface.BOLD);

                        totalContainer.addView(total);
                        //endregion

                        //region price
                        LinearLayout priceContainer = new LinearLayout(context);
                        LinearLayout.LayoutParams plparams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                        priceContainer.setLayoutParams(plparams);
                        priceContainer.setGravity(Gravity.RIGHT);
                        test1.addView(priceContainer);
                        TextView price = new TextView(context);
                        LinearLayout.LayoutParams lparamsp = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        price.setLayoutParams(lparamsp);
                        price.setText(ArrayOrdersPrice[j] + " تومان");
                        price.setGravity(View.FOCUS_RIGHT);
                        //t.setPadding(4,4,4,4);
                        //t.setTextColor(getResources().getColor(R.color.Black));
                        price.setTextSize(13f);
                        price.setTypeface(null, Typeface.BOLD);

                        priceContainer.addView(price);
                        //endregion

                        //region count
                        LinearLayout countContainer = new LinearLayout(context);
                        LinearLayout.LayoutParams clparams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                        countContainer.setLayoutParams(clparams);
                        countContainer.setGravity(Gravity.RIGHT);
                        test1.addView(countContainer);
                        TextView count = new TextView(context);
                        LinearLayout.LayoutParams lparamsc = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        count.setLayoutParams(lparamsc);
                        count.setText(ArrayOrdersCount[j] + " عدد");
                        count.setGravity(View.FOCUS_RIGHT);
                        //t.setPadding(4,4,4,4);
                        //t.setTextColor(getResources().getColor(R.color.Black));
                        count.setTextSize(13f);
                        count.setTypeface(null, Typeface.BOLD);

                        countContainer.addView(count);
                        //endregion

                        //region name
                        LinearLayout nameContainer = new LinearLayout(context);
                        LinearLayout.LayoutParams nlparams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                        nameContainer.setLayoutParams(nlparams);
                        nameContainer.setGravity(Gravity.RIGHT);
                        test1.addView(nameContainer);
                        TextView name = new TextView(context);
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
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Toaster.toast(context,"خطا در برقراری ارتباط !");
            }
        });

        final LinearLayout submit=(LinearLayout)layout.findViewById(R.id.sendholder);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        dialog.setView(layout);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    public void GetAllBuy(){
    if (Load) {
        String ts = Long.toString(System.currentTimeMillis() / 1000L);
        String key = Utils.SHA1(ts);

        APIHandler.getApiInterface().GetAllFactor(ts, realmUser.getUsername(), key, currepage,"", new Callback<List<SimpleOrder>>() {
            @Override
            public void success(List<SimpleOrder> simpleOrders, Response response) {
                if (simpleOrders != null && simpleOrders.size() != 0) {
                    if (gridAdapter == null) {
                        contentHolder.setVisibility(View.VISIBLE);
                        gridAdapter = new GridBuyListAdapter(context, simpleOrders);
                        grid.setAdapter(gridAdapter);
                        grid.setOnItemClickListener(ItemSelected);
                    } else {
                        gridAdapter.addItems(simpleOrders);
                    }
                    loadingHolder.setVisibility(View.GONE);
                    findViewById(R.id.NothingFound).setVisibility(View.GONE);
                    LoadingFooter.setVisibility(View.VISIBLE);
                    LoadingFooter.findViewById(R.id.nointernet).setVisibility(View.GONE);
                    LoadingFooter.findViewById(R.id.loadingmore).setVisibility(View.VISIBLE);
                    LoadingFooter.findViewById(R.id.finished).setVisibility(View.GONE);
                    grid.invalidateViews();
                    currepage++;
                    LoadingStatus = false;
                }else {
                    if (gridAdapter == null) {
                        contentHolder.setVisibility(View.GONE);
                        loadingHolder.setVisibility(View.GONE);
                        LoadingFooter.setVisibility(View.GONE);
                        findViewById(R.id.NothingFound).setVisibility(View.VISIBLE);
                    } else {
                        loadingHolder.setVisibility(View.GONE);
                        findViewById(R.id.NothingFound).setVisibility(View.GONE);
                        LoadingFooter.setVisibility(View.VISIBLE);
                        LoadingFooter.findViewById(R.id.nointernet).setVisibility(View.GONE);
                        LoadingFooter.findViewById(R.id.loadingmore).setVisibility(View.GONE);
                        LoadingFooter.findViewById(R.id.finished).setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (gridAdapter != null) {
                    loadingHolder.setVisibility(View.GONE);
                    findViewById(R.id.NothingFound).setVisibility(View.GONE);
                    LoadingFooter.setVisibility(View.VISIBLE);
                    LoadingFooter.findViewById(R.id.nointernet).setVisibility(View.VISIBLE);
                    LoadingFooter.findViewById(R.id.loadingmore).setVisibility(View.GONE);
                    LoadingFooter.findViewById(R.id.finished).setVisibility(View.GONE);
                } else {
                    contentHolder.setVisibility(View.GONE);
                    findViewById(R.id.NothingFound).setVisibility(View.GONE);
                    loadingHolder.setVisibility(View.VISIBLE);
                    loadingHolder.findViewById(R.id.NoInternet).setVisibility(View.VISIBLE);
                    loadingHolder.findViewById(R.id.loadingmore).setVisibility(View.GONE);
                }
                grid.invalidateViews();
            }
        });
    } else {
        contentHolder.setVisibility(View.GONE);
        loadingHolder.setVisibility(View.GONE);
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
        if (position != 7) {
            super.CallActivity(position);
        } else {
            mDrawerLayout.closeDrawer(Gravity.RIGHT);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
    @Override
    protected void onResume() {
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
