package smo.zooket;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import smo.zooket.Adapter.NavigationDrawerAdapter;
import smo.zooket.Adapter.ViewPagerAdapter;
import smo.zooket.Models.DatabaseUser;
import smo.zooket.Util.MaterialTab.MaterialTabs;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class ProSupFavoritActivity extends BaseActivity {

    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    DrawerLayout mDrawerLayout;
    MaterialTabs tabHost;
    ViewPager pager;
    ViewPagerAdapter pagerAdapter;
    boolean doubleBackToExitPressedOnce;
    private Toolbar toolbar;
    private Realm realm;
    Activity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pro_sup_favorit);
        context = this;

        //Initialization
        doubleBackToExitPressedOnce = false;

        //Attach DB
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(this).deleteRealmIfMigrationNeeded().build();
        realm = Realm.getInstance(realmConfig);

        //Font
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/BYekan.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );


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

        //Set Navigation Width
        Display display = this.getWindowManager().getDefaultDisplay();
//        mRecyclerView.getLayoutParams().width = display.getWidth() * 80 / 100;
        mRecyclerView.requestLayout();

        //Handling Navigation Drawer On Click
        final GestureDetector mGestureDetector = new GestureDetector(ProSupFavoritActivity.this, new GestureDetector.SimpleOnGestureListener() {
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
                            Intent myIntent = new Intent(ProSupFavoritActivity.this, ProfileActivity.class);
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

        //Init View Pager
        tabHost = (MaterialTabs) findViewById(R.id.tabHost);
        pager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        tabHost.setViewPager(pager);

        // Set Custom Font
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/BYekan.ttf");
        tabHost.setTypeface(typeface, Typeface.NORMAL);

        pager.setOffscreenPageLimit(5);
        pager.setCurrentItem(4);

    }


    public void ToggleDrawer(View view) {
        if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            mDrawerLayout.closeDrawer(Gravity.RIGHT);
        } else {
            mDrawerLayout.openDrawer(Gravity.RIGHT);
        }
    }

    public void SelectMenu(int position) {
        if (position != 2) {
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
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void UpdateUI() {
        DatabaseUser realmUser = realm.where(DatabaseUser.class).findFirst();
        mAdapter = new NavigationDrawerAdapter(realmUser);
        setUser(realmUser);
        mRecyclerView.setAdapter(mAdapter);
        mDrawerLayout.invalidate();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
