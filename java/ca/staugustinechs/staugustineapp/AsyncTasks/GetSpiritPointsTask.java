package ca.staugustinechs.staugustineapp.AsyncTasks;

import android.os.AsyncTask;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.Fragments.CafMenuFragment;
import ca.staugustinechs.staugustineapp.Fragments.SpiritMetersFragment;
import ca.staugustinechs.staugustineapp.Objects.CafMenuItem;

public class GetSpiritPointsTask extends AsyncTask<String, Void, int[]> {

    SpiritMetersFragment spiritMetersFragment;

    public GetSpiritPointsTask(SpiritMetersFragment spiritMetersFragment){
        this.spiritMetersFragment = spiritMetersFragment;
    }

    @Override
    protected int[] doInBackground(String... strings) {
        Task<DocumentSnapshot> task = FirebaseFirestore.getInstance().collection("info")
                .document("spiritPoints").get();

        while(!task.isComplete()){ }

        if(task.isSuccessful()){
            DocumentSnapshot doc = task.getResult();

            String[] years = ((List<String>) doc.get("years")).toArray(new String[]{});

            int[] points = new int[4];
            points[0] = AppUtils.longToInt(doc.getLong(years[3]));
            points[1] = AppUtils.longToInt(doc.getLong(years[2]));
            points[2] = AppUtils.longToInt(doc.getLong(years[1]));
            points[3] = AppUtils.longToInt(doc.getLong(years[0]));

            return points;
        }

        return null;
    }

    @Override
    protected void onPostExecute(int[] points) {
        if(!this.isCancelled()){
            spiritMetersFragment.updatePoints(points);
        }
    }

}
