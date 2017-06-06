package smo.zooket;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import smo.zooket.API.APIHandler;
import smo.zooket.Adapter.NavigationDrawerAdapter;
import smo.zooket.Models.DatabaseSimpleProduct;
import smo.zooket.Models.DatabaseSimpleSupermarket;
import smo.zooket.Models.DatabaseUser;
import smo.zooket.Models.ProductDetails;
import smo.zooket.Models.SupermarketDetails;
import smo.zooket.Util.PersianCalendar;
import smo.zooket.Util.Toaster;
import smo.zooket.Util.Utils;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ProductPageActivity extends BaseActivity {
    //Set the radius of the Blur. Supported range 0 < radius <= 25
    private static final float BLUR_RADIUS = 12f;

    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    DrawerLayout mDrawerLayout;
    Realm realm;
    Activity context;
    private Toolbar toolbar;


    //all component
    ImageView like, comment,favorit;
    TextView like_count, comment_count, product_name, status, title_proname, category, brand, main_price, super_price, description;
    LinearLayout attr, feau;

    LinearLayout Loading_Failed;
    LinearLayout Loading_Load;
    LinearLayout loadingHolder;
    LinearLayout detailsHolder;

    int ID = 0;
    int SuperID=0;
String SuperName="";
    ImageView img;

    ProductDetails productDetail;

    String srcc="";

    Boolean LikeDisLike=false;

    Boolean fav=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_page);
        //blur
        img=(ImageView)findViewById(R.id.ImageBack);
     //   Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.noimage);
       // Bitmap blurredBitmap = blur(bitmap);
      //  img.setImageBitmap(blurredBitmap);

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

        //Handling Navigation Drawer On Click
        final GestureDetector mGestureDetector = new GestureDetector(ProductPageActivity.this, new GestureDetector.SimpleOnGestureListener() {
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
                            Intent myIntent = new Intent(ProductPageActivity.this, ProfileActivity.class);
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
        loadingHolder = (LinearLayout) findViewById(R.id.loadingHolder);
        detailsHolder = (LinearLayout) findViewById(R.id.contentHolder);
        //Initialization
        try {
            Uri data = getIntent().getData();
            List<String> params = data.getPathSegments();
            ID = Integer.parseInt(params.get(2));
        } catch (Exception ex) {
            if (getIntent().getExtras().containsKey("ID")) {
                ID = (int) getIntent().getExtras().get("ID");
            } else {
                ID = -1;
                loadingHolder.setVisibility(View.GONE);
                detailsHolder.setVisibility(View.GONE);
                findViewById(R.id.NotFound).setVisibility(View.VISIBLE);
            }
        }
        SuperID = (int) getIntent().getExtras().get("SuperID");
        SuperName = (String) getIntent().getExtras().get("SuperName");

        //Set Loading
        Loading_Load = (LinearLayout) loadingHolder.findViewById(R.id.loadingmore);
        Loading_Failed = (LinearLayout) loadingHolder.findViewById(R.id.NoInternet);
        Loading_Failed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Loading_Load.setVisibility(View.VISIBLE);
                Loading_Failed.setVisibility(View.GONE);
            }
        });

        //initialize
        like = (ImageView) findViewById(R.id.Like);
        comment = (ImageView) findViewById(R.id.Comment);
        favorit = (ImageView) findViewById(R.id.favbtn);

        like_count = (TextView) findViewById(R.id.Like_Count);
        like_count.setText("0");
        comment_count = (TextView) findViewById(R.id.Comment_Count);
        comment_count.setText("0");

        product_name = (TextView) findViewById(R.id.Product_Name);
        title_proname = (TextView) findViewById(R.id.Pro_Name);
        category = (TextView) findViewById(R.id.Category);//current date
        status=(TextView)findViewById(R.id.IsAvailable);
        brand = (TextView) findViewById(R.id.Brand);
        main_price = (TextView) findViewById(R.id.MainPrice);
        super_price = (TextView) findViewById(R.id.SuperPrice);

        attr = (LinearLayout) findViewById(R.id.AttrContainer);
        feau = (LinearLayout) findViewById(R.id.FeaContainer);

        description = (TextView) findViewById(R.id.Description);


        GetProductDetails();

        ((LinearLayout)findViewById(R.id.liked)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (productDetail.getLiked()){
                    DisLike();
                }else{
                    Like();
                }

            }
        });
        ((LinearLayout)findViewById(R.id.Comments)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(ProductPageActivity.this, CommentProductActivity.class);
                myIntent.putExtra("ID", ID);
                myIntent.putExtra("Name", productDetail.getName());
                startActivity(myIntent);
            }
        });


    }

    public void GetProductDetails(){
        if (ID!=-1) {
            String ts = Long.toString(System.currentTimeMillis() / 1000L);
            String key = Utils.SHA1(ts);
            DatabaseUser realmUser = realm.where(DatabaseUser.class).findFirst();
            String id = "NA";
            if (realmUser != null) {
                id = realmUser.getUsername();
            }
            APIHandler.getApiInterface().getProductsDetails(ts, id, key, ID, new Callback<ProductDetails>() {
                @Override
                public void success(final ProductDetails item, Response response) {
                    if (item!=null){
                        productDetail=item;
                        if (item.getMainImage()!= null && item.getMainImage()!=""){
                            srcc=item.getMainImage();
                                    Picasso.with(context)
                                            .load(item.getMainImage())
                                            .placeholder(R.drawable.loading)
                                            .into(img);
                        }
                        if (item.getAttributes()!= null && item.getAttributes()!="") {
                            String[] res=item.getAttributes().split("-");
                            for (int i=0;i<res.length;i++){
                                TextView t=new TextView(context);
                                LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                t.setLayoutParams(lparams);
                                t.setBackgroundResource(R.drawable.round);
                                t.setText(res[i]);
                                t.setGravity(View.FOCUS_RIGHT);
                                t.setPadding(4,4,4,4);
                                t.setTextColor(getResources().getColor(R.color.Black));
                                t.setTextSize(15f);
                                t.setTypeface(null, Typeface.BOLD);

                                attr.addView(t);
                            }
                        }
                        if (item.getAvailable()==true) {
status.setText("موجود است");
                        }else {
                            status.setText("موجود نیست");
                        }
                        if (item.getCategory()!= null && item.getCategory()!="") {
category.setText(item.getCategory());
                        }
                        if (item.getCompany()!= null && item.getCompany()!="") {
brand.setText(item.getCompany());
                        }
                        if (item.getDescription()!= null && item.getDescription()!="") {
description.setText(item.getDescription());
                        }
                        if (item.getFeatures()!= null && item.getFeatures()!="") {
                            String[] res=item.getFeatures().split("-");
                            for (int i=0;i<res.length;i++){
                                TextView t=new TextView(context);
                                LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                t.setLayoutParams(lparams);
                                t.setBackgroundResource(R.drawable.round);
                                t.setText(res[i]);
                                t.setGravity(View.FOCUS_RIGHT);
                                t.setPadding(4,4,4,4);
                                t.setTextColor(getResources().getColor(R.color.Black));
                                t.setTextSize(15f);
                                t.setTypeface(null, Typeface.BOLD);

                                feau.addView(t);
                            }
                        }
                        if (item.getName()!= null && item.getName()!="") {
product_name.setText(item.getName());
                            title_proname.setText(item.getName());
                        }
                        if (item.getOffPrice()!= null && item.getOffPrice()!="") {
super_price.setText(item.getOffPrice()+" تومان");
                        }
                        if (item.getPrice()!= null && item.getPrice()!="") {
main_price.setText(item.getPrice()+" تومان");
                        }

                        if (item.getLiked()) {
                            like.setBackgroundResource(R.drawable.smo_like);
                        }else{
                            like.setBackgroundResource(R.drawable.smo_dislike);
                        }
                        like_count.setText(String.valueOf(item.getLikeCount()));
                        comment_count.setText(String.valueOf(item.getCommentCount()));

                        //checkFromDatabase
                        CheckedFav();

                        detailsHolder.setVisibility(View.VISIBLE);
                        loadingHolder.setVisibility(View.GONE);
                    }else{
                        loadingHolder.setVisibility(View.GONE);
                        detailsHolder.setVisibility(View.GONE);
                        findViewById(R.id.NotFound).setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    Loading_Load.setVisibility(View.GONE);
                    Loading_Failed.setVisibility(View.VISIBLE);
                }
            });
        }
    }
    //bitmap for bluring img
    public class MyAsync extends AsyncTask<Void, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Void... params) {

            try {
                URL url = new URL(srcc);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

        }
    }
  //  public Bitmap blur(Bitmap image) {
   //     if (null == image) return null;

    ///    Bitmap outputBitmap = Bitmap.createBitmap(image);
   //     final RenderScript renderScript = RenderScript.create(this);
     //   Allocation tmpIn = Allocation.createFromBitmap(renderScript, image);
    //    Allocation tmpOut = Allocation.createFromBitmap(renderScript, outputBitmap);

       /////////// //Intrinsic Gausian blur filter
    //    ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
    //    theIntrinsic.setRadius(BLUR_RADIUS);
    ////    theIntrinsic.setInput(tmpIn);
     //   theIntrinsic.forEach(tmpOut);
      //  tmpOut.copyTo(outputBitmap);
      //  return outputBitmap;
    //}
    public void CheckedFav(){
        Realm r = null;
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(getApplication().getApplicationContext()).deleteRealmIfMigrationNeeded().build();
        r = Realm.getInstance(realmConfig);
        RealmResults<DatabaseSimpleProduct> results = r.where(DatabaseSimpleProduct.class).equalTo("ID", productDetail.getID()).findAll();
        //Toaster.toast(context,String.valueOf("product Size= "+results.size()),Toast.LENGTH_LONG);
        if (results.size()==0){
            ((ImageView) findViewById(R.id.favbtn)).setBackgroundResource(R.drawable.smo_disfav);
fav=false;
        }else{
            ((ImageView) findViewById(R.id.favbtn)).setBackgroundResource(R.drawable.smo_fav);
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
    class SaveAsyncTask extends AsyncTask<Void, Void, Void> {
        Activity activity;
        boolean wasSuccessfull = false;
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
                DatabaseSimpleProduct newitem = r.createObject(DatabaseSimpleProduct.class);

                newitem.setID(productDetail.getID());
                newitem.setName(productDetail.getName());
                newitem.setSuperPrice(productDetail.getOffPrice());
                newitem.setMainPrice(productDetail.getPrice());
                newitem.setSupermarketName(SuperName);
                newitem.setSupermarketID(SuperID);
                if (productDetail.getMainImage() != null && productDetail.getMainImage() != "") {
                    SaveImage(productDetail.getMainImage());
                    newitem.setMainImg(productDetail.getMainImage().substring(productDetail.getMainImage().lastIndexOf("/")));
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
                ((ImageView) findViewById(R.id.favbtn)).setBackgroundResource(R.drawable.smo_fav);
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
                RealmResults<DatabaseSimpleProduct> results = r.where(DatabaseSimpleProduct.class).equalTo("ID", productDetail.getID()).findAll();
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
                ((ImageView) findViewById(R.id.favbtn)).setBackgroundResource(R.drawable.smo_disfav);
                Toaster.toast(activity, "باموفقیت حذف شد", Toast.LENGTH_SHORT);
                fav=false;
            } else {
                Toaster.toast(activity, "خطا! : لطفا دوباره تلاش کنید", Toast.LENGTH_SHORT);
            }
        }


    }
    public void AddToShoppingList(View view) {
        if (fav) {
            new RemoveAsyncTask(this).execute();
        } else {
            new SaveAsyncTask(this).execute();
        }
    }

    public void Like(){
        DatabaseUser realmUser = realm.where(DatabaseUser.class).findFirst();
        if(!productDetail.getLiked()){
            if (realmUser==null){
                ShowLoginDialog();
            }else{
                String ts = Long.toString(System.currentTimeMillis() / 1000L);
                String key = Utils.SHA1(ts + realmUser.getUsername());

                productDetail.setLiked(true);
                like.setBackgroundResource(R.drawable.smo_like);
                int likeCount=Integer.parseInt(like_count.getText().toString())+1;
                like_count.setText(String.valueOf(likeCount));
                productDetail.setLikeCount(productDetail.getLikeCount() + 1);

                APIHandler.getApiInterface().UserProLike(ts, key, realmUser.getUsername(), ID, new Callback<Boolean>() {
                    @Override
                    public void success(Boolean aBoolean, Response response) {
                        if (aBoolean==false) {
                            productDetail.setLiked(false);
                            like.setBackgroundResource(R.drawable.smo_dislike);
                            int likeCount=Integer.parseInt(like_count.getText().toString())-1;
                            like_count.setText(String.valueOf(likeCount));
                            productDetail.setLikeCount(productDetail.getLikeCount() - 1);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        productDetail.setLiked(false);
                        like.setBackgroundResource(R.drawable.smo_dislike);
                        int likeCount=Integer.parseInt(like_count.getText().toString())-1;
                        like_count.setText(String.valueOf(likeCount));
                        productDetail.setLikeCount(productDetail.getLikeCount() - 1);
                        Toaster.toast(context,"خطا در برقراری ارتباط !!!",Toast.LENGTH_SHORT);
                    }
                });
            }
        }
    }
    public void DisLike(){
        DatabaseUser realmUser = realm.where(DatabaseUser.class).findFirst();
        if(productDetail.getLiked()) {
            if (realmUser == null) {
                ShowLoginDialog();
            } else {
                String ts = Long.toString(System.currentTimeMillis() / 1000L);
                String key = Utils.SHA1(ts + realmUser.getUsername());

                productDetail.setLiked(false);
                like.setBackgroundResource(R.drawable.smo_dislike);
                int likeCount = Integer.parseInt(like_count.getText().toString()) - 1;
                like_count.setText(String.valueOf(likeCount));
                productDetail.setLikeCount(productDetail.getLikeCount() - 1);
                APIHandler.getApiInterface().UserProDisLike(ts, key, realmUser.getUsername(), ID, new Callback<Boolean>() {
                    @Override
                    public void success(Boolean aBoolean, Response response) {
                        if (aBoolean == false) {
                            productDetail.setLiked(true);
                            like.setBackgroundResource(R.drawable.smo_like);
                            int likeCount = Integer.parseInt(like_count.getText().toString()) + 1;
                            like_count.setText(String.valueOf(likeCount));
                            productDetail.setLikeCount(productDetail.getLikeCount() + 1);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        productDetail.setLiked(true);
                        like.setBackgroundResource(R.drawable.smo_like);
                        int likeCount=Integer.parseInt(like_count.getText().toString())+1;
                        like_count.setText(String.valueOf(likeCount));
                        productDetail.setLikeCount(productDetail.getLikeCount() + 1);
                        Toaster.toast(context,"خطا در برقراری ارتباط !!!",Toast.LENGTH_SHORT);
                    }
                });
            }
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
