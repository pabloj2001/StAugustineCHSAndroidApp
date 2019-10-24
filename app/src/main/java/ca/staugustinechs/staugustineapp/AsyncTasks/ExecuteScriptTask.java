package ca.staugustinechs.staugustineapp.AsyncTasks;

import android.app.Activity;
import android.os.AsyncTask;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.script.Script;
import com.google.api.services.script.ScriptScopes;
import com.google.api.services.script.model.ExecutionRequest;
import com.google.api.services.script.model.Operation;
import com.google.firebase.auth.FirebaseAuth;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.util.Arrays;

public class ExecuteScriptTask extends AsyncTask<String, Void, Integer> {

    private Activity activity;

    public ExecuteScriptTask(Activity activity){
        this.activity = activity;
    }

    @Override
    protected Integer doInBackground(String... strings) {
        return getScriptUsingGoogle2(strings);
    }

    private int getScriptUsingPost(String[] strings){
        try {
            /*String token = "";
            Task<GetTokenResult> getToken = FirebaseAuth.getInstance().getAccessToken(true);
            while(!getToken.isComplete()){}
            if(getToken.isSuccessful()){
                token = getToken.getResult().getToken();
            }*/

            URL url = new URL("https://script.google.com/macros/s/AKfycbx6-P4Lp35hzXzwBuLA4AAWpsmb8iAk2NtrewUcJxZ-Dzl7TYCi/exec" +
                    "?user_content_key=" + strings[0]);
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);

            HttpURLConnection urlConn = (HttpURLConnection) conn;
            urlConn.setRequestProperty("Authorization", "Bearer " + strings[0]);

            OutputStreamWriter wr = new OutputStreamWriter(urlConn.getOutputStream());
            wr.write("");
            wr.flush();

            System.out.println(urlConn.getResponseCode());
            if(urlConn.getResponseCode() == 200){
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }

                System.out.println(sb.toString());
                return Integer.parseInt(sb.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return -2;
    }

    private int getScriptUsingPost2(String[] strings){
        try {
            // Load the service account key JSON file
            InputStream serviceAccount = activity
                    .getAssets().open("staugustinechsapp-82d51d7a2bc6.json");

            GoogleCredential googleCred = GoogleCredential.fromStream(serviceAccount)
                    .createScoped(Arrays.asList(ScriptScopes.GROUPS));

            // Use the Google credential to generate an access token
            googleCred.refreshToken();

            URI url = new URI("https://script.google.com/macros/s/AKfycbx6-P4Lp35hzXzwBuLA4AAWpsmb8iAk2NtrewUcJxZ-Dzl7TYCi/exec");

            HttpClient client = new DefaultHttpClient();

            HttpPost request = new HttpPost();
            request.setURI(url);
            request.addHeader("Authorization", "Bearer " + googleCred.getAccessToken());

            HttpResponse response = client.execute(request);
            BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            String line = in.readLine();
            System.out.println(line);

            return Integer.parseInt(line);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return -2;
    }

    private int getScriptUsingGoogle(String[] strings){
        try {
            // Load the service account key JSON file
           /* InputStream serviceAccount = activity
                    .getAssets().open("staugustinechsapp-82d51d7a2bc6.json");

            GoogleCredential googleCred = GoogleCredential.fromStream(serviceAccount)
                    .createScoped(Arrays.asList(ScriptScopes.GROUPS));*/

            GoogleCredential credentials = new GoogleCredential.Builder()
                    .setClientSecrets("448336593725-i621cq64lq9aukrde0qc0mmrikv84cg4.apps.googleusercontent.com",
                            "-lktlCamexQaathNrAPaNuBe").build()
                    .createScoped(Arrays.asList(ScriptScopes.GROUPS));

            // Use the Google credential to generate an access token
           // googleCred.refreshToken();

            Script service = new Script.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(), credentials)
                    .setApplicationName("St Augustine CHS App")
                    .build();

            // Create an execution request object.
            ExecutionRequest request = new ExecutionRequest().setFunction("testGroup");

            // Make the request.
            Operation op = service.scripts().run(strings[1], request).execute();
            if (op.getResponse() != null && op.getResponse().get("result") != null) {
                return (int) op.getResponse().get("result");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

        return -2;
    }

    private int getScriptUsingGoogle2(String[] strings){
        try{
            GoogleAccountCredential credential =
                    GoogleAccountCredential.usingOAuth2(activity, Arrays.asList(ScriptScopes.GROUPS));
            credential.setSelectedAccountName(FirebaseAuth.getInstance().getCurrentUser().getEmail());

            Script service = new Script.Builder(new NetHttpTransport(),
                    JacksonFactory.getDefaultInstance(), credential)
                    .setApplicationName("St Augustine CHS App")
                    .build();

            ExecutionRequest request = new ExecutionRequest().setFunction("testGroup");

            System.out.println("BEFORE REQUEST");

            // Make the request.
            InputStream in = service.scripts().run(strings[1], request).executeAsInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            System.out.println("GETTING RESULT");
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            System.out.println("WE GOT IT BOIZZ");
            reader.close();

            if (line != null) {
                System.out.println("RETURNNNNN!!!");
                return Integer.parseInt(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return -2;
    }

}
