package smo.zooket;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
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
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Locale;

import smo.zooket.Util.SingleShotLocationProvider;
import smo.zooket.Util.Toaster;

/**
 * Created by Smo on 06/06/2017.
 */
public class FragmentGPSMap extends Fragment {
    View view;
    LinearLayout loadingHolder;
    LinearLayout contentHolder;
    LinearLayout GPSHolder;
    Location location;
    LocationManager locationManager;
    boolean CanLoad;
    private boolean LoadedOnce = false;
    Context context;

    //google map;
    MapView mapView;
    GoogleMap map;
    Bundle bundle;
    private GoogleApiClient client;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.item_gps_map_fragment, container, false);

        loadingHolder = (LinearLayout) view.findViewById(R.id.loadingHolder);
        GPSHolder = (LinearLayout) view.findViewById(R.id.GPSHolder);
        contentHolder = (LinearLayout) view.findViewById(R.id.contentHolder);

        context = getActivity();


        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        location = null;

        //Set Loading
        loadingHolder.findViewById(R.id.NoInternet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingHolder.findViewById(R.id.loadingmore).setVisibility(View.VISIBLE);
                loadingHolder.findViewById(R.id.NoInternet).setVisibility(View.GONE);
                //RefreshLocation();
            }
        });

        GPSHolder.findViewById(R.id.RefreshLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //RefreshLocation();
            }
        });

        //Set GPS Enabling Dialog
        GPSHolder.findViewById(R.id.ActivateLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        //ggoogle map
        bundle = savedInstanceState;
        mapView = (MapView) view.findViewById(R.id.mapView);
        client = new GoogleApiClient.Builder(context).addApi(AppIndex.API).build();

        try {
            mapView.onCreate(bundle);
            mapView.onResume();
            MapsInitializer.initialize(context);
            map = mapView.getMap();
            new MapAsync().execute();
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

        return view;
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

    @Override
    public void onResume() {
        if (!CanLoad && LoadedOnce) {
            LoadedOnce = true;
        }else{
            LoadedOnce = true;
        }
        super.onResume();
    }
    @Override
    public void setUserVisibleHint(boolean isFragmentVisible_) {
        super.setUserVisibleHint(true);
        if (this.isVisible()) {
            if (isFragmentVisible_ && !LoadedOnce) {
                LoadedOnce = true;
            }
        }
    }
}
