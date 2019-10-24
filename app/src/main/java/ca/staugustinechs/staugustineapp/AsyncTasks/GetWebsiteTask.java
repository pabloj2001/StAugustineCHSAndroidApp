package ca.staugustinechs.staugustineapp.AsyncTasks;

import android.os.AsyncTask;

import com.crashlytics.android.Crashlytics;
import com.squareup.okhttp.Dns;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import java.util.concurrent.TimeUnit;

import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.Fragments.HomeFragment;

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
