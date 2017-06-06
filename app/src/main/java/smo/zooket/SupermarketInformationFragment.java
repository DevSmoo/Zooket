package smo.zooket;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import smo.zooket.API.APIHandler;
import smo.zooket.Models.DatabasePayWay;
import smo.zooket.Models.DatabaseUser;
import smo.zooket.Models.SupermarketDetails;
import smo.zooket.Util.JalaliCalendar;
import smo.zooket.Util.PersianCalendar;
import smo.zooket.Util.Toaster;
import smo.zooket.Util.Utils;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by Smo on 07/06/2017.
 */
public class SupermarketInformationFragment extends Fragment {
    View view;
    int ID = 0;
    Activity context;
    Realm realm;

    SupermarketDetails superDetails;

    TextView today, work_time, call1, call2, address, pay_way, send_cost, min_order, description,satday, sunday, monday, tueday, wedday, turday, friday;
    //server attr
    String sat, sun, mon, tue, wed, tur, fri, location_longtitute;

    //google map;
    MapView mapView;
    GoogleMap map;
    Bundle bundle;
    com.google.android.gms.maps.model.LatLng LatLng;
    com.google.android.gms.maps.model.LatLng LatLngTemp;
    private GoogleApiClient client;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.supermarket_information_fragment, container, false);

        //ggoogle map
        bundle = savedInstanceState;
        LatLng = null;
        LatLngTemp = null;

        context = getActivity();


        //Attach DB
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(context).deleteRealmIfMigrationNeeded().build();
        realm = Realm.getInstance(realmConfig);

        //Font
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/BYekan.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
        //Initialization
        ID = (int) context.getIntent().getExtras().get("ID");

        today = (TextView) view.findViewById(R.id.today);//current date
        today.setText("ساعت کاری امروز : " + PersianCalendar.getCurrentShamsidate());
        work_time = (TextView) view.findViewById(R.id.todat_job_time);
        call1 = (TextView) view.findViewById(R.id.call1);
        call2 = (TextView) view.findViewById(R.id.call2);
        address = (TextView) view.findViewById(R.id.address);
        pay_way = (TextView) view.findViewById(R.id.pay_way);
        send_cost = (TextView) view.findViewById(R.id.send_cost);
        min_order = (TextView) view.findViewById(R.id.min_order);
        description = (TextView) view.findViewById(R.id.description);

        satday=(TextView) view.findViewById(R.id.sat);
        sunday=(TextView) view.findViewById(R.id.sun);
        monday=(TextView) view.findViewById(R.id.mon);
        tueday=(TextView) view.findViewById(R.id.tue);
        wedday=(TextView) view.findViewById(R.id.wed);
        turday=(TextView) view.findViewById(R.id.tur);
        friday=(TextView) view.findViewById(R.id.fri);

        //server attr
        sat = "";
        sun = "";
        mon = "";
        tue = "";
        wed = "";
        tur = "";
        fri = "";
        location_longtitute = "";

        GetSuperDetails();
        mapView = (MapView) view.findViewById(R.id.mapView);

        client = new GoogleApiClient.Builder(context).addApi(AppIndex.API).build();
        return view;
    }

    class MapAsync extends AsyncTask<Void, Integer, Address> {
        protected void onPreExecute() {
        }

        protected Address doInBackground(Void... arg) {
            String province = getResources().getStringArray(R.array.provinces2)[7];
            Address location = getLatlngOfProvince(province);

            return location;
        }

        protected void onPostExecute(final Address location) {
            if (location != null) {
                mapView.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        map=googleMap;
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 10));
                    }
                });
            }
        }
    }

    public void initMap() {
        try {
            mapView.onCreate(bundle);
            mapView.onResume();
            MapsInitializer.initialize(context);
            map = mapView.getMap();
            if (LatLng == null) {
                new MapAsync().execute();
            } else {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(LatLng);
                map.clear();
                map.addMarker(markerOptions);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng, 16));
            }
        } catch (Exception ex) {
            switch (GooglePlayServicesUtil.isGooglePlayServicesAvailable(context)) {
                case ConnectionResult.SERVICE_MISSING:
                    Toaster.toast(context, "خطا در بارگزاری نقشه ! گوگل پلی سرویس موجود نیست !!!", Toast.LENGTH_SHORT);
                    break;
                case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                    Toaster.toast(context, "خطا در بارگزاری نقشه ! گوگل پلی سرویس آپدیت نمی باشد !!!", Toast.LENGTH_SHORT);
                    break;
                default:
                    Toaster.toast(context, "خطا در بارگزاری نقشه !", Toast.LENGTH_SHORT);
            }
        }
    }

    public Address getLatlngOfProvince(String province) {
        Locale EN = new Locale("en");
        Geocoder coder = new Geocoder(context, EN);
        List<Address> address;
        try {
            address = coder.getFromLocationName(province, 5);
            Address loc = address.get(0);
            if (loc != null) {
                return loc;
            }
            return null;
        } catch (Exception e) {
            return null;
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
                        if (item.getAddress() != null && item.getAddress() != "") {
                            if (item.getProvince() != null && item.getProvince() != "") {
                                if (item.getPostalCode() != null && item.getPostalCode() != "") {
                                    address.setText(item.getProvince() + " , " + item.getAddress() + " , " + "کدپستی : " + item.getPostalCode());
                                } else {
                                    address.setText(item.getProvince() + " , " + item.getAddress());
                                }
                            } else {
                                if (item.getPostalCode() != null && item.getPostalCode() != "") {
                                    address.setText(item.getAddress() + " , " + "کدپستی : " + item.getPostalCode());
                                } else {
                                    address.setText(item.getAddress());
                                }
                            }
//copy address to clipboard
                            address.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText("Address", address.getText().toString());
                                    clipboard.setPrimaryClip(clip);
                                    Toaster.toast(context, "آدرس در حافظه کپی شد", Toast.LENGTH_SHORT);
                                }
                            });
                        }
                        if (item.getTel() != null && item.getTel() != "") {
                            call1.setText(item.getTel());
                            call1.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                                    callIntent.setData(Uri.parse("tel:" + call1.getText().toString()));
                                    callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(callIntent);
                                }
                            });

                        }
                        if (item.getMobile() != null && item.getMobile() != "") {
                            call2.setText(item.getMobile());
                            call2.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                                    callIntent.setData(Uri.parse("tel:" + call2.getText().toString()));
                                    callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(callIntent);
                                }
                            });

                        }
                        if (item.getDescription() != null && item.getDescription() != "") {
                            description.setText(item.getDescription());

                        }
                        if (item.getSatTime() != null && item.getSatTime() != "") {
                            satday.setText(item.getSatTime());
                            sat=item.getSatTime();

                        }
                        if (item.getSunTime() != null && item.getSunTime() != "") {
                            sunday.setText(item.getSunTime());
                            sun=item.getSunTime();
                        }
                        if (item.getMonTime() != null && item.getMonTime() != "") {
                            monday.setText(item.getMonTime());
                            mon=item.getMonTime();
                        }
                        if (item.getTueTime() != null && item.getTueTime() != "") {
                            tueday.setText(item.getTueTime());
                            tue=item.getTueTime();
                        }
                        if (item.getWedTime() != null && item.getWedTime() != "") {
                            wedday.setText(item.getWedTime());
                            wed=item.getWedTime();
                        }
                        if (item.getTurTime() != null && item.getTurTime() != "") {
                            turday.setText(item.getTurTime());
                            tur=item.getTurTime();
                        }
                        if (item.getFriTime() != null && item.getFriTime() != "") {
                            friday.setText(item.getFriTime());
                            fri=item.getFriTime();
                        }
                        if (item.getLatituteLongtitute() != null && item.getLatituteLongtitute() != "") {
                            location_longtitute = item.getLatituteLongtitute();
                            String[] temp = location_longtitute.split(",");
                            String X = temp[0].replace('(', ' ');
                            String Y = temp[1].replace(')', ' ');
                            LatLng = new LatLng(Double.parseDouble(X.trim()), Double.parseDouble(Y.trim()));

                        }
                        if (item.getDeliveryCost() != null && item.getDeliveryCost() != "") {
                            send_cost.setText(item.getDeliveryCost()+" تومان");
                        }
                        if (item.getMinOrderCost() != null && item.getMinOrderCost() != "") {
                            min_order.setText(item.getMinOrderCost()+" تومان");
                        }
                        String PayCash = "";
                        if (item.getPayCash() != null && item.getPayCash() != false) {
                            PayCash += "پرداخت نقدی";
                        }
                        if (item.getPayElectronic() != null && item.getPayElectronic() != false) {
                            if (PayCash == "") {
                                PayCash += "پرداخت الکترونیکی";
                            } else {
                                PayCash += " -پرداخت الکترونیکی";
                            }
                        }
                        if (item.getPayPos() != null && item.getPayPos() != false) {
                            if (PayCash == "") {
                                PayCash += "پرداخت با دستگاه پوز";
                            } else {
                                PayCash += " -پرداخت با دستگاه پوز";
                            }
                        }
                        pay_way.setText(PayCash);

                        Boolean a= superDetails.getPayCash().booleanValue();
                        Boolean b=superDetails.getPayElectronic().booleanValue();
                        Boolean c=superDetails.getPayPos().booleanValue();

                        realm.beginTransaction();
                        DatabasePayWay results = realm.where(DatabasePayWay.class).equalTo("SuperID", item.getID()).findFirst();
                        if (results==null){
                            DatabasePayWay db=realm.createObject(DatabasePayWay.class);
                            db.setSuperID(item.getID());
                            db.setPayCash(a);
                            db.setPayElec(b);
                            db.setPayPos(c);
                        }else{
                            results.setSuperID(item.getID());
                            results.setPayCash(a);
                            results.setPayElec(b);
                            results.setPayPos(c);
                        }
                        realm.commitTransaction();
                        realm.close();

                        work_time.setText(TodayWorkTime());

                        //  if (IsOpen()) {
                        //      status.setText("فروشگاه باز است.از خرید خود لذت ببرید!");
                        //      status.setTextColor(Color.GREEN);
                        //   } else {
                        //       status.setText("فروشگاه تعطیل است .لطفا در زمان مقرر مراجعه فرمایید!");
                        //       status.setTextColor(Color.RED);
                        //    }
                        //checkFromDatabase
                      //  CheckedFav();
initMap();
                     //   detailsHolder.setVisibility(View.VISIBLE);
                       // loadingHolder.setVisibility(View.GONE);
                    } else {
                      //  loadingHolder.setVisibility(View.GONE);
                       // detailsHolder.setVisibility(View.GONE);
                       // findViewById(R.id.NotFound).setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                   // Loading_Load.setVisibility(View.GONE);
                   // Loading_Failed.setVisibility(View.VISIBLE);
                }
            });
        }
    }


    public String TodayWorkTime() {
        //DateRes=getIntent().getExtras().get("Date").toString();
        Date d = PersianCalendar.getMiladi(PersianCalendar.getCurrentShamsidate());
        String day = JalaliCalendar.getDayOfWeek(d);
        if (day == "شنبه") {
            return sat;
        } else if (day == "یکشنبه") {
            return sun;
        } else if (day == "دوشنبه") {
            return mon;
        } else if (day == "سه شنبه") {
            return tue;
        } else if (day == "چهارشنبه") {
            return wed;
        } else if (day == "پنج شنبه") {
            return tur;
        } else if (day == "جمعه") {
            return fri;
        } else {
            return "";
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
