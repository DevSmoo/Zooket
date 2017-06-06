package smo.zooket;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import in.srain.cube.views.GridViewWithHeaderAndFooter;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import smo.zooket.API.APIHandler;
import smo.zooket.Adapter.GridProductAdapter;
import smo.zooket.Adapter.RecyclerProductAdapter;
import smo.zooket.Adapter.SwipeLayout;
import smo.zooket.Models.Category;
import smo.zooket.Models.DatabaseOrder;
import smo.zooket.Models.SimpleProduct;
import smo.zooket.Util.Toaster;
import smo.zooket.Util.Utils;
import smo.zooket.testC.MyAdapter;
import smo.zooket.testC.MyAdapter1;

/**
 * Created by Smo on 07/06/2017.
 */
public class SupermarketProductFragment extends Fragment {
View view;

    private RecyclerView rview;
    private RecyclerView.LayoutManager layoutManager;
    private MyAdapter madapter;

    private RecyclerView rview1;
    private RecyclerView.LayoutManager layoutManager1;
    private MyAdapter1 madapter1;

    Context context;
    String Cate;
    String Subcate;
    int currentPage;

    boolean doubleBackToExitPressedOnce;
    Realm realm;

    GridViewWithHeaderAndFooter grid;
    GridProductAdapter gridAdapter;
    boolean LoadingStatus = false;
    View LoadingFooter;
    LinearLayout contentHolder;
    LinearLayout loadingHolder;

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.supermarket_product_fragment, container, false);

        context = getActivity();
        Cate="";
        Subcate="";
        currentPage = 1;
        Subcate="همه";

        //Initialization
        doubleBackToExitPressedOnce = false;

        //Attach DB
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(context).deleteRealmIfMigrationNeeded().build();
        realm = Realm.getInstance(realmConfig);
        layoutInflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        grid = (GridViewWithHeaderAndFooter) view.findViewById(R.id.itemgrid);

        loadingHolder = (LinearLayout) view.findViewById(R.id.loadingHolder);
        contentHolder = (LinearLayout) view.findViewById(R.id.contentHolder);
        LoadingFooter = layoutInflater.inflate(R.layout.loading, null);
        grid.addFooterView(LoadingFooter);
        LoadingFooter.setVisibility(View.GONE);

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
                GetProductRecycler();
            }
        });

        LoadingFooter.findViewById(R.id.nointernet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoadingFooter.setVisibility(View.VISIBLE);
                LoadingFooter.findViewById(R.id.loadingmore).setVisibility(View.VISIBLE);
                LoadingFooter.findViewById(R.id.nointernet).setVisibility(View.GONE);
                LoadingFooter.findViewById(R.id.finished).setVisibility(View.GONE);
                GetProductRecycler();
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
        swipeLayout = (SwipeLayout) view.findViewById(R.id.pulltorefresh_container);
        swipeLayout.setEnabled(false);


        //1
        rview = (RecyclerView) view.findViewById(R.id.RecyclerView);
        layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, true);
        rview.setLayoutManager(layoutManager);
        //2
        rview1 = (RecyclerView) view.findViewById(R.id.RecyclerView1);
        layoutManager1 = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, true);
        rview1.setLayoutManager(layoutManager1);


        //show by recycler
        recyclerView=(RecyclerView)view.findViewById(R.id.recycleProduct);
        LinearLayoutManager manager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(manager);

        GetCategory();
        GetFirstSubCategory();

        return view;
    }


    public void GetCategory() {
        final ArrayList<Category> CategoryList = new ArrayList<>();
        String ts = Long.toString(System.currentTimeMillis() / 1000L);
        String key = Utils.SHA1(ts);
        APIHandler.getApiInterface().GetCatgeory(ts, key, new Callback<List<Category>>() {
            @Override
            public void success(List<Category> categories, Response response) {
                if (categories != null) {

                    Cate=categories.get(0).getName();

                    GetProductRecycler();
                    CategoryList.addAll(categories);
                    madapter = new MyAdapter(CategoryList, context, new MyAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(Category item) {
                            GetSubCategory(item.getID());

                            Cate=item.getName();
                            Subcate="همه";
                            GetProductRecycler();
                        }
                    });
                    rview.setAdapter(madapter);
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public void GetSubCategory(final int id) {
        final ArrayList<Category> CategoryList = new ArrayList<>();
        String ts = Long.toString(System.currentTimeMillis() / 1000L);
        String key = Utils.SHA1(ts);
        APIHandler.getApiInterface().GetSubCatgeory(ts, key, new Callback<List<Category>>() {
            @Override
            public void success(List<Category> categories, Response response) {
                if (categories != null) {
                    for (Category item : categories) {
                        if (item.getParentID() == id || item.getParentID()==749) {
                            CategoryList.add(item);
                        }
                    }

                    Subcate=CategoryList.get(0).getName();

                    GetProductRecycler();
                    madapter1 = new MyAdapter1(CategoryList, context, new MyAdapter1.OnItemClickListener() {
                        @Override
                        public void onItemClick(Category item) {

                            Subcate=item.getName();
                            GetProductRecycler();
                            // Toaster.toast(Main2Activity.this,item.getName()+"  "+String.valueOf(item.getID()), Toast.LENGTH_LONG);
                        }
                    });
                    rview1.setAdapter(madapter1);
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }
    public void GetFirstSubCategory() {
        final ArrayList<Category> CategoryList = new ArrayList<>();
        String ts = Long.toString(System.currentTimeMillis() / 1000L);
        String key = Utils.SHA1(ts);
        APIHandler.getApiInterface().GetFirstSubCatgeory(ts, key,0, new Callback<List<Category>>() {
            @Override
            public void success(List<Category> categories, Response response) {
                if (categories != null) {
                    Category c=new Category();
                    c.setName("همه");
                    c.setID(749);
                    CategoryList.add(c);

                    CategoryList.addAll(categories);

                    madapter1 = new MyAdapter1(CategoryList, context, new MyAdapter1.OnItemClickListener() {
                        @Override
                        public void onItemClick(Category item) {

                            Subcate=item.getName();
                            // Toaster.toast(Main2Activity.this,item.getName()+"  "+String.valueOf(item.getID()), Toast.LENGTH_LONG);
                        }
                    });
                    rview1.setAdapter(madapter1);
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public void GetProductRecycler() {
        String ts = Long.toString(System.currentTimeMillis() / 1000L);
        String key = Utils.SHA1(ts);
        //Initialization
        ID = (int) getActivity().getIntent().getExtras().get("ID");
        Name = (String) getActivity().getIntent().getExtras().get("Name");
        if (Subcate == "همه") {
            Subcate = "";
        }
        currentPage = 1;
        APIHandler.getApiInterface().GetProductCatgeory(ts, key, Cate, Subcate, ID, new Callback<List<SimpleProduct>>() {
            @Override
            public void success(List<SimpleProduct> simpleProducts, Response response) {
                if (simpleProducts != null && simpleProducts.size() != 0) {
                    if (recyclerProductAdapter == null) {
                        contentHolder.setVisibility(View.VISIBLE);
                        recyclerProductAdapter = new RecyclerProductAdapter(context, simpleProducts, ID, Name);
                        recyclerView.setAdapter(recyclerProductAdapter);
                    } else {
                        recyclerProductAdapter.clear();
                        recyclerProductAdapter.addItems(simpleProducts);
                    }
                    loadingHolder.setVisibility(View.GONE);
                    view.findViewById(R.id.NothingFound).setVisibility(View.GONE);
                    currentPage++;
                    LoadingStatus = false;
                } else {
                    if (recyclerProductAdapter == null) {
                        contentHolder.setVisibility(View.GONE);
                        loadingHolder.setVisibility(View.GONE);
                        view.findViewById(R.id.NothingFound).setVisibility(View.VISIBLE);
                    } else {
                        recyclerProductAdapter.clear();
                        loadingHolder.setVisibility(View.GONE);
                        view.findViewById(R.id.NothingFound).setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (recyclerProductAdapter != null) {
                    loadingHolder.setVisibility(View.GONE);
                    view.findViewById(R.id.NothingFound).setVisibility(View.GONE);
                } else {
                    contentHolder.setVisibility(View.GONE);
                    view.findViewById(R.id.NothingFound).setVisibility(View.GONE);
                }
            }
        });
    }

    private void RemoveImage(String image) throws Exception {
        File filepath = context.getFilesDir();
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
        File filepath = context.getFilesDir();
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
                RealmConfiguration realmConfig = new RealmConfiguration.Builder(context).deleteRealmIfMigrationNeeded().build();
                realm = Realm.getInstance(realmConfig);
                realm.beginTransaction();
                DatabaseOrder databaseOrder=realm.createObject(DatabaseOrder.class);
                databaseOrder.setSupermarketID(ID);
                databaseOrder.setSupermarketName(Name);
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
                Toaster.toast(context, "با موفقیت ثبت شد", Toast.LENGTH_SHORT);
            } else {
                Toaster.toast(context, "خطا! : لطفا دوباره تلاش کنید", Toast.LENGTH_SHORT);
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
                RealmConfiguration realmConfig = new RealmConfiguration.Builder(context).deleteRealmIfMigrationNeeded().build();
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
                Toaster.toast(context, "باموفقیت حذف شد", Toast.LENGTH_SHORT);
            } else {
                Toaster.toast(context, "خطا! : لطفا دوباره تلاش کنید", Toast.LENGTH_SHORT);
            }
        }


    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}
