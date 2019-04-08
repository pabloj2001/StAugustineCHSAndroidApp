package ca.staugustinechs.staugustineapp.AsyncTasks;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Looper;
import android.security.NetworkSecurityPolicy;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.squareup.okhttp.Dns;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.Fragments.HomeFragment;
import ca.staugustinechs.staugustineapp.R;

public class GetWebsiteTask extends AsyncTask<String, Void, String> {

    private HomeFragment homeFragment;
    private OkHttpClient client;

    public GetWebsiteTask(HomeFragment homeFragment){
        this.homeFragment = homeFragment;
    }

    @Override
    protected String doInBackground(String... strings) {
        if(homeFragment.getContext() != null && homeFragment.getActivity() != null){
            if (AppUtils.isNetworkAvailable(homeFragment.getActivity())) {
                //BufferedReader in = null;
                try {
                        /*URL url = new URL(strings[0]);
                        in = new BufferedReader(new InputStreamReader(url.openStream()));

                        String inputLine;
                        StringBuilder result = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                            result.append(inputLine);
                        }

                        return result.toString();*/

                    client = new OkHttpClient();
                    client.setConnectTimeout(30, TimeUnit.SECONDS);
                    client.setReadTimeout(30, TimeUnit.SECONDS);
                    client.setWriteTimeout(30, TimeUnit.SECONDS);
                    client.setDns(Dns.SYSTEM);
                    client.setRetryOnConnectionFailure(true);

                    Request request = new Request.Builder().url(strings[0]).tag("FETCH").build();

                    return client.newCall(request).execute().body().string();

                    /*DefaultHttpClient httpClient = new DefaultHttpClient();
                    HttpGet httpGet = new HttpGet(strings[0]);
                    ResponseHandler<String> resHandler = new BasicResponseHandler();
                    return httpClient.execute(httpGet, resHandler);*/
                } catch (Exception e) {
                    //CAN'T CONNECT OR SOME OTHER ERROR OCCURED
                    e.printStackTrace();
                    Crashlytics.logException(e);
                    return null;
                }/* finally {
                        try {
                            if (in != null) {
                                in.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }*/
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        if(!this.isCancelled()){
            System.out.println("UPDATING ANNONUCEMENTS");
            homeFragment.updateAnnouncements(s);
        }
    }

    @Override
    protected void onCancelled() {
        if(client != null){
            client.cancel("FETCH");
        }
        super.onCancelled();
    }
}
