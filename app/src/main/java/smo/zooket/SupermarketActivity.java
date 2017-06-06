package smo.zooket;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.TabLayout;
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
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import smo.zooket.API.APIHandler;
import smo.zooket.Adapter.NavigationDrawerAdapter;
import smo.zooket.Adapter.SupermarketTabViewPagerAdapter;
import smo.zooket.Adapter.ViewPagerGPSAdapter;
import smo.zooket.Models.DatabasePayWay;
import smo.zooket.Models.DatabaseSimpleSupermarket;
import smo.zooket.Models.DatabaseUser;
import smo.zooket.Models.SupermarketDetails;
import smo.zooket.Util.MaterialTab.MaterialTabs;
import smo.zooket.Util.Toaster;
import smo.zooket.Util.Utils;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SupermarketActivity extends BaseActivity {
    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    DrawerLayout mDrawerLayout;
    MaterialTabs tabHost;
    ViewPager pager;
    SupermarketTabViewPagerAdapter pagerAdapter;
    boolean doubleBackToExitPressedOnce;
    private Toolbar toolbar;
    private Realm realm;
    Activity context;

    Boolean fav=false;
    //all component
    ImageView like,favrit;
    TextView like_count, super_name;

    SupermarketDetails superDetails;
    int ID = 0;
    ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supermarket);

        context = this;

        //Initialization
        doubleBackToExitPressedOnce = false;
        ID = (int) getIntent().getExtras().get("ID");

        img = (ImageView) findViewById(R.id.ImageBack);

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
        final GestureDetector mGestureDetector = new GestureDetector(SupermarketActivity.this, new GestureDetector.SimpleOnGestureListener() {
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
                            Intent myIntent = new Intent(SupermarketActivity.this, ProfileActivity.class);
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
        pagerAdapter = new SupermarketTabViewPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        tabHost.setViewPager(pager);
        tabHost.setForegroundGravity(TabLayout.GRAVITY_CENTER);
        // Set Custom Font
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/BYekan.ttf");
        tabHost.setTypeface(typeface, Typeface.NORMAL);

        pager.setOffscreenPageLimit(3);
        pager.setCurrentItem(2);

        //initialize
        like = (ImageView) findViewById(R.id.Like);
        like_count = (TextView) findViewById(R.id.Like_Count);
        like_count.setText("0");
        super_name = (TextView) findViewById(R.id.SuperName);

        ((LinearLayout) findViewById(R.id.liked)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (superDetails.getLiked()) {
                    DisLike();
                } else {
                    Like();
                }

            }
        });


        GetSuperDetails();
    }
    public void Like() {
        DatabaseUser realmUser = realm.where(DatabaseUser.class).findFirst();
        if (!superDetails.getLiked()) {
            if (realmUser == null) {
                ShowLoginDialog();
            } else {
                String ts = Long.toString(System.currentTimeMillis() / 1000L);
                String key = Utils.SHA1(ts + realmUser.getUsername());

                superDetails.setLiked(true);
                like.setBackgroundResource(R.drawable.white_smo_like);
                int likeCount = Integer.parseInt(like_count.getText().toString()) + 1;
                like_count.setText(String.valueOf(likeCount));
                superDetails.setLikeCount(superDetails.getLikeCount() + 1);

                APIHandler.getApiInterface().UserLike(ts, key, realmUser.getUsername(), ID, new Callback<Boolean>() {
                    @Override
                    public void success(Boolean aBoolean, Response response) {
                        if (aBoolean == false) {
                            superDetails.setLiked(false);
                            like.setBackgroundResource(R.drawable.white_smo_dislike);
                            int likeCount = Integer.parseInt(like_count.getText().toString()) - 1;
                            like_count.setText(String.valueOf(likeCount));
                            superDetails.setLikeCount(superDetails.getLikeCount() - 1);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        superDetails.setLiked(false);
                        like.setBackgroundResource(R.drawable.white_smo_dislike);
                        int likeCount = Integer.parseInt(like_count.getText().toString()) - 1;
                        like_count.setText(String.valueOf(likeCount));
                        superDetails.setLikeCount(superDetails.getLikeCount() - 1);
                        Toaster.toast(context, "خطا در برقراری ارتباط !!!", Toast.LENGTH_SHORT);
                    }
                });
            }
        }
    }

    public void DisLike() {
        DatabaseUser realmUser = realm.where(DatabaseUser.class).findFirst();
        if (superDetails.getLiked()) {
            if (realmUser == null) {
                ShowLoginDialog();
            } else {
                String ts = Long.toString(System.currentTimeMillis() / 1000L);
                String key = Utils.SHA1(ts + realmUser.getUsername());

                superDetails.setLiked(false);
                like.setBackgroundResource(R.drawable.white_smo_dislike);
                int likeCount = Integer.parseInt(like_count.getText().toString()) - 1;
                like_count.setText(String.valueOf(likeCount));
                superDetails.setLikeCount(superDetails.getLikeCount() - 1);
                APIHandler.getApiInterface().UserDisLike(ts, key, realmUser.getUsername(), ID, new Callback<Boolean>() {
                    @Override
                    public void success(Boolean aBoolean, Response response) {
                        if (aBoolean == false) {
                            superDetails.setLiked(true);
                            like.setBackgroundResource(R.drawable.white_smo_like);
                            int likeCount = Integer.parseInt(like_count.getText().toString()) + 1;
                            like_count.setText(String.valueOf(likeCount));
                            superDetails.setLikeCount(superDetails.getLikeCount() + 1);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        superDetails.setLiked(true);
                        like.setBackgroundResource(R.drawable.white_smo_like);
                        int likeCount = Integer.parseInt(like_count.getText().toString()) + 1;
                        like_count.setText(String.valueOf(likeCount));
                        superDetails.setLikeCount(superDetails.getLikeCount() + 1);
                        Toaster.toast(context, "خطا در برقراری ارتباط !!!", Toast.LENGTH_SHORT);
                    }
                });
            }
        }
    }

    public void GetSuperDetails() {
        if (ID != -1) {
            String ts = Long.toString(System.currentTimeMillis() / 1000L);
            final DatabaseUser realmUser = realm.where(DatabaseUser.class).findFirst();
            String id = "NA";
            if (realmUser != null) {
                id = realmUser.getUsername();
            }
            String key = Utils.SHA1(ts);
            APIHandler.getApiInterface().getSupermarketDetails(ts, id, key, ID, new Callback<SupermarketDetails>() {
                @Override
                public void success(final SupermarketDetails item, Response response) {
                    if (item != null) {
                        superDetails = item;
                        if (item.getMainImage() != null && item.getMainImage() != "") {
                            Picasso.with(SupermarketActivity.this)
                                    .load(item.getMainImage())
                                    .placeholder(R.drawable.loading)
                                    .into(img);
                        }
                        if (item.getName() != null && item.getName() != "") {
                            super_name.setText(item.getName());
                        }


                        if (item.getLiked()) {
                            like.setBackgroundResource(R.drawable.white_smo_like);
                        } else {
                            like.setBackgroundResource(R.drawable.white_smo_dislike);
                        }
                        like_count.setText(String.valueOf(item.getLikeCount()));
                        CheckedFav();
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                }
            });
        }
    }
    public void AddToShoppingList(View view) {
        if (fav) {
            new RemoveAsyncTask(this).execute();
        } else {
            new SaveAsyncTask(this).execute();
        }
    }

    public void ShowReportDialog(View v) {
        final Activity context = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.create();
        Rect displayRectangle = new Rect();
        Window window = this.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.dialog_report, null);
        layout.setMinimumWidth((int) (displayRectangle.width() * 0.75f));
        layout.setMinimumHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        ViewGroup.LayoutParams params = layout.findViewById(R.id.DetailsHolder).getLayoutParams();
        params.width = (ViewGroup.LayoutParams.WRAP_CONTENT);
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        layout.findViewById(R.id.DetailsHolder).setLayoutParams(params);

        layout.findViewById(R.id.sendholder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.findViewById(R.id.sendholder).setEnabled(false);
                ((TextView) layout.findViewById(R.id.sendbutton)).setText("لطفا صبر کنید...");
                layout.findViewById(R.id.sendprogressbar).setVisibility(View.VISIBLE);
                layout.findViewById(R.id.sendImage).setVisibility(View.GONE);
                String Report = ((EditText) layout.findViewById(R.id.ReportTxt)).getText().toString().trim().toLowerCase();
                if (Report.isEmpty()) {
                    Toaster.toast(context, "متنی وارد نشده است", Toast.LENGTH_SHORT);
                    layout.findViewById(R.id.sendholder).setEnabled(true);
                    ((TextView) layout.findViewById(R.id.sendbutton)).setText("ارسال");
                    layout.findViewById(R.id.sendprogressbar).setVisibility(View.GONE);
                    layout.findViewById(R.id.sendImage).setVisibility(View.VISIBLE);
                } else {
                    String ts = Long.toString(System.currentTimeMillis() / 1000L);
                    String key = Utils.SHA1(ts);
                    APIHandler.getApiInterface().SendReport(ts, key, ID, Report, new Callback<Boolean>() {
                        @Override
                        public void success(Boolean aBoolean, Response response) {
                            if (aBoolean) {
                                dialog.dismiss();
                                Toaster.toast(context, "متن شما با موفقیت ارسال شد");
                            } else {
                                Toaster.toast(context, "خطا در برقراری ارتباط !!!", Toast.LENGTH_SHORT);
                                layout.findViewById(R.id.sendholder).setEnabled(true);
                                ((TextView) layout.findViewById(R.id.sendbutton)).setText("ثبت");
                                layout.findViewById(R.id.sendprogressbar).setVisibility(View.GONE);
                                layout.findViewById(R.id.sendImage).setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Toaster.toast(context, "خطا در برقراری ارتباط !!!", Toast.LENGTH_SHORT);
                            layout.findViewById(R.id.sendholder).setEnabled(true);
                            ((TextView) layout.findViewById(R.id.sendbutton)).setText("ثبت");
                            layout.findViewById(R.id.sendprogressbar).setVisibility(View.GONE);
                            layout.findViewById(R.id.sendImage).setVisibility(View.VISIBLE);
                        }
                    });

                }
            }
        });
        dialog.setView(layout);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }
    class SaveAsyncTask extends AsyncTask<Void, Void, Void> {
        Activity activity;
        boolean wasSuccessfull = false;
        private AlertDialog dialog;
        private View layout;

        SaveAsyncTask(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Realm r = null;
            try {
                RealmConfiguration realmConfig = new RealmConfiguration.Builder(getApplication().getApplicationContext()).deleteRealmIfMigrationNeeded().build();
                r = Realm.getInstance(realmConfig);
                r.beginTransaction();
                DatabaseSimpleSupermarket newitem = r.createObject(DatabaseSimpleSupermarket.class);

                newitem.setID(superDetails.getID());
                newitem.setProvince(superDetails.getProvince());
                newitem.setName(superDetails.getName());
                if (superDetails.getMainImage() != null && superDetails.getMainImage() != "") {
                    SaveImage(superDetails.getMainImage());
                    newitem.setMainImg(superDetails.getMainImage().substring(superDetails.getMainImage().lastIndexOf("/")));
                }
                r.commitTransaction();
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
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            //Dismiss Dialog Or Show
            if (wasSuccessfull) {
                ((ImageView) findViewById(R.id.favbtn)).setBackgroundResource(R.drawable.white_smo_fav);
                Toaster.toast(activity, "با موفقیت ثبت شد", Toast.LENGTH_SHORT);
                fav=true;
            } else {
                Toaster.toast(activity, "خطا! : لطفا دوباره تلاش کنید", Toast.LENGTH_SHORT);
            }
        }
    }

    class RemoveAsyncTask extends AsyncTask<Void, Void, Void> {
        Activity activity;
        boolean wasSuccessfull = false;
        private AlertDialog dialog;
        private View layout;

        RemoveAsyncTask(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Realm r = null;
            try {
                RealmConfiguration realmConfig = new RealmConfiguration.Builder(getApplication().getApplicationContext()).deleteRealmIfMigrationNeeded().build();
                r = Realm.getInstance(realmConfig);
                r.beginTransaction();
                RealmResults<DatabaseSimpleSupermarket> results = r.where(DatabaseSimpleSupermarket.class).equalTo("ID", superDetails.getID()).findAll();
                for (int i = 0; i < results.size(); i++) {
                    if (results.get(i).getMainImg() != null && !results.get(i).getMainImg().isEmpty()) {
                        RemoveImage(results.get(i).getMainImg());
                    }
                }
                results.clear();
                r.commitTransaction();
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
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            //Dismiss Dialog Or Show
            if (wasSuccessfull) {
                ((ImageView) findViewById(R.id.favbtn)).setBackgroundResource(R.drawable.white_smo_disfav);
                Toaster.toast(activity, "باموفقیت حذف شد", Toast.LENGTH_SHORT);
                fav=false;
            } else {
                Toaster.toast(activity, "خطا! : لطفا دوباره تلاش کنید", Toast.LENGTH_SHORT);
            }
        }


    }

    public void CheckedFav() {
        Realm r = null;
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(getApplication().getApplicationContext()).deleteRealmIfMigrationNeeded().build();
        r = Realm.getInstance(realmConfig);
        RealmResults<DatabaseSimpleSupermarket> results = r.where(DatabaseSimpleSupermarket.class).equalTo("ID", superDetails.getID()).findAll();
        // Toaster.toast(context, String.valueOf(results.size()), Toast.LENGTH_LONG);
        if (results.size()==0){
            ((ImageView) findViewById(R.id.favbtn)).setBackgroundResource(R.drawable.white_smo_disfav);
            fav=false;
        }else{
            ((ImageView) findViewById(R.id.favbtn)).setBackgroundResource(R.drawable.white_smo_fav);
            fav=true;
        }
    }

    private void RemoveImage(String image) throws Exception {
        File filepath = getFilesDir();
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
        File filepath = getFilesDir();
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






    public void ToggleDrawer(View view) {
        if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            mDrawerLayout.closeDrawer(Gravity.RIGHT);
        } else {
            mDrawerLayout.openDrawer(Gravity.RIGHT);
        }
    }

    public void SelectMenu(int position) {

        super.CallActivity(position);

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    public void Back(View view) {
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
