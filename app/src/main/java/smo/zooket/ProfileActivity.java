package smo.zooket;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import smo.zooket.API.APIHandler;
import smo.zooket.Adapter.NavigationDrawerAdapter;
import smo.zooket.Models.DatabaseUser;
import smo.zooket.Util.Toaster;
import smo.zooket.Util.Utils;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ProfileActivity extends BaseActivity {
    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    DrawerLayout mDrawerLayout;
    Realm realm;
    Activity context;
    private Toolbar toolbar;
    CheckBox changePass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        changePass = (CheckBox) findViewById(R.id.changepasscheckbox);
        context = this;

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
        mRecyclerView.requestLayout();

        //Set Data
        if (realmUser != null) {
            ((EditText) findViewById(R.id.Name)).setText(realmUser.getName());
            ((EditText) findViewById(R.id.Mobile)).setText(realmUser.getMobile());
            ((EditText) findViewById(R.id.Address)).setText(realmUser.getAddress());
        }
//Handling Navigation Drawer On Click
        final GestureDetector mGestureDetector = new GestureDetector(ProfileActivity.this, new GestureDetector.SimpleOnGestureListener() {
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
                        if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                            mDrawerLayout.closeDrawer(Gravity.RIGHT);
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

        changePass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    findViewById(R.id.changepassholder).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.changepassholder).setVisibility(View.GONE);
                }
            }
        });
    }
    public void Submit(final View view) {
        findViewById(R.id.sendholder).setEnabled(false);
        ((TextView) findViewById(R.id.sendbutton)).setText("لطفا صبر کنید...");
        findViewById(R.id.sendprogressbar).setVisibility(View.VISIBLE);
        findViewById(R.id.sendImage).setVisibility(View.GONE);

        final String password = ((EditText) findViewById(R.id.Password)).getText().toString().trim();
        String passwordconfirm = ((EditText) findViewById(R.id.PasswordConfirm)).getText().toString().trim();
        String currentpassword = ((EditText) findViewById(R.id.CurrentPassword)).getText().toString().trim();
        final String name = ((EditText) findViewById(R.id.Name)).getText().toString().trim();
        final String mobile = ((EditText) findViewById(R.id.Mobile)).getText().toString().trim();
        final String address = ((EditText) findViewById(R.id.Address)).getText().toString().trim();

        final DatabaseUser realmUser = realm.where(DatabaseUser.class).findFirst();
        String currentpass = "";
        String email = "";
        if (realmUser != null) {
            currentpass = realmUser.getPassword();
            email = realmUser.getUsername();
        }

        if (currentpassword.isEmpty() || !currentpassword.equals(currentpass)) {
            Toaster.toast(context, "رمز عبور فعلی درست نمی باشد", Toast.LENGTH_SHORT);
            findViewById(R.id.sendholder).setEnabled(true);
            ((TextView) findViewById(R.id.sendbutton)).setText("بروز رسانی تغییرات");
            findViewById(R.id.sendprogressbar).setVisibility(View.GONE);
            findViewById(R.id.sendImage).setVisibility(View.VISIBLE);
        } else if (changePass.isChecked() && (password.isEmpty() || password.length() < 5)) {
            Toaster.toast(context, "رمز عبور نباید کمتر از 5 کارکتر باشد", Toast.LENGTH_SHORT);
            findViewById(R.id.sendholder).setEnabled(true);
            ((TextView) findViewById(R.id.sendbutton)).setText("بروز رسانی تغییرات");
            findViewById(R.id.sendprogressbar).setVisibility(View.GONE);
            findViewById(R.id.sendImage).setVisibility(View.VISIBLE);
        } else if (changePass.isChecked() && (passwordconfirm.isEmpty() || !passwordconfirm.equals(password))) {
            Toaster.toast(context, "رمز عبور جدید و تکرار آن برابر نمی باشند", Toast.LENGTH_SHORT);
            findViewById(R.id.sendholder).setEnabled(true);
            ((TextView) findViewById(R.id.sendbutton)).setText("بروز رسانی تغییرات");
            findViewById(R.id.sendprogressbar).setVisibility(View.GONE);
            findViewById(R.id.sendImage).setVisibility(View.VISIBLE);
        } else if (name.isEmpty()) {
            Toaster.toast(context, "لطفا نام را وارد کنید", Toast.LENGTH_SHORT);
            findViewById(R.id.sendholder).setEnabled(true);
            ((TextView) findViewById(R.id.sendbutton)).setText("ثبت نام");
            findViewById(R.id.sendprogressbar).setVisibility(View.GONE);
            findViewById(R.id.sendImage).setVisibility(View.VISIBLE);
        } else if (mobile.isEmpty()) {
            Toaster.toast(context, "لطفا تلفن همراه را وارد کنید", Toast.LENGTH_SHORT);
            findViewById(R.id.sendholder).setEnabled(true);
            ((TextView) findViewById(R.id.sendbutton)).setText("ثبت نام");
            findViewById(R.id.sendprogressbar).setVisibility(View.GONE);
            findViewById(R.id.sendImage).setVisibility(View.VISIBLE);
        }else if (address.isEmpty()) {
            Toaster.toast(context, "لطفا آدرس را وارد کنید", Toast.LENGTH_SHORT);
            findViewById(R.id.sendholder).setEnabled(true);
            ((TextView) findViewById(R.id.sendbutton)).setText("ثبت نام");
            findViewById(R.id.sendprogressbar).setVisibility(View.GONE);
            findViewById(R.id.sendImage).setVisibility(View.VISIBLE);
        } else {
            String ts = Long.toString(System.currentTimeMillis() / 1000L);
            String key = Utils.SHA1(ts + email);
            APIHandler.getApiInterface().UserProfile(ts, key, email, currentpass, password, name, mobile, address, new Callback<Boolean>() {
               @Override
               public void success(Boolean aBoolean, Response response) {
                   if (aBoolean){
                       realm.beginTransaction();
                       realmUser.setMobile(mobile);
                       realmUser.setName(name);
                       realmUser.setAddress(address);
                       if (!password.isEmpty()){
                           realmUser.setPassword(password);
                       }
                       realm.commitTransaction();
                       //Refresh Drawer
                       DatabaseUser realmUser = realm.where(DatabaseUser.class).findFirst();
                       mAdapter = new NavigationDrawerAdapter(realmUser);
                       mRecyclerView.setAdapter(mAdapter);
                       mDrawerLayout.invalidate();

                       Toast.makeText(context, "عملیات باموفقیت انجام شد!", Toast.LENGTH_SHORT).show();
                       findViewById(R.id.sendholder).setEnabled(true);
                       ((TextView) findViewById(R.id.sendbutton)).setText("بروز رسانی تغییرات");
                       findViewById(R.id.sendprogressbar).setVisibility(View.GONE);
                       findViewById(R.id.sendImage).setVisibility(View.VISIBLE);
                   }else{
                       Toast.makeText(context, "خطا در برقراری ارتباط !!!", Toast.LENGTH_SHORT).show();
                       findViewById(R.id.sendholder).setEnabled(true);
                       ((TextView) findViewById(R.id.sendbutton)).setText("بروز رسانی تغییرات");
                       findViewById(R.id.sendprogressbar).setVisibility(View.GONE);
                       findViewById(R.id.sendImage).setVisibility(View.VISIBLE);
                   }
               }

               @Override
               public void failure(RetrofitError error) {
                   Toast.makeText(context, "خطا در برقراری ارتباط !!!", Toast.LENGTH_SHORT).show();
                   findViewById(R.id.sendholder).setEnabled(true);
                   ((TextView) findViewById(R.id.sendbutton)).setText("بروز رسانی تغییرات");
                   findViewById(R.id.sendprogressbar).setVisibility(View.GONE);
                   findViewById(R.id.sendImage).setVisibility(View.VISIBLE);
               }
           });
        }
    }
    public void Logout(View view) {
        //ChangePassword Successfully Down
        realm.beginTransaction();
        RealmResults<DatabaseUser> dbUsers = realm.where(DatabaseUser.class).findAll();
        dbUsers.clear();
        realm.commitTransaction();

        //Refresh Drawer
        DatabaseUser realmUser = realm.where(DatabaseUser.class).findFirst();
        mAdapter = new NavigationDrawerAdapter(realmUser);
        mRecyclerView.setAdapter(mAdapter);
        mDrawerLayout.invalidate();

        //Go To Main And Clear Stack
        MainPage(view);
    }

    public void MainPage(View view) {
        //Go To MainPage
        Intent myIntent = new Intent(ProfileActivity.this, MainActivity.class);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(myIntent);
        finish();
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
