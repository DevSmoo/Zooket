package smo.zooket;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import in.srain.cube.views.GridViewWithHeaderAndFooter;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import smo.zooket.Adapter.GridItemAdapter;
import smo.zooket.Adapter.GridProductAdapter;
import smo.zooket.Adapter.SwipeLayout;
import smo.zooket.Models.DatabaseSimpleProduct;
import smo.zooket.Models.DatabaseSimpleSupermarket;
import smo.zooket.Models.GpsSupermarket;
import smo.zooket.Models.SimpleProduct;
import smo.zooket.Models.SimpleSupermarket;

/**
 * Created by Smo on 16/05/2017.
 */
public class FavSuperFragment extends Fragment {
    Realm realm;
    View view;
    GridViewWithHeaderAndFooter grid;
    GridItemAdapter gridAdapter;
    LinearLayout loadingHolder;
    LinearLayout contentHolder;
    SwipeLayout swipeLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fav_pro_fragment, container, false);
        grid = (GridViewWithHeaderAndFooter) view.findViewById(R.id.itemgrid);
        loadingHolder = (LinearLayout) view.findViewById(R.id.loadingHolder);
        contentHolder = (LinearLayout) view.findViewById(R.id.contentHolder);

        //Initialization
        grid.setSmoothScrollbarEnabled(true);

        //Attach DB
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(view.getContext()).deleteRealmIfMigrationNeeded().build();
        realm = Realm.getInstance(realmConfig);

        //Pull To Refresh
        swipeLayout = (SwipeLayout) view;
        swipeLayout.setEnabled(false);

        //Set Loading
        loadingHolder.findViewById(R.id.NoInternet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingHolder.findViewById(R.id.loadingmore).setVisibility(View.VISIBLE);
                loadingHolder.findViewById(R.id.NoInternet).setVisibility(View.GONE);
                new FetchAsyncTask(getActivity()).execute();
            }
        });


        new FetchAsyncTask(getActivity()).execute();
        return view;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }
    private AdapterView.OnItemClickListener ItemSelected = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            GpsSupermarket item = (GpsSupermarket) grid.getAdapter().getItem(position);
            if (item != null) {
                Intent MyIntent = new Intent(getActivity(), SupermarketActivity.class);
                MyIntent.putExtra("ID", item.getID());
                startActivity(MyIntent);
            }
        }
    };
    @Override
    public void onResume() {
        super.onResume();
        contentHolder.setVisibility(View.GONE);
        loadingHolder.setVisibility(View.VISIBLE);
        loadingHolder.findViewById(R.id.loadingmore).setVisibility(View.VISIBLE);
        loadingHolder.findViewById(R.id.NoInternet).setVisibility(View.GONE);
        new FetchAsyncTask(getActivity()).execute();
    }
    class FetchAsyncTask extends AsyncTask<Void, Void, Void> {
        Activity activity;
        boolean wasSuccessfull = false;

        FetchAsyncTask(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            //Dismiss Dialog Or Show
            if (!wasSuccessfull) {
                contentHolder.setVisibility(View.GONE);
                loadingHolder.setVisibility(View.VISIBLE);
                loadingHolder.findViewById(R.id.NoInternet).setVisibility(View.VISIBLE);
                loadingHolder.findViewById(R.id.loadingmore).setVisibility(View.GONE);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            Realm r = null;
            try {
                RealmConfiguration realmConfig = new RealmConfiguration.Builder(getActivity()).deleteRealmIfMigrationNeeded().build();
                r = Realm.getInstance(realmConfig);
                RealmResults<DatabaseSimpleSupermarket> realmitems = r.where(DatabaseSimpleSupermarket.class).findAll();
                final List<GpsSupermarket> items = new ArrayList<GpsSupermarket>();
                for (int i = 0; i < realmitems.size(); i++) {
                    GpsSupermarket simpleItem = new GpsSupermarket();
                    simpleItem.setID(realmitems.get(i).getID());
                    simpleItem.setName(realmitems.get(i).getName());
                    simpleItem.setProvince(realmitems.get(i).getProvince());
                    // simpleItem.setLikesCount(realmitems.get(i).getLikesCount());
                    if (realmitems.get(i).getMainImg() != null && !realmitems.get(i).getMainImg().isEmpty()) {
                        simpleItem.setThumbnail(getActivity().getFilesDir().getAbsolutePath() + realmitems.get(i).getMainImg());
                    } else {
                        simpleItem.setThumbnail(null);
                    }
                    //simpleItem.setProvince(realmitems.get(i).getProvince());
                    items.add(simpleItem);
                }
                if (items != null && items.size() != 0) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            gridAdapter = new GridItemAdapter(getActivity(), items);
                            grid.setAdapter(gridAdapter);
                            grid.setOnItemClickListener(ItemSelected);
                            grid.deferNotifyDataSetChanged();
                            loadingHolder.setVisibility(View.GONE);
                            contentHolder.setVisibility(View.VISIBLE);
                            grid.invalidateViews();
                        }
                    });
                } else if (items.size() == 0) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingHolder.setVisibility(View.GONE);
                            contentHolder.setVisibility(View.GONE);
                            view.findViewById(R.id.NothingFound).setVisibility(View.VISIBLE);
                        }
                    });
                }
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
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }
}
