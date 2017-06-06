package smo.zooket.API;

import retrofit.RestAdapter;

/**
 * Created by Smo on 5/7/2017.
 */
public class APIHandler {

    private static final String API_URL = "http://devsmo.com/api/v1";

    private static RestAdapter restAdapter;

    private static RestAdapter getRestAdapter() {
        if (restAdapter == null) {
            restAdapter = new RestAdapter.Builder()
                    .setEndpoint(API_URL).setLogLevel(RestAdapter.LogLevel.FULL)
                    .build();
        }
        return restAdapter;
    }

    public static RestServices getApiInterface() {
        RestServices RestAPI = null;
        try {
            if (restAdapter == null) {
                restAdapter = getRestAdapter();
            }
            RestAPI = restAdapter.create(RestServices.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RestAPI;
    }
}
