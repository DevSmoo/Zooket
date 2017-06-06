package smo.zooket.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.EditText;
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
import smo.zooket.ForgetPassActivity;
import smo.zooket.Models.Comment;
import smo.zooket.Models.DatabaseUser;
import smo.zooket.Models.User;
import smo.zooket.R;
import smo.zooket.Util.Toaster;
import smo.zooket.Util.Utils;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by Smo on 07/06/2017.
 */
public class SupermarketCommentFragment extends Fragment {

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

    View view;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_comment, container, false);

        commentlist = (ListView) view.findViewById(R.id.commentlist);
        loadingHolder = (LinearLayout) view.findViewById(R.id.loadingHolder);
        LoadingFooter = getActivity().getLayoutInflater().inflate(R.layout.loading, null);
        commentlist.addFooterView(LoadingFooter);
        fab = (com.melnykov.fab.FloatingActionButton) view.findViewById(R.id.AddCommentBtn);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddComment(v);
            }
        });
        context = getActivity();

        //Initialization
        currentpage = 1;
        commentlist.setSmoothScrollbarEnabled(true);
        ID = getActivity().getIntent().getExtras().get("ID").toString();
        Name = (String) getActivity().getIntent().getExtras().get("Name");

        //Attach DB
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(context).deleteRealmIfMigrationNeeded().build();
        realm = Realm.getInstance(realmConfig);

        //Font
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/BYekan.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );


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

        ((TextView) view.findViewById(R.id.Title)).setText("نظرات " + Name);

    return view;
    }

    public void AddComment(final View view) {
        DatabaseUser realmUser1 = realm.where(DatabaseUser.class).findFirst();
        if (realmUser1 == null) {
            //ShowLoginDialog();
            Toaster.toast(context,"برای وارد کردن نظر ابتدا باید وارد شوید!",Toast.LENGTH_SHORT);
        } else {
            final String email = realmUser1.getUsername();
            //Show Comment Dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            final AlertDialog dialog = builder.create();
            Rect displayRectangle = new Rect();
            Window window = context.getWindow();
            window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                        APIHandler.getApiInterface().SendComment(ts, key, email, Integer.parseInt(ID), Body, new Callback<Boolean>() {
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

        APIHandler.getApiInterface().GetComment(ts, key, currentpage, Integer.parseInt(ID), new Callback<List<Comment>>() {
            @Override
            public void success(List<Comment> comments, Response response) {
                if (comments != null && comments.size() != 0) {
                    if (commentAdapter == null) {
                        commentlist.setVisibility(View.VISIBLE);
                        view.findViewById(R.id.DetailsHolder).setVisibility(View.VISIBLE);
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
                        view.findViewById(R.id.DetailsHolder).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.NothingFound).setVisibility(View.VISIBLE);
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
                    view.findViewById(R.id.DetailsHolder).setVisibility(View.GONE);
                    commentlist.setVisibility(View.GONE);
                    loadingHolder.setVisibility(View.VISIBLE);
                    loadingHolder.findViewById(R.id.NoInternet).setVisibility(View.VISIBLE);
                    loadingHolder.findViewById(R.id.loadingmore).setVisibility(View.GONE);
                }
                commentlist.invalidateViews();
            }
        });
    }
    public void ShowLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final AlertDialog dialog = builder.create();
        Rect displayRectangle = new Rect();
        Window window = context.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.dialog_login, null);
        layout.setMinimumWidth((int) (displayRectangle.width() * 0.75f));
        layout.setMinimumHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        ViewGroup.LayoutParams params = layout.findViewById(R.id.DetailsHolder).getLayoutParams();
        params.width = (ViewGroup.LayoutParams.WRAP_CONTENT);
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        layout.findViewById(R.id.DetailsHolder).setLayoutParams(params);
        layout.findViewById(R.id.ForgetPass).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go To Forget Pass Activity
                Intent intent = new Intent(context, ForgetPassActivity.class);
                dialog.dismiss();
                //startActivity(intent);
                ShowForgetPassDialog();
            }
        });
        layout.findViewById(R.id.Register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go To Register Activity
                dialog.dismiss();
                ShowRegDialog();
            }
        });
        final TextView login = (TextView) layout.findViewById(R.id.loginText);
        layout.findViewById(R.id.Login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                layout.findViewById(R.id.Login).setEnabled(false);
                String email = ((EditText) layout.findViewById(R.id.Email)).getText().toString().trim().toLowerCase();
                String password = ((EditText) layout.findViewById(R.id.Password)).getText().toString().trim();

                if (email.isEmpty() || !isValidEmail(email)) {
                    Toaster.toast(context, "ایمیل وارد شده معتبر نمی باشد", Toast.LENGTH_SHORT);
                    layout.findViewById(R.id.Login).setEnabled(true);
                    ;
                } else if (password.isEmpty() || password.length() < 5) {
                    Toaster.toast(context, "رمز عبور نباید کمتر از 5 کارکتر باشد", Toast.LENGTH_SHORT);
                    layout.findViewById(R.id.Login).setEnabled(true);
                } else {
                    login.setText("لطفا صبر کنید...");
                    layout.findViewById(R.id.loginImage).setVisibility(View.GONE);
                    layout.findViewById(R.id.loginprogressbar).setVisibility(View.VISIBLE);
                    String ts = Long.toString(System.currentTimeMillis() / 1000L);
                    String key = Utils.SHA1(ts + email);

                    APIHandler.getApiInterface().UserLogin(ts, key, email, password, new Callback<User>() {
                        @Override
                        public void success(User user, Response response) {
                            if (user != null) {
                                //Login Successfull
                                RealmConfiguration realmConfig = new RealmConfiguration.Builder(context).deleteRealmIfMigrationNeeded().build();
                                Realm realm = Realm.getInstance(realmConfig);
                                realm.beginTransaction();

                                DatabaseUser dbUser = realm.createObject(DatabaseUser.class);
                                dbUser.setUsername(user.getUsername());
                                dbUser.setName(user.getName());
                                dbUser.setPassword(user.getPassword());
                                dbUser.setSupermarketID(user.getSupermarketID());
                                dbUser.setMobile(user.getMobile());
                                dbUser.setRole(user.getRole());
                                dbUser.setAddress(user.getAddress());

                                realm.commitTransaction();
                                //Refresh Drawer
                                Toaster.toast(context, user.getName() + " خوش آمدید", Toast.LENGTH_SHORT);
                                dialog.dismiss();
                            } else {
                                //login Failed
                                layout.findViewById(R.id.Login).setEnabled(true);
                                Toaster.toast(context, "اطلاعات وارد شده درست نمی باشد", Toast.LENGTH_SHORT);
                                login.setText("ورود");
                                layout.findViewById(R.id.loginImage).setVisibility(View.VISIBLE);
                                layout.findViewById(R.id.loginprogressbar).setVisibility(View.GONE);
                            }
                        }
                        @Override
                        public void failure(RetrofitError error) {
                            layout.findViewById(R.id.Login).setEnabled(true);
                            Toaster.toast(context, "خطا در برقراری ارتباط !!!", Toast.LENGTH_SHORT);
                            login.setText("ورود");
                            layout.findViewById(R.id.loginImage).setVisibility(View.VISIBLE);
                            layout.findViewById(R.id.loginprogressbar).setVisibility(View.GONE);
                        }
                    });
                }
            }
        });
        dialog.setView(layout);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    public boolean isValidEmail(String target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public void ShowRegDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final AlertDialog dialog = builder.create();
        Rect displayRectangle = new Rect();
        Window window = context.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.dialog_register, null);
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
                final String email = ((EditText) layout.findViewById(R.id.Email)).getText().toString().trim().toLowerCase();
                final String password = ((EditText) layout.findViewById(R.id.Password)).getText().toString().trim();
                final String passwordconfirm = ((EditText) layout.findViewById(R.id.PasswordConfirm)).getText().toString().trim();
                final String name = ((EditText) layout.findViewById(R.id.Name)).getText().toString().trim();
                final String mobile = ((EditText) layout.findViewById(R.id.Phone)).getText().toString().trim();
                if (email.isEmpty() || !isValidEmail(email)) {
                    Toaster.toast(context, "ایمیل وارد شده معتبر نمی باشد", Toast.LENGTH_SHORT);
                    layout.findViewById(R.id.sendholder).setEnabled(true);
                    ((TextView) layout.findViewById(R.id.sendbutton)).setText("ثبت نام");
                    layout.findViewById(R.id.sendprogressbar).setVisibility(View.GONE);
                    layout.findViewById(R.id.sendImage).setVisibility(View.VISIBLE);
                } else if (password.isEmpty() || password.length() < 5) {
                    Toaster.toast(context, "رمز عبور نباید کمتر از 5 کارکتر باشد", Toast.LENGTH_SHORT);
                    layout.findViewById(R.id.sendholder).setEnabled(true);
                    ((TextView) layout.findViewById(R.id.sendbutton)).setText("ثبت نام");
                    layout.findViewById(R.id.sendprogressbar).setVisibility(View.GONE);
                    layout.findViewById(R.id.sendImage).setVisibility(View.VISIBLE);
                } else if (passwordconfirm.isEmpty() || !passwordconfirm.equals(password)) {
                    Toaster.toast(context, "رمز عبور و تکرار آن برابر نمی باشند", Toast.LENGTH_SHORT);
                    layout.findViewById(R.id.sendholder).setEnabled(true);
                    ((TextView) layout.findViewById(R.id.sendbutton)).setText("ثبت نام");
                    layout.findViewById(R.id.sendprogressbar).setVisibility(View.GONE);
                    layout.findViewById(R.id.sendImage).setVisibility(View.VISIBLE);
                } else if (mobile.isEmpty()) {
                    Toaster.toast(context, "لطفا تلفن همراه را وارد کنید", Toast.LENGTH_SHORT);
                    layout.findViewById(R.id.sendholder).setEnabled(true);
                    ((TextView) layout.findViewById(R.id.sendbutton)).setText("ثبت نام");
                    layout.findViewById(R.id.sendprogressbar).setVisibility(View.GONE);
                    layout.findViewById(R.id.sendImage).setVisibility(View.VISIBLE);
                } else if (name.isEmpty()) {
                    Toaster.toast(context, "لطفا نام را وارد کنید", Toast.LENGTH_SHORT);
                    layout.findViewById(R.id.sendholder).setEnabled(true);
                    ((TextView) layout.findViewById(R.id.sendbutton)).setText("ثبت نام");
                    layout.findViewById(R.id.sendprogressbar).setVisibility(View.GONE);
                    layout.findViewById(R.id.sendImage).setVisibility(View.VISIBLE);
                } else {
                    String ts = Long.toString(System.currentTimeMillis() / 1000L);
                    String key = Utils.SHA1(ts + email);

                    final Realm realm;//Attach DB
                    RealmConfiguration realmConfig = new RealmConfiguration.Builder(context).deleteRealmIfMigrationNeeded().build();
                    realm = Realm.getInstance(realmConfig);

                    APIHandler.getApiInterface().UserRegister(ts, key, name, email, password, mobile, new Callback<Integer>() {
                        @Override
                        public void success(Integer answer, Response response) {
                            if (answer == 1) {
                                realm.beginTransaction();

                                DatabaseUser dbUser = realm.createObject(DatabaseUser.class);
                                dbUser.setUsername(email);
                                dbUser.setName(name);
                                dbUser.setPassword(password);
                                dbUser.setMobile(mobile);
                                dbUser.setSupermarketID(-1);
                                dbUser.setRole("User");
                                dbUser.setAddress("");

                                realm.commitTransaction();

                                Toaster.toast(context, "ثبت نام شما با موفقیت انجام شد", Toast.LENGTH_SHORT);
                                dialog.dismiss();

                            } else if (answer == 0) {
                                // User Exists
                                Toaster.toast(context, "ایمیل وارد شده تکراری می باشد", Toast.LENGTH_SHORT);
                                layout.findViewById(R.id.sendholder).setEnabled(true);
                                ((TextView) layout.findViewById(R.id.sendbutton)).setText("ثبت نام");
                                layout.findViewById(R.id.sendprogressbar).setVisibility(View.GONE);
                                layout.findViewById(R.id.sendImage).setVisibility(View.VISIBLE);
                            } else {
                                Toaster.toast(context, "خطا در برقراری ارتباط !!!", Toast.LENGTH_SHORT);
                                layout.findViewById(R.id.sendholder).setEnabled(true);
                                ((TextView) layout.findViewById(R.id.sendbutton)).setText("ثبت نام");
                                layout.findViewById(R.id.sendprogressbar).setVisibility(View.GONE);
                                layout.findViewById(R.id.sendImage).setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Toaster.toast(context, "خطا در برقراری ارتباط !!!", Toast.LENGTH_SHORT);
                            layout.findViewById(R.id.sendholder).setEnabled(true);
                            ((TextView) layout.findViewById(R.id.sendbutton)).setText("ثبت نام");
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

    public void ShowForgetPassDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final AlertDialog dialog = builder.create();
        Rect displayRectangle = new Rect();
        Window window = context.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.dialog_forget_pass, null);
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
                String email = ((EditText) layout.findViewById(R.id.Email)).getText().toString().trim().toLowerCase();
                if (email.isEmpty() || !isValidEmail(email)) {
                    Toaster.toast(context, "ایمیل وارد شده معتبر نمی باشد", Toast.LENGTH_SHORT);
                    layout.findViewById(R.id.sendholder).setEnabled(true);
                    ((TextView) layout.findViewById(R.id.sendbutton)).setText("ثبت");
                    layout.findViewById(R.id.sendprogressbar).setVisibility(View.GONE);
                    layout.findViewById(R.id.sendImage).setVisibility(View.VISIBLE);
                } else {
                    String ts = Long.toString(System.currentTimeMillis() / 1000L);
                    String key = Utils.SHA1(ts + email);

                    APIHandler.getApiInterface().UserForgetPass(ts, key, email, new Callback<Integer>() {
                        @Override
                        public void success(Integer answer, Response response) {
                            if (answer == 1) {
                                dialog.dismiss();
                                Toaster.toast(context,"لینک تغییر رمز به ایمیل شما ارسال شد!");
                            } else if (answer == 0) {
                                // User Doesnt Exists
                                Toaster.toast(context, "نام کاربری (ایمیل) وارد شده وجود ندارد", Toast.LENGTH_SHORT);
                                layout.findViewById(R.id.sendholder).setEnabled(true);
                                ((TextView) layout.findViewById(R.id.sendbutton)).setText("ثبت");
                                layout.findViewById(R.id.sendprogressbar).setVisibility(View.GONE);
                                layout.findViewById(R.id.sendImage).setVisibility(View.VISIBLE);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }
    @Override
    public void onResume() {
        super.onResume();
    }
}
