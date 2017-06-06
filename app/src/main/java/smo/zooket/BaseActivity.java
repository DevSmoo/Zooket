package smo.zooket;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import smo.zooket.API.APIHandler;
import smo.zooket.Models.DatabaseUser;
import smo.zooket.Models.User;
import smo.zooket.Util.Toaster;
import smo.zooket.Util.Utils;

/**
 * Created by Smo on 5/7/2017.
 */
public abstract class BaseActivity extends ActionBarActivity {

    DatabaseUser user;

    public void setUser(DatabaseUser u) {
        user = u;
    }

    public void ShowLoginDialog() {
        final Activity context = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.create();
        Rect displayRectangle = new Rect();
        Window window = this.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                hide_keyboard(layout);
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
                                UpdateUI();
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

    public String getRole1() {
        return user.getRole().toString();
    }

    private void hide_keyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.findViewById(R.id.Email).getWindowToken(), 0);
        // imm.hideSoftInputFromWindow(view.findViewById(R.id.Password).getWindowToken(), 0);
    }

    public boolean isValidEmail(String target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public void CallActivity(int position) {
        Intent myIntent;

        if (user != null) {
                switch (position) {
                    case 1:
                        myIntent = new Intent(this, MainActivity.class);
                        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        break;
                    case 2:
                        myIntent = new Intent(this, ProSupFavoritActivity.class);
                        break;
                    case 3:
                        myIntent = new Intent(this, AboutUsActivity.class);
                        break;
                    case 4:
                        myIntent = new Intent(this, ContactUsActivity.class);
                        break;
                    case 5:
                        myIntent = new Intent(this, SearchAdvanceActivity.class);
                        break;
                    case 6:
                        myIntent = new Intent(this, Order1Activity.class);
                        break;
                    case 7:
                        myIntent = new Intent(this, BuyActivity.class);
                        break;
                    default:
                        myIntent = new Intent(this, MainActivity.class);
                        break;
                }
                startActivity(myIntent);

        } else {
            switch (position) {
                case 1:
                    myIntent = new Intent(this, MainActivity.class);
                    myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    break;
                case 2:
                    myIntent = new Intent(this, ProSupFavoritActivity.class);
                    break;
                case 3:
                    myIntent = new Intent(this, AboutUsActivity.class);
                    break;
                case 4:
                    myIntent = new Intent(this, ContactUsActivity.class);
                    break;
                case 5:
                    myIntent = new Intent(this, SearchAdvanceActivity.class);
                    break;
                default:
                    myIntent = new Intent(this, MainActivity.class);
                    break;
            }
            startActivity(myIntent);
        }
    }

    public abstract void UpdateUI();

    public void ShowRegDialog() {
        final Activity context = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.create();
        Rect displayRectangle = new Rect();
        Window window = this.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                                UpdateUI();
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
        final Activity context = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.create();
        Rect displayRectangle = new Rect();
        Window window = this.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
}

