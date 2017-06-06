package smo.zooket;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.srain.cube.views.GridViewWithHeaderAndFooter;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import smo.zooket.Adapter.GridOrederPro1Adapter;
import smo.zooket.Adapter.NavigationDrawerAdapter;
import smo.zooket.Adapter.SwipeLayout;
import smo.zooket.Models.DatabaseOrder;
import smo.zooket.Models.DatabaseUser;
import smo.zooket.Models.Order1Details;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Order1Activity extends BaseActivity {
    Activity context;

    Realm realm;
    GridViewWithHeaderAndFooter grid;
    GridOrederPro1Adapter gridAdapter;
    LinearLayout loadingHolder;
    LinearLayout contentHolder;
    SwipeLayout swipeLayout;

    Toolbar toolbar;
    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    DrawerLayout mDrawerLayout;

    boolean doubleBackToExitPressedOnce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order1);

        context=this;


        //toolbar
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ((ImageButton)toolbar.findViewById(R.id.back)).setVisibility(View.VISIBLE);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle("");
        ((ImageButton)toolbar.findViewById(R.id.basketbutton)).setVisibility(View.GONE);

        //Initialization
        doubleBackToExitPressedOnce = false;

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
        final GestureDetector mGestureDetector = new GestureDetector(Order1Activity.this, new GestureDetector.SimpleOnGestureListener() {
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
                            Intent myIntent = new Intent(Order1Activity.this, ProfileActivity.class);
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

        new FetchAsyncTask().execute();
    }

    private AdapterView.OnItemClickListener ItemSelected = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Order1Details item = (Order1Details) grid.getAdapter().getItem(position);
            if (item != null) {
                Intent MyIntent = new Intent(Order1Activity.this, Order2Activity.class);
                MyIntent.putExtra("ID", item.getID());
                MyIntent.putExtra("Name", item.getName());
                startActivity(MyIntent);
            }
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
                final RealmResults<DatabaseOrder> realmitems = r.where(DatabaseOrder.class).findAll();

                final List<Order1Details> items = new ArrayList<Order1Details>();

                List<Integer> ll=new ArrayList<>();
                List<String> names=new ArrayList<>();
                for (DatabaseOrder d:realmitems){
                    if (!ll.contains(d.getSupermarketID())){
                        ll.add(d.getSupermarketID());
                        names.add(d.getSupermarketName());
                    }
                }

                int countPro=0;
                int total=0;
                int profit=0;
                int i=0;
                for (int m:ll){
                    for (DatabaseOrder ord:realmitems){
                        if (ord.getSupermarketID()==m){
                            countPro+=Integer.parseInt(ord.getCountsOrder());
                            total=total+(Integer.parseInt(ord.getSuperPrice())*Integer.parseInt(ord.getCountsOrder()));
                            int superP=Integer.parseInt(ord.getSuperPrice());
                            int clientP=Integer.parseInt(ord.getClientPrice());
                            profit=profit+((clientP-superP)*Integer.parseInt(ord.getCountsOrder()));
                        }
                    }
                    Order1Details simpleItem = new Order1Details();
                    simpleItem.setID(m);
                    simpleItem.setName(names.get(i));
                    simpleItem.setCount(String.valueOf(countPro));
                    simpleItem.setTotal(String.valueOf(total));
                    simpleItem.setProfit(String.valueOf(profit));
                    items.add(simpleItem);
                    i++;
                }
                //for (int i = 0; i < realmitems.size(); i++) {
                   // Order1Details simpleItem = new Order1Details();

                   // simpleItem.setID(realmitems.get(i).getSupermarketID());
                  //  simpleItem.setName(realmitems.get(i).getSupermarketName());


                   // items.add(simpleItem);
             //   }

                if (items != null && items.size() != 0) {
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("itemSize=",String.valueOf(items.size()));
                            gridAdapter = new GridOrederPro1Adapter(context, items);
                            grid.setAdapter(gridAdapter);
                            grid.deferNotifyDataSetChanged();
                            loadingHolder.setVisibility(View.GONE);
                            contentHolder.setVisibility(View.VISIBLE);
                            grid.setOnItemClickListener(ItemSelected);
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
        if (position != 6) {
            super.CallActivity(position);
        } else {
            mDrawerLayout.closeDrawer(Gravity.RIGHT);
        }
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
