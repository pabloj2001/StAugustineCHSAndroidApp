package ca.staugustinechs.staugustineapp.AsyncTasks;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.URL;
import java.net.URLConnection;

import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.Fragments.HomeFragment;
import ca.staugustinechs.staugustineapp.R;

public class GetWebsiteTask extends AsyncTask<String, Void, String> {

    private HomeFragment homeFragment;

    public GetWebsiteTask(HomeFragment homeFragment){
        this.homeFragment = homeFragment;
    }

    @Override
    protected String doInBackground(String... strings) {
        if(ContextCompat.checkSelfPermission(homeFragment.getContext(), Manifest.permission.INTERNET)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(homeFragment.getContext(), Manifest.permission.ACCESS_NETWORK_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            if (AppUtils.isNetworkAvailable(homeFragment.getActivity())) {
                Log.println(Log.DEBUG, "GetWebsiteTask", "FETCHING");
                BufferedReader in = null;
                InputStream is = null;
                try {
                    URL url = new URL(strings[0]);
                    URLConnection uc = url.openConnection();
                    uc.setConnectTimeout(10000);

                    uc.connect();
                    is = uc.getInputStream();
                    in = new BufferedReader(new InputStreamReader(is));

                    String inputLine;
                    StringBuilder result = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        result.append(inputLine);
                    }

                    return result.toString();
                } catch (Exception e) {
                    //CAN'T CONNECT OR SOME OTHER ERROR OCCURED
                    e.printStackTrace();
                    Crashlytics.logException(e);
                    this.cancel(true);
                    return null;
                } finally {
                    try {
                        if (in != null) {
                            in.close();
                        }

                        if(is != null){
                            is.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }else{
            ActivityCompat.requestPermissions(homeFragment.getActivity(),
                    new String[]{Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE},
                    19);
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        if(!this.isCancelled()){
            Log.println(Log.DEBUG, "GetWebsiteTask", "UPDATING HOME ANNOUNS");
            homeFragment.updateAnnouncements(s);
        }
    }

}
