package smo.zooket;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import java.util.List;

import in.srain.cube.views.GridViewWithHeaderAndFooter;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import smo.zooket.API.APIHandler;
import smo.zooket.Adapter.GridItemAdapter;
import smo.zooket.Adapter.SwipeLayout;
import smo.zooket.Models.GpsSupermarket;
import smo.zooket.Util.SingleShotLocationProvider;
import smo.zooket.Util.Utils;

/**
 * Created by Smo on 06/06/2017.
 */
public class FragmentListGPSSupermarket extends Fragment {
    View view;
    GridViewWithHeaderAndFooter grid;
    GridItemAdapter gridAdapter;

    boolean LoadingStatus = false;
    LinearLayout loadingHolder;
    LinearLayout contentHolder;
    LinearLayout GPSHolder;
    View LoadingFooter;
    SwipeLayout swipeLayout;
    LocationManager locationManager;
    int currentPage;
    boolean CanLoad;
    Location location;

    private boolean LoadedOnce = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.list_gps_fragment, container, false);
        grid = (GridViewWithHeaderAndFooter) view.findViewById(R.id.itemgrid);
        loadingHolder = (LinearLayout) view.findViewById(R.id.loadingHolder);
        GPSHolder = (LinearLayout) view.findViewById(R.id.GPSHolder);
        contentHolder = (LinearLayout) view.findViewById(R.id.contentHolder);
        LoadingFooter = inflater.inflate(R.layout.loading, null);
        grid.addFooterView(LoadingFooter);

        //Initialization
        grid.setSmoothScrollbarEnabled(true);
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        currentPage = 1;
        location = null;
        CanLoad = false;

        //Pull To Refresh
        swipeLayout = (SwipeLayout) view.findViewById(R.id.pulltorefresh_container);
        swipeLayout.setColorSchemeResources(R.color.ColorPrimary, R.color.ColorPrimaryDark, R.color.ScrollColor);
        swipeLayout.requestDisallowInterceptTouchEvent(true);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                LoadingStatus = true;
                RefreshItems();
            }
        });
        swipeLayout.setOnChildScrollUpListener(new SwipeLayout.OnChildScrollUpListener() {
            @Override
            public boolean canChildScrollUp() {
                return grid.getFirstVisiblePosition() > 0 ||
                        grid.getChildAt(0) == null ||
                        grid.getChildAt(0).getTop() < 0;
            }
        });

        //Set Loading
        loadingHolder.findViewById(R.id.NoInternet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingHolder.findViewById(R.id.loadingmore).setVisibility(View.VISIBLE);
                loadingHolder.findViewById(R.id.NoInternet).setVisibility(View.GONE);
                RefreshLocation();
            }
        });

        LoadingFooter.findViewById(R.id.nointernet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoadingFooter.setVisibility(View.VISIBLE);
                LoadingFooter.findViewById(R.id.loadingmore).setVisibility(View.VISIBLE);
                LoadingFooter.findViewById(R.id.nointernet).setVisibility(View.GONE);
                LoadingFooter.findViewById(R.id.finished).setVisibility(View.GONE);
                FetchItems();
            }
        });

        GPSHolder.findViewById(R.id.RefreshLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RefreshLocation();
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

        //Set Endless Scrolling
        grid.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int lastInScreen = firstVisibleItem + visibleItemCount;
                if ((lastInScreen == totalItemCount) && !(LoadingStatus) && CanLoad) {
                    if (gridAdapter == null) {
                        loadingHolder.findViewById(R.id.NoInternet).setVisibility(View.GONE);
                        loadingHolder.findViewById(R.id.loadingmore).setVisibility(View.VISIBLE);
                        loadingHolder.setVisibility(View.VISIBLE);
                    }
                    LoadingStatus = true;
                    FetchItems();
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

        return view;
    }

    private AdapterView.OnItemClickListener ItemSelected = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            GpsSupermarket item = (GpsSupermarket) grid.getAdapter().getItem(position);
            if (item != null) {
                Intent MyIntent = new Intent(getActivity(), SupermarketActivity.class);
                MyIntent.putExtra("ID", item.getID());
                MyIntent.putExtra("Name", item.getName());
                startActivity(MyIntent);
            }
        }
    };
    private boolean IsLocationEnable() {
//        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER) && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void RefreshLocation() {
        if (IsLocationEnable()) {
            if (location != null) {
                loadingHolder.setVisibility(View.VISIBLE);
                contentHolder.setVisibility(View.GONE);
                loadingHolder.findViewById(R.id.NoInternet).setVisibility(View.GONE);
                loadingHolder.findViewById(R.id.loadingmore).setVisibility(View.VISIBLE);
                GPSHolder.setVisibility(View.GONE);
                CanLoad = true;
                if (!LoadingStatus) {
                    LoadingStatus = true;
                    FetchItems();
                }
            } else {
                if (isOnline()) {
                    SingleShotLocationProvider.requestSingleUpdate(getActivity(),
                            new SingleShotLocationProvider.LocationCallback() {
                                @Override
                                public void onNewLocationAvailable(Location loc) {
                                    location = loc;
                                    RefreshLocation();
                                }
                            });
                    CanLoad = false;
                    loadingHolder.setVisibility(View.VISIBLE);
                    loadingHolder.findViewById(R.id.NoInternet).setVisibility(View.GONE);
                    loadingHolder.findViewById(R.id.loadingmore).setVisibility(View.VISIBLE);
                    GPSHolder.setVisibility(View.GONE);
                } else {
                    SingleShotLocationProvider.requestSingleUpdate(getActivity(),
                            new SingleShotLocationProvider.LocationCallback() {
                                @Override
                                public void onNewLocationAvailable(Location loc) {
                                    location = loc;
                                    RefreshLocation();
                                }
                            });
                    CanLoad = false;
                    loadingHolder.setVisibility(View.VISIBLE);
                    loadingHolder.findViewById(R.id.NoInternet).setVisibility(View.VISIBLE);
                    loadingHolder.findViewById(R.id.loadingmore).setVisibility(View.GONE);
                    GPSHolder.setVisibility(View.GONE);
                }
            }
        } else {
            CanLoad = false;
            loadingHolder.setVisibility(View.GONE);
            contentHolder.setVisibility(View.GONE);
            GPSHolder.setVisibility(View.VISIBLE);
        }
    }

    private void FetchItems() {
        if (CanLoad) {
            String ts = Long.toString(System.currentTimeMillis() / 1000L);
            String key = Utils.SHA1(ts);
            APIHandler.getApiInterface().GetNearestSupermarket(ts, key, currentPage, location.getLatitude(), location.getLongitude(), new Callback<List<GpsSupermarket>>() {
                @Override
                public void success(List<GpsSupermarket> items, Response response) {
                    if (items != null && items.size() != 0) {
                        if (gridAdapter == null) {
                            contentHolder.setVisibility(View.VISIBLE);
                            gridAdapter = new GridItemAdapter(view.getContext(), items);
                            grid.setAdapter(gridAdapter);
                            grid.setOnItemClickListener(ItemSelected);
                        } else {
                            gridAdapter.addItems(items);
                        }
                        loadingHolder.setVisibility(View.GONE);
                        LoadingFooter.setVisibility(View.VISIBLE);
                        LoadingFooter.findViewById(R.id.nointernet).setVisibility(View.GONE);
                        LoadingFooter.findViewById(R.id.loadingmore).setVisibility(View.VISIBLE);
                        LoadingFooter.findViewById(R.id.finished).setVisibility(View.GONE);
                        grid.invalidateViews();
                        currentPage++;
                        LoadingStatus = false;
                    } else {
                        if (gridAdapter == null) {
                            contentHolder.setVisibility(View.GONE);
                            loadingHolder.setVisibility(View.GONE);
                            LoadingFooter.setVisibility(View.GONE);
                            view.findViewById(R.id.NothingFound).setVisibility(View.VISIBLE);
                        } else {
                            loadingHolder.setVisibility(View.GONE);
                            LoadingFooter.setVisibility(View.VISIBLE);
                            LoadingFooter.findViewById(R.id.nointernet).setVisibility(View.GONE);
                            LoadingFooter.findViewById(R.id.loadingmore).setVisibility(View.GONE);
                            LoadingFooter.findViewById(R.id.finished).setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    if (gridAdapter != null) {
                        loadingHolder.setVisibility(View.GONE);
                        LoadingFooter.setVisibility(View.VISIBLE);
                        LoadingFooter.findViewById(R.id.nointernet).setVisibility(View.VISIBLE);
                        LoadingFooter.findViewById(R.id.loadingmore).setVisibility(View.GONE);
                        LoadingFooter.findViewById(R.id.finished).setVisibility(View.GONE);
                    } else {
                        contentHolder.setVisibility(View.GONE);
                        loadingHolder.setVisibility(View.VISIBLE);
                        loadingHolder.findViewById(R.id.NoInternet).setVisibility(View.VISIBLE);
                        loadingHolder.findViewById(R.id.loadingmore).setVisibility(View.GONE);
                    }
                    grid.invalidateViews();
                }
            });

        }
    }

    private void RefreshItems() {
        if (CanLoad) {
            String ts = Long.toString(System.currentTimeMillis() / 1000L);
            String key = Utils.SHA1(ts);

            APIHandler.getApiInterface().GetNearestSupermarket(ts, key, currentPage, location.getLatitude(), location.getLongitude(), new Callback<List<GpsSupermarket>>() {
                @Override
                public void success(List<GpsSupermarket> items, Response response) {
                    if (items != null && items.size() != 0) {
                        gridAdapter.clear();
                        contentHolder.setVisibility(View.VISIBLE);
                        gridAdapter = new GridItemAdapter(view.getContext(), items);
                        grid.setAdapter(gridAdapter);
                        grid.setOnItemClickListener(ItemSelected);
                        loadingHolder.setVisibility(View.GONE);
                        LoadingFooter.setVisibility(View.VISIBLE);
                        LoadingFooter.findViewById(R.id.loadingmore).setVisibility(View.VISIBLE);
                        LoadingFooter.findViewById(R.id.nointernet).setVisibility(View.GONE);
                        LoadingFooter.findViewById(R.id.finished).setVisibility(View.GONE);
                        swipeLayout.setRefreshing(false);
                        grid.invalidateViews();
                        LoadingStatus = false;
                    } else {
                        contentHolder.setVisibility(View.GONE);
                        loadingHolder.setVisibility(View.GONE);
                        LoadingFooter.setVisibility(View.GONE);
                        view.findViewById(R.id.NothingFound).setVisibility(View.VISIBLE);
                    }
                    currentPage = 2;
                    LoadingStatus = false;
                }

                @Override
                public void failure(RetrofitError error) {
                    swipeLayout.setRefreshing(false);
                }
            });
        } else {
            swipeLayout.setRefreshing(false);
        }
    }

    @Override
    public void onResume() {
        if (!CanLoad && LoadedOnce) {
            LoadedOnce = true;
        }else{
            LoadedOnce = true;
            RefreshLocation();
        }
        super.onResume();
    }

    public boolean isOnline() {
        ConnectivityManager connectionManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    @Override
    public void setUserVisibleHint(boolean isFragmentVisible_) {
        super.setUserVisibleHint(true);
        if (this.isVisible()) {
            if (isFragmentVisible_ && !LoadedOnce) {
                RefreshLocation();
                LoadedOnce = true;
            }
        }
    }
}
