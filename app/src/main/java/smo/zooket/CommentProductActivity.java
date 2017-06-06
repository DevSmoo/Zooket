package smo.zooket;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import smo.zooket.API.APIHandler;
import smo.zooket.Adapter.CommentListAdapter;
import smo.zooket.Adapter.NavigationDrawerAdapter;
import smo.zooket.Models.Comment;
import smo.zooket.Models.DatabaseUser;
import smo.zooket.R;
import smo.zooket.Util.Toaster;
import smo.zooket.Util.Utils;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class CommentProductActivity extends BaseActivity {
    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    DrawerLayout mDrawerLayout;
    Realm realm;
    LinearLayout loadingHolder;
    ListView commentlist;
    CommentListAdapter commentAdapter;
    View LoadingFooter;
    int currentpage;
    private Toolbar toolbar;
    Activity context;
    String ID;
    String Name;
    boolean LoadingStatus = false;
    private int lastKnownFirst = 0;
    FloatingActionButton fab;

    DatabaseUser realmUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_product);


        commentlist = (ListView) findViewById(R.id.commentlist);
        loadingHolder = (LinearLayout) findViewById(R.id.loadingHolder);
        LoadingFooter = this.getLayoutInflater().inflate(R.layout.loading, null);
        commentlist.addFooterView(LoadingFooter);
        fab = (com.melnykov.fab.FloatingActionButton) findViewById(R.id.AddCommentBtn);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddComment(v);
            }
        });
        context = this;

        //Initialization
        currentpage = 1;
        commentlist.setSmoothScrollbarEnabled(true);
        ID = getIntent().getExtras().get("ID").toString();
        Name = (String) getIntent().getExtras().get("Name");

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
        mAdapter = new NavigationDrawerAdapter(realmUser);
        setUser(realmUser);
        mRecyclerView.setAdapter(mAdapter);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.requestLayout();

        //Handling Navigation Drawer On Click
        final GestureDetector mGestureDetector = new GestureDetector(CommentProductActivity.this, new GestureDetector.SimpleOnGestureListener() {
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
                            Intent myIntent = new Intent(CommentProductActivity.this, ProfileActivity.class);
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

        //Set Loading
        loadingHolder.findViewById(R.id.NoInternet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingHolder.findViewById(R.id.loadingmore).setVisibility(View.VISIBLE);
                loadingHolder.findViewById(R.id.NoInternet).setVisibility(View.GONE);
                FetchComments();
            }
        });


        LoadingFooter.findViewById(R.id.nointernet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoadingFooter.setVisibility(View.VISIBLE);
                LoadingFooter.findViewById(R.id.loadingmore).setVisibility(View.VISIBLE);
                LoadingFooter.findViewById(R.id.nointernet).setVisibility(View.GONE);
                LoadingFooter.findViewById(R.id.finished).setVisibility(View.GONE);
                FetchComments();
            }
        });

        //Set Endless Scrolling
        commentlist.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem > lastKnownFirst) {
                    fab.hide(true);
                } else if (firstVisibleItem < lastKnownFirst) {
                    fab.show(true);
                }
                lastKnownFirst = firstVisibleItem;
                int lastInScreen = firstVisibleItem + visibleItemCount;
                if ((lastInScreen == totalItemCount) && !(LoadingStatus)) {
                    if (commentAdapter == null) {
                        loadingHolder.findViewById(R.id.NoInternet).setVisibility(View.GONE);
                        loadingHolder.findViewById(R.id.loadingmore).setVisibility(View.VISIBLE);
                        loadingHolder.setVisibility(View.VISIBLE);
                    }
                    LoadingStatus = true;
                    FetchComments();
                }
            }
        });

        ((TextView) findViewById(R.id.Title)).setText("نظرات " + Name);
    }

    public void AddComment(final View view) {
        DatabaseUser realmUser1 = realm.where(DatabaseUser.class).findFirst();
        if (realmUser1 == null) {
            ShowLoginDialog();
        } else {
            final String email = realmUser1.getUsername();
            //Show Comment Dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final AlertDialog dialog = builder.create();
            Rect displayRectangle = new Rect();
            Window window = this.getWindow();
            window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
            final View layout = inflater.inflate(R.layout.dialog_comment, null);
            layout.setMinimumWidth((int) (displayRectangle.width() * 0.75f));
            layout.setMinimumHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            ViewGroup.LayoutParams params = layout.findViewById(R.id.DetailsHolder).getLayoutParams();
            params.width = (int) (displayRectangle.width() * 1f);
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            layout.findViewById(R.id.DetailsHolder).setLayoutParams(params);
            final TextView comment = (TextView) layout.findViewById(R.id.CommentText);
            layout.findViewById(R.id.Comment).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    layout.findViewById(R.id.Comment).setEnabled(false);
                    String Body = ((EditText) layout.findViewById(R.id.Body)).getText().toString();
                    if (Body.isEmpty()) {
                        Toaster.toast(context, "لطفا نظر خود را وارد کنید", Toast.LENGTH_SHORT);
                        layout.findViewById(R.id.Comment).setEnabled(true);
                    } else {
                        comment.setText("لطفا صبر کنید...");
                        layout.findViewById(R.id.CommentImage).setVisibility(View.GONE);
                        layout.findViewById(R.id.Commentprogressbar).setVisibility(View.VISIBLE);
                        String ts = Long.toString(System.currentTimeMillis() / 1000L);
                        String key = Utils.SHA1(ts + email);
                        APIHandler.getApiInterface().SendProComment(ts, key, email, Integer.parseInt(ID), Body, new Callback<Boolean>() {
                            @Override
                            public void success(Boolean answer, Response response) {
                                if (answer == true) {
                                    //Comment Successfully
                                    Toaster.toast(context, "نظر شما با موفقیت ثبت شد و پس از تایید نمایش داده می شود", Toast.LENGTH_SHORT);
                                    dialog.dismiss();
                                } else {
                                    //Failed
                                    layout.findViewById(R.id.Comment).setEnabled(true);
                                    Toaster.toast(context, "خطا در ثبت نظر !!!", Toast.LENGTH_SHORT);
                                    comment.setText("ثبت");
                                    layout.findViewById(R.id.CommentImage).setVisibility(View.VISIBLE);
                                    layout.findViewById(R.id.Commentprogressbar).setVisibility(View.GONE);
                                }

                            }

                            @Override
                            public void failure(RetrofitError error) {
                                layout.findViewById(R.id.Comment).setEnabled(true);
                                Toaster.toast(context, "خطا در برقراری ارتباط !!!", Toast.LENGTH_SHORT);
                                comment.setText("ثبت");
                                layout.findViewById(R.id.CommentImage).setVisibility(View.VISIBLE);
                                layout.findViewById(R.id.Commentprogressbar).setVisibility(View.GONE);
                            }
                        });

                    }
                }
            });
            dialog.setView(layout);
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
        }
    }
    private void FetchComments() {
        String ts = Long.toString(System.currentTimeMillis() / 1000L);
        String key = Utils.SHA1(ts);

        APIHandler.getApiInterface().GetProComment(ts, key, currentpage, Integer.parseInt(ID), new Callback<List<Comment>>() {
            @Override
            public void success(List<Comment> comments, Response response) {
                if (comments != null && comments.size() != 0) {
                    if (commentAdapter == null) {
                        commentlist.setVisibility(View.VISIBLE);
                        findViewById(R.id.DetailsHolder).setVisibility(View.VISIBLE);
                        commentAdapter = new CommentListAdapter(context, comments);
                        commentlist.setAdapter(commentAdapter);
                    } else {
                        commentAdapter.addItems(comments);
                    }
                    loadingHolder.setVisibility(View.GONE);
                    LoadingFooter.setVisibility(View.VISIBLE);
                    LoadingFooter.findViewById(R.id.nointernet).setVisibility(View.GONE);
                    LoadingFooter.findViewById(R.id.loadingmore).setVisibility(View.VISIBLE);
                    LoadingFooter.findViewById(R.id.finished).setVisibility(View.GONE);
                    commentlist.invalidateViews();
                    currentpage++;
                    LoadingStatus = false;
                } else {
                    if (commentAdapter == null) {
                        commentlist.setVisibility(View.GONE);
                        loadingHolder.setVisibility(View.GONE);
                        LoadingFooter.setVisibility(View.GONE);
                        findViewById(R.id.DetailsHolder).setVisibility(View.VISIBLE);
                        findViewById(R.id.NothingFound).setVisibility(View.VISIBLE);
                    } else {
                        loadingHolder.setVisibility(View.GONE);
                        LoadingFooter.setVisibility(View.VISIBLE);
                        LoadingFooter.findViewById(R.id.nointernet).setVisibility(View.GONE);
                        LoadingFooter.findViewById(R.id.loadingmore).setVisibility(View.GONE);
                        LoadingFooter.findViewById(R.id.finished).setVisibility(View.GONE);
                    }
                }
            }
            @Override
            public void failure(RetrofitError error) {
                if (commentAdapter != null) {
                    loadingHolder.setVisibility(View.GONE);
                    LoadingFooter.setVisibility(View.VISIBLE);
                    LoadingFooter.findViewById(R.id.nointernet).setVisibility(View.VISIBLE);
                    LoadingFooter.findViewById(R.id.loadingmore).setVisibility(View.GONE);
                    LoadingFooter.findViewById(R.id.finished).setVisibility(View.GONE);
                } else {
                    findViewById(R.id.DetailsHolder).setVisibility(View.GONE);
                    commentlist.setVisibility(View.GONE);
                    loadingHolder.setVisibility(View.VISIBLE);
                    loadingHolder.findViewById(R.id.NoInternet).setVisibility(View.VISIBLE);
                    loadingHolder.findViewById(R.id.loadingmore).setVisibility(View.GONE);
                }
                commentlist.invalidateViews();
            }
        });
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
        super.CallActivity(position);
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
        //Refresh Drawer
        DatabaseUser realmUser = realm.where(DatabaseUser.class).findFirst();
        mAdapter = new NavigationDrawerAdapter(realmUser);
        setUser(realmUser);
        mRecyclerView.setAdapter(mAdapter);
        mDrawerLayout.invalidate();
        super.onResume();
    }
}
