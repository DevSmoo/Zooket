package smo.zooket;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.srx.widget.PullToLoadView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.StringTokenizer;

import in.srain.cube.views.GridViewWithHeaderAndFooter;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import smo.zooket.API.APIHandler;
import smo.zooket.Adapter.GridItemAdapter;
import smo.zooket.Adapter.GridProductAdapter;
import smo.zooket.Adapter.NavigationDrawerAdapter;
import smo.zooket.Adapter.RecyclerProductAdapter;
import smo.zooket.Adapter.SwipeLayout;
import smo.zooket.Models.DatabaseOrder;
import smo.zooket.Models.DatabaseUser;
import smo.zooket.Models.SimpleProduct;
import smo.zooket.Models.SimpleSupermarket;
import smo.zooket.Util.Toaster;
import smo.zooket.Util.Utils;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ProductListActivity extends BaseActivity {
    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    DrawerLayout mDrawerLayout;

    boolean doubleBackToExitPressedOnce;
    Activity context;
    Toolbar toolbar;
    Realm realm;

    GridViewWithHeaderAndFooter grid;
    GridProductAdapter gridAdapter;
    boolean LoadingStatus = false;
    View LoadingFooter;

    LinearLayout contentHolder;
    LinearLayout loadingHolder;

    int currentPage;
    String Name;
    int SortType;

    LayoutInflater layoutInflater;
    SwipeLayout swipeLayout;

    boolean Load;
    private boolean LoadedBefore = false;
    int ID;


    //for show by recycler
    RecyclerView recyclerView;
    RecyclerProductAdapter recyclerProductAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);


        //toolbar
        toolbar=(Toolbar)findViewById(R.id.toolr);
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
        context = this;

        //Attach DB
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(this).deleteRealmIfMigrationNeeded().build();
        realm = Realm.getInstance(realmConfig);


        //Navigation Drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.DrawerLayout);
        mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView);
        mRecyclerView.setHasFixedSize(true);
        DatabaseUser realmUser = realm.where(DatabaseUser.class).findFirst();
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
        final GestureDetector mGestureDetector = new GestureDetector(ProductListActivity.this, new GestureDetector.SimpleOnGestureListener() {
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
                            Intent myIntent = new Intent(ProductListActivity.this, ProfileActivity.class);
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
        currentPage = 1;
        Load = true;
        SortType = 1;
        grid.setSmoothScrollbarEnabled(true);

        //Set Loading
        loadingHolder.findViewById(R.id.NoInternet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingHolder.findViewById(R.id.loadingmore).setVisibility(View.VISIBLE);
                loadingHolder.findViewById(R.id.NoInternet).setVisibility(View.GONE);
                GetProductRecycler(v);
            }
        });

        LoadingFooter.findViewById(R.id.nointernet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoadingFooter.setVisibility(View.VISIBLE);
                LoadingFooter.findViewById(R.id.loadingmore).setVisibility(View.VISIBLE);
                LoadingFooter.findViewById(R.id.nointernet).setVisibility(View.GONE);
                LoadingFooter.findViewById(R.id.finished).setVisibility(View.GONE);
                GetProductRecycler(v);
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
                    GetProductRecycler(view);
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

        //Pull To Refresh
        swipeLayout = (SwipeLayout) findViewById(R.id.pulltorefresh_container);
        swipeLayout.setEnabled(false);


        //show by recycler
        recyclerView=(RecyclerView)findViewById(R.id.recycleProduct);
        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(manager);

    }

    //region clickgridview
    private AdapterView.OnItemClickListener ItemSelected = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            final SimpleProduct item = (SimpleProduct) grid.getAdapter().getItem(position);

            View convertView=grid.getAdapter().getView(position,view,parent);

            TextView name = (TextView) convertView.findViewById(R.id.name);
            final TextView mainPrice = (TextView) convertView.findViewById(R.id.main_price);
            TextView superPrice = (TextView) convertView.findViewById(R.id.super_price);

            final LinearLayout buy=(LinearLayout) convertView.findViewById(R.id.buy);
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

                    RealmConfiguration realmConfig = new RealmConfiguration.Builder(context).deleteRealmIfMigrationNeeded().build();
                    Realm realm = Realm.getInstance(realmConfig);
                    DatabaseOrder results = realm.where(DatabaseOrder.class).equalTo("ProductID", item.getID()).findFirst();
                    if (results==null) {
                       // SaveAsyncTask save = new SaveAsyncTask(item);
                       // save.execute();
                    }else{
                        realm.beginTransaction();
                        DatabaseOrder result = realm.where(DatabaseOrder.class).equalTo("ProductID", item.getID()).findFirst();
                        counterTxt.setText(result.getCountsOrder());
                        if (result!=null) {
                            result.setCountsOrder(counterTxt.getText().toString());
                        }
                        realm.commitTransaction();
                    }
                    buy_more_container.setVisibility(View.GONE);
                    counter_container.setVisibility(View.VISIBLE);
                }
            });



            dec.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int a=Integer.parseInt(counterTxt.getText().toString());

                    if (a==1){
                       // RemoveAsyncTask rem=new RemoveAsyncTask(item);
                        //rem.execute();

                        buy_more_container.setVisibility(View.VISIBLE);
                        counter_container.setVisibility(View.GONE);
                    }else{
                        a--;
                        counterTxt.setText(String.valueOf(a));

                        RealmConfiguration realmConfig = new RealmConfiguration.Builder(context).deleteRealmIfMigrationNeeded().build();
                        Realm realm = Realm.getInstance(realmConfig);
                        realm.beginTransaction();
                        DatabaseOrder results = realm.where(DatabaseOrder.class).equalTo("ProductID", item.getID()).findFirst();
                        if (results!=null) {
                            results.setCountsOrder(counterTxt.getText().toString());
                        }
                        realm.commitTransaction();
                    }
                }
            });
            inc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int a=Integer.parseInt(counterTxt.getText().toString());
                    a++;
                    counterTxt.setText(String.valueOf(a));

                    RealmConfiguration realmConfig = new RealmConfiguration.Builder(context).deleteRealmIfMigrationNeeded().build();
                    Realm realm = Realm.getInstance(realmConfig);
                    realm.beginTransaction();
                    DatabaseOrder results = realm.where(DatabaseOrder.class).equalTo("ProductID", item.getID()).findFirst();
                    if (results!=null) {
                        results.setCountsOrder(String.valueOf(a));
                    }
                    realm.commitTransaction();
                }
            });
            showMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent MyIntent = new Intent(context, ProductPageActivity.class);
                    MyIntent.putExtra("ID", item.getID());
                    MyIntent.putExtra("SuperID", ID);
                    MyIntent.putExtra("SuperName", Name);
                    startActivity(MyIntent);
                }
            });

           // SimpleSupermarket item = (SimpleSupermarket) grid.getAdapter().getItem(position);
            if (item != null) {
                // Toaster.toast(context,"آفرین عالی",Toast.LENGTH_LONG);
              //  Intent MyIntent = new Intent(MainActivity.this, SupermarketPageActivity.class);
                //MyIntent.putExtra("ID", item.getID());
                //startActivity(MyIntent);
            }
        }
    };

    public void GetProduct(View view){
        //Initialization
        ID = (int) getIntent().getExtras().get("ID");
        Name = (String) getIntent().getExtras().get("Name");
        String ts = Long.toString(System.currentTimeMillis() / 1000L);
        String key = Utils.SHA1(ts);
        APIHandler.getApiInterface().getProductItem(ts, key, currentPage, ID, new Callback<List<SimpleProduct>>() {
            @Override
            public void success(List<SimpleProduct> simpleProducts, Response response) {
               // Toaster.toast(context,String.valueOf(simpleProducts.size()),Toast.LENGTH_SHORT);
                if (simpleProducts != null && simpleProducts.size() != 0) {
                    if (gridAdapter == null) {
                        contentHolder.setVisibility(View.VISIBLE);
                        gridAdapter = new GridProductAdapter(context, simpleProducts,ID,Name);
                        grid.setAdapter(gridAdapter);
                        grid.setOnItemClickListener(ItemSelected);
                    } else {
                        gridAdapter.addItems(simpleProducts);
                    }
                    loadingHolder.setVisibility(View.GONE);
                    findViewById(R.id.NothingFound).setVisibility(View.GONE);
                    LoadingFooter.setVisibility(View.VISIBLE);
                    LoadingFooter.findViewById(R.id.nointernet).setVisibility(View.GONE);
                    LoadingFooter.findViewById(R.id.loadingmore).setVisibility(View.VISIBLE);
                    LoadingFooter.findViewById(R.id.finished).setVisibility(View.GONE);
                    grid.invalidateViews();
                    currentPage++;
                    LoadingStatus = false;
                }else{
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
    }
//endregion

    public void GetProductRecycler(View view){
        //Initialization
        ID = (int) getIntent().getExtras().get("ID");
        Name = (String) getIntent().getExtras().get("Name");
        String ts = Long.toString(System.currentTimeMillis() / 1000L);
        String key = Utils.SHA1(ts);
        APIHandler.getApiInterface().getProductItem(ts, key, currentPage, ID, new Callback<List<SimpleProduct>>() {
            @Override
            public void success(List<SimpleProduct> simpleProducts, Response response) {
              //  Toaster.toast(context,"recycler",Toast.LENGTH_SHORT);
                if (simpleProducts != null && simpleProducts.size() != 0) {
                    if (gridAdapter == null) {
                        contentHolder.setVisibility(View.VISIBLE);
                        recyclerProductAdapter = new RecyclerProductAdapter(context, simpleProducts,ID,Name);
                        recyclerView.setAdapter(recyclerProductAdapter);
                    } else {
                        recyclerProductAdapter.addItems(simpleProducts);
                    }
                    loadingHolder.setVisibility(View.GONE);
                    findViewById(R.id.NothingFound).setVisibility(View.GONE);
                    LoadingFooter.setVisibility(View.VISIBLE);
                    LoadingFooter.findViewById(R.id.nointernet).setVisibility(View.GONE);
                    LoadingFooter.findViewById(R.id.loadingmore).setVisibility(View.VISIBLE);
                    LoadingFooter.findViewById(R.id.finished).setVisibility(View.GONE);
                    currentPage++;
                    LoadingStatus = false;
                }else{
                    if (recyclerProductAdapter == null) {
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
                if (recyclerProductAdapter != null) {
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
            }
        });
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
