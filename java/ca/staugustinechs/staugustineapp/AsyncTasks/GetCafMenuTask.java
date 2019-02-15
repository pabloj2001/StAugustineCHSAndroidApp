package ca.staugustinechs.staugustineapp.AsyncTasks;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.Fragments.CafMenuFragment;
import ca.staugustinechs.staugustineapp.Fragments.HomeFragment;
import ca.staugustinechs.staugustineapp.Objects.CafMenuItem;

public class GetCafMenuTask extends AsyncTask<String, Void, List<CafMenuItem>> {

    private CafMenuFragment cafMenuFragment;
    private boolean dailyMenu;

    public GetCafMenuTask(CafMenuFragment cafMenuFragment, boolean dailyMenu){
        this.cafMenuFragment = cafMenuFragment;
        this.dailyMenu = dailyMenu;
    }

    @Override
    protected List<CafMenuItem> doInBackground(String... strings) {
        Task<DocumentSnapshot> task = FirebaseFirestore.getInstance().collection("info")
                .document(dailyMenu ? "cafMenu" : "cafMenuRegular").get();

        while(!task.isComplete()){ }

        if(task.isSuccessful()){
            List<CafMenuItem> items = new ArrayList<CafMenuItem>();
            for(Map.Entry<String, Object> entry : task.getResult().getData().entrySet()){
                items.add(new CafMenuItem(entry.getKey(),
                        entry.getValue() instanceof Long ? (long) entry.getValue() : (double) entry.getValue()));
            }
            return items;
        }

        return null;
    }

    @Override
    protected void onPostExecute(List<CafMenuItem> items) {
        if(!this.isCancelled()){
            cafMenuFragment.updateMenu(items, dailyMenu);
        }
    }

}
