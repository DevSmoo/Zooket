package smo.zooket;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
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
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.api.Api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import in.srain.cube.views.GridViewWithHeaderAndFooter;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import smo.zooket.API.APIHandler;
import smo.zooket.Adapter.ExpandableListAdapter1;
import smo.zooket.Adapter.GridItemAdapter;
import smo.zooket.Adapter.NavigationDrawerAdapter;
import smo.zooket.Adapter.SwipeLayout;
import smo.zooket.Models.DatabaseUser;
import smo.zooket.Models.GpsSupermarket;
import smo.zooket.Util.Utils;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends BaseActivity {
    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    DrawerLayout mDrawerLayout;

    boolean doubleBackToExitPressedOnce;
    Activity context;
    Toolbar toolbar;
    Realm realm;

    GridViewWithHeaderAndFooter grid;
    GridItemAdapter gridAdapter;
    boolean LoadingStatus = false;
    View LoadingFooter;

    LinearLayout contentHolder;
    LinearLayout loadingHolder;

    int currentPage;
    int SortType;

    LayoutInflater layoutInflater;
    SwipeLayout swipeLayout;

    boolean Load;

String DistrictName="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //toolbar
        toolbar=(Toolbar)findViewById(R.id.toolr);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle("");
        ((ImageButton)toolbar.findViewById(R.id.basketbutton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,Order1Activity.class));
            }
        });

        //Initialization
        doubleBackToExitPressedOnce = false;
        context = this;

        DistrictName=(String) getIntent().getExtras().get("DistrictName");

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
        final GestureDetector mGestureDetector = new GestureDetector(MainActivity.this, new GestureDetector.SimpleOnGestureListener() {
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
                            Intent myIntent = new Intent(MainActivity.this, ProfileActivity.class);
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

        grid.setSmoothScrollbarEnabled(true);

        //Set Loading
        loadingHolder.findViewById(R.id.NoInternet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingHolder.findViewById(R.id.loadingmore).setVisibility(View.VISIBLE);
                loadingHolder.findViewById(R.id.NoInternet).setVisibility(View.GONE);
                GetSupermarket();
            }
        });

        LoadingFooter.findViewById(R.id.nointernet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoadingFooter.setVisibility(View.VISIBLE);
                LoadingFooter.findViewById(R.id.loadingmore).setVisibility(View.VISIBLE);
                LoadingFooter.findViewById(R.id.nointernet).setVisibility(View.GONE);
                LoadingFooter.findViewById(R.id.finished).setVisibility(View.GONE);
                GetSupermarket();
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
                    GetSupermarket();
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
    }

    private AdapterView.OnItemClickListener ItemSelected = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            GpsSupermarket item = (GpsSupermarket) grid.getAdapter().getItem(position);
            if (item != null) {
                Intent MyIntent = new Intent(MainActivity.this, SupermarketActivity.class);
                MyIntent.putExtra("ID", item.getID());
                MyIntent.putExtra("Name", item.getName());
                startActivity(MyIntent);
            }
        }
    };

    public void GetSupermarket() {
        if (Load) {
            String ts = Long.toString(System.currentTimeMillis() / 1000L);
            String key = Utils.SHA1(ts);

            APIHandler.getApiInterface().GetDistrictSupermarket(ts, key, currentPage, DistrictName, new Callback<List<GpsSupermarket>>() {
                @Override
                public void success(List<GpsSupermarket> gpsSupermarkets, Response response) {
                    if (gpsSupermarkets != null && gpsSupermarkets.size() != 0) {
                        if (gridAdapter == null) {
                            contentHolder.setVisibility(View.VISIBLE);
                            gridAdapter = new GridItemAdapter(context, gpsSupermarkets);
                            grid.setAdapter(gridAdapter);
                            grid.setOnItemClickListener(ItemSelected);
                        } else {
                            gridAdapter.addItems(gpsSupermarkets);
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

    public void SelectMenu(int position) {
        if (position != 1) {
            super.CallActivity(position);
        } else {
            mDrawerLayout.closeDrawer(Gravity.RIGHT);
        }
    }


    public void Back(View view) {
        onBackPressed();
    }

    @Override
    protected void onResume() {
        UpdateUI();
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
