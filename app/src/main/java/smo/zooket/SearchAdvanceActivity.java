package smo.zooket;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.common.api.Api;
import com.melnykov.fab.FloatingActionButton;

import android.graphics.Rect;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appyvet.rangebar.RangeBar;

import org.w3c.dom.Attr;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import smo.zooket.API.APIHandler;
import smo.zooket.Adapter.ExpandableListAdapter1;
import smo.zooket.Adapter.NavigationDrawerAdapter;
import smo.zooket.Adapter.RecyclerProductAdapter;
import smo.zooket.Adapter.SearchProductAdapter;
import smo.zooket.Models.Attributes;
import smo.zooket.Models.Category;
import smo.zooket.Models.DatabaseUser;
import smo.zooket.Models.SearchProduct;
import smo.zooket.Models.SimpleProduct;
import smo.zooket.Util.Toaster;
import smo.zooket.Util.Utils;
import smo.zooket.testC.MyAdapter;
import smo.zooket.testC.MyAdapter1;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SearchAdvanceActivity extends BaseActivity {
FloatingActionButton RefreshFBN;
    EditText SuperName,ProductName,Category,Attribute;
    CheckBox Available,NotAvailable;
    RangeBar rangeBar;
    LinearLayout NotFound;
    LinearLayout DetailsHolder,ContainerSearch;
    RecyclerView ItemSearch;
    SearchProductAdapter recyclerProductAdapter;
    TextView StartPrice,EndPrice;
    List<Attributes> attributesList;
    List<smo.zooket.Models.Category> categories;
    List<smo.zooket.Models.Category> subcategories;

    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    DrawerLayout mDrawerLayout;
    Realm realm;
    private Toolbar toolbar;
    Activity context;
    DatabaseUser realmUser;



    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    List<String> Attr=new ArrayList<>();
    List<String> Cate=new ArrayList<>();

    Boolean IsAvailable=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_advance);


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
        realmUser = realm.where(DatabaseUser.class).findFirst();
        setUser(realmUser);
        mAdapter = new NavigationDrawerAdapter(realmUser);
        mRecyclerView.setAdapter(mAdapter);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.requestLayout();

        //Handling Navigation Drawer On Click
        final GestureDetector mGestureDetector = new GestureDetector(SearchAdvanceActivity.this, new GestureDetector.SimpleOnGestureListener() {
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
                            Intent myIntent = new Intent(SearchAdvanceActivity.this, ProfileActivity.class);
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

        RefreshFBN=(FloatingActionButton)findViewById(R.id.Refresh);

        SuperName=(EditText)findViewById(R.id.SuperName);
        ProductName= (EditText)findViewById(R.id.ProductName);
        Category=(EditText)findViewById(R.id.Category);
        Attribute=(EditText)findViewById(R.id.Attribute);

        Available=(CheckBox)findViewById(R.id.Available);
        NotAvailable=(CheckBox)findViewById(R.id.NotAvailable);

        rangeBar=(RangeBar)findViewById(R.id.rangebar);

        NotFound=(LinearLayout)findViewById(R.id.NothingFound);

        DetailsHolder=(LinearLayout) findViewById(R.id.DetailsHolder);
        ItemSearch=(RecyclerView)findViewById(R.id.ItemSearch);
        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        ItemSearch.setLayoutManager(manager);
        ContainerSearch=(LinearLayout) findViewById(R.id.ContainerSearch);

        StartPrice=(TextView)findViewById(R.id.StartPrice);
        EndPrice=(TextView)findViewById(R.id.EndPrice);
        StartPrice.setText("شروع قیمت از : 0 تومان");
        EndPrice.setText("تا قیمت : 20000 تومان");

        attributesList=new ArrayList<>();
        categories=new ArrayList<>();
        subcategories=new ArrayList<>();

        GetAttributes();
        GetCategory();
        GetSubCategory();

        Attribute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AttributDialog();
            }
        });
        Category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CategoryDialog();
            }
        });

        rangeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
           public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex,
                                            int rightPinIndex,
                                            String leftPinValue, String rightPinValue) {
              StartPrice.setText("شروع قیمت از : "+leftPinValue+"00"+" تومان");
              EndPrice.setText("تا قیمت : "+rightPinValue+"00"+" تومان");
           }
         });
        Available.setChecked(true);
        NotAvailable.setChecked(false);
        IsAvailable=true;
        Available.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Available.isChecked()){
                    NotAvailable.setChecked(false);
                    IsAvailable=true;
                }
            }
        });
        NotAvailable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NotAvailable.isChecked()){
                    Available.setChecked(false);
                    IsAvailable=false;
                }
            }
        });
        RefreshFBN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DetailsHolder.setVisibility(View.VISIBLE);
                ContainerSearch.setVisibility(View.GONE);
                RefreshFBN.setVisibility(View.GONE);
            }
        });
        ((LinearLayout)findViewById(R.id.sendholder)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ts = Long.toString(System.currentTimeMillis() / 1000L);
                String key = Utils.SHA1(ts);

                findViewById(R.id.sendholder).setEnabled(false);
                ((TextView) findViewById(R.id.sendbutton)).setText("لطفا صبر کنید...");
                findViewById(R.id.sendImage).setVisibility(View.GONE);
                findViewById(R.id.sendprogressbar).setVisibility(View.VISIBLE);

                int startP=Integer.parseInt(rangeBar.getLeftPinValue())*100;
                int endP=Integer.parseInt(rangeBar.getRightPinValue())*100;

                APIHandler.getApiInterface().SearchProduct(ts, key, Attribute.getText().toString(),
                        String.valueOf(startP), String.valueOf(endP), ProductName.getText().toString(),
                        Category.getText().toString(), SuperName.getText().toString(), IsAvailable, new Callback<List<SearchProduct>>() {
                            @Override
                            public void success(List<SearchProduct> simpleProducts, Response response) {
                                if (simpleProducts!=null&& simpleProducts.size() != 0){
                                    if (recyclerProductAdapter == null) {
                                        recyclerProductAdapter = new SearchProductAdapter(context, simpleProducts);
                                        ItemSearch.setAdapter(recyclerProductAdapter);
                                    } else {
                                        recyclerProductAdapter.addItems(simpleProducts);
                                    }

                                    findViewById(R.id.sendholder).setEnabled(true);
                                    ((TextView)findViewById(R.id.sendbutton)).setText("جستجو");
                                    findViewById(R.id.sendImage).setVisibility(View.VISIBLE);
                                    findViewById(R.id.sendprogressbar).setVisibility(View.GONE);
                                    DetailsHolder.setVisibility(View.GONE);
                                    ContainerSearch.setVisibility(View.VISIBLE);
                                    RefreshFBN.setVisibility(View.VISIBLE);

                                }else{
                                    findViewById(R.id.sendholder).setEnabled(true);
                                    ((TextView)findViewById(R.id.sendbutton)).setText("جستجو");
                                    findViewById(R.id.sendImage).setVisibility(View.VISIBLE);
                                    findViewById(R.id.sendprogressbar).setVisibility(View.GONE);
                                    Toaster.toast(context,"موردی یافت نشد", Toast.LENGTH_SHORT);
                                }
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                findViewById(R.id.sendholder).setEnabled(true);
                                ((TextView)findViewById(R.id.sendbutton)).setText("جستجو");
                                findViewById(R.id.sendImage).setVisibility(View.VISIBLE);
                                findViewById(R.id.sendprogressbar).setVisibility(View.GONE);
                                Toaster.toast(context,"خطا دربرقراری ارتباط!", Toast.LENGTH_SHORT);
                            }
                        });
            }
        });
    }
    public void GetCategory() {
        String ts = Long.toString(System.currentTimeMillis() / 1000L);
        String key = Utils.SHA1(ts);
        APIHandler.getApiInterface().GetCatgeory(ts, key, new Callback<List<Category>>() {
            @Override
            public void success(List<Category> items, Response response) {
                if (items != null) {
                     categories=items;
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public void GetSubCategory() {
        String ts = Long.toString(System.currentTimeMillis() / 1000L);
        String key = Utils.SHA1(ts);
        APIHandler.getApiInterface().GetSubCatgeory(ts, key, new Callback<List<Category>>() {
            @Override
            public void success(List<Category> item, Response response) {
                if (item != null) {
                    subcategories=item;
                    subcategories.remove(0);
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public void GetAttributes(){
    String ts = Long.toString(System.currentTimeMillis() / 1000L);
    String key = Utils.SHA1(ts);
    APIHandler.getApiInterface().GetAttr(ts, key, new Callback<List<Attributes>>() {
        @Override
        public void success(List<Attributes> attributes, Response response) {
            if (attributes!=null){
                attributesList=attributes;
            }
        }

        @Override
        public void failure(RetrofitError error) {

        }
    });
}

    public void AttributDialog(){
        final Activity context = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.create();
        Rect displayRectangle = new Rect();
        Window window = this.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.dialog_attribute_list, null);
        layout.setMinimumWidth((int) (displayRectangle.width() * 0.75f));
        layout.setMinimumHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        ViewGroup.LayoutParams params = layout.findViewById(R.id.DetailsHolder).getLayoutParams();
        params.width = (ViewGroup.LayoutParams.WRAP_CONTENT);
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        layout.findViewById(R.id.DetailsHolder).setLayoutParams(params);

        ExpandableListView expListView=(ExpandableListView)layout.findViewById(R.id.lvExp);
        expListView.setMinimumHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        final TextView result=(TextView)layout.findViewById(R.id.AttrSelected);
        result.setText(Attribute.getText().toString());
        //create
        prepareListData();
        ExpandableListAdapter1 listAdapter = new ExpandableListAdapter1(this, listDataHeader, listDataChild);
        // setting list adapter
        expListView.setAdapter(listAdapter);
        expListView.expandGroup(0);
        setListViewHeight(expListView, 0,listAdapter);
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                String Temp;
                Temp=listDataHeader.get(groupPosition)+ ":" + listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition);

                if (Attr.contains(Temp)){
                    Attr.remove(Temp);
                }else{
                    Attr.add(Temp);
                }
                String res = "";
                for (String str:Attr){
                    res+=str;
                    res+="-";
                }
                if (res.length()>0){
                    res = res.substring(0, res.length()-1);
                }

                result.setText(res);
                Attribute.setText(res);
                return true;
            }
        });
        ((LinearLayout)layout.findViewById(R.id.sendholder)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        //
        dialog.setView(layout);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();


    }

    public void CategoryDialog(){
        final Activity context = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.create();
        Rect displayRectangle = new Rect();
        Window window = this.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.dialog_attribute_list, null);
        layout.setMinimumWidth((int) (displayRectangle.width() * 0.75f));
        layout.setMinimumHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        ViewGroup.LayoutParams params = layout.findViewById(R.id.DetailsHolder).getLayoutParams();
        params.width = (ViewGroup.LayoutParams.WRAP_CONTENT);
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        layout.findViewById(R.id.DetailsHolder).setLayoutParams(params);

        ExpandableListView expListView=(ExpandableListView)layout.findViewById(R.id.lvExp);
        expListView.setMinimumHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        final TextView result=(TextView)layout.findViewById(R.id.AttrSelected);
        result.setText(Category.getText().toString());
        //create
        prepareListData1();
        ExpandableListAdapter1 listAdapter = new ExpandableListAdapter1(this, listDataHeader, listDataChild);
        // setting list adapter
        expListView.setAdapter(listAdapter);
        expListView.expandGroup(0);
        setListViewHeight(expListView, 0,listAdapter);
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                String Temp=listDataHeader.get(groupPosition)+ "-" + listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition);
                result.setText(Temp);
                Category.setText(Temp);
                dialog.dismiss();
                return true;
            }
        });
        ((LinearLayout)layout.findViewById(R.id.sendholder)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        //
        dialog.setView(layout);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    private void setListViewHeight(ExpandableListView listView,int group,ExpandableListAdapter1 listAdapter) {
        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(),
                View.MeasureSpec.EXACTLY);
        for (int i = 0; i < listAdapter.getGroupCount(); i++) {
            View groupItem = listAdapter.getGroupView(i, false, null, listView);
            groupItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);

            totalHeight += groupItem.getMeasuredHeight();

            if (((listView.isGroupExpanded(i)) && (i != group))
                    || ((!listView.isGroupExpanded(i)) && (i == group))) {
                for (int j = 0; j < listAdapter.getChildrenCount(i); j++) {
                    View listItem = listAdapter.getChildView(i, j, false, null,
                            listView);
                    listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);

                    totalHeight += listItem.getMeasuredHeight();

                }
            }
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        int height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getGroupCount() - 1));
        if (height < 10)
            height = 200;
        params.height = height;
        listView.setLayoutParams(params);
        listView.requestLayout();

    }
    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        List<String> Temp=new ArrayList<>();
        // Adding header data
        for(Attributes a:attributesList){
            listDataHeader.add(a.getAttrName());
        }

        for (int i=0;i<attributesList.size();i++){
            int SubListSize=attributesList.get(i).getListSubAttr().size();
            Temp = new ArrayList<String>();
            for (int j=0;j<SubListSize;j++){
                // Adding child data
                Temp.add(attributesList.get(i).getListSubAttr().get(j).getSubAttrName());
            }
            listDataChild.put(listDataHeader.get(i), Temp); // Header, Child data
        }
    }

    private void prepareListData1() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        List<String> Temp=new ArrayList<>();
        // Adding header data
        for(Category a:categories){
            listDataHeader.add(a.getName());
        }

        for (int i=0;i<categories.size();i++){

            int SubCateSize=subcategories.size();
            Temp = new ArrayList<String>();
            for (int j=0;j<SubCateSize;j++){
                // Adding child data
                if (categories.get(i).getID()==subcategories.get(j).getParentID()) {
                    Temp.add(subcategories.get(j).getName());
                }
            }
            listDataChild.put(listDataHeader.get(i), Temp); // Header, Child data
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
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
        if (position != 5) {
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
