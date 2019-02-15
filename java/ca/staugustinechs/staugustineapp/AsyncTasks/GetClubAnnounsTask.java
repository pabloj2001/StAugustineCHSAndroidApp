package ca.staugustinechs.staugustineapp.AsyncTasks;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.Interfaces.ClubAnnounGetter;
import ca.staugustinechs.staugustineapp.R;
import ca.staugustinechs.staugustineapp.Objects.ClubAnnouncement;

public class GetClubAnnounsTask extends AsyncTask<String, Void, List<ClubAnnouncement>> implements OnFailureListener {

    Activity activity;
    String clubId;
    ClubAnnounGetter getter;
    boolean isFinished = false;

    public GetClubAnnounsTask(String clubId, Activity activity, ClubAnnounGetter getter){
        this.activity = activity;
        this.clubId = clubId;
        this.getter = getter;
    }

    @Override
    protected List<ClubAnnouncement> doInBackground(String... strings) {
        if(AppUtils.isNetworkAvailable(activity)){
            return getClubAnnouns();
        }
        return null;
    }

    private List<ClubAnnouncement> getClubAnnouns() {
        List<ClubAnnouncement> announs = new ArrayList<ClubAnnouncement>();
        Task<QuerySnapshot> querySnapshotTask = FirebaseFirestore.getInstance()
                .collection("announcements")
                .whereEqualTo("club", clubId)
                .get().addOnFailureListener(this);

        while (!querySnapshotTask.isComplete()) { }

        if (querySnapshotTask.isSuccessful()) {
            for (DocumentSnapshot doc : querySnapshotTask.getResult().getDocuments()) {
                String imgName = doc.getString("img");
                Bitmap img = null;
                if (imgName != null && !imgName.isEmpty()) {
                    Task<StorageMetadata> metaTask = FirebaseStorage.getInstance()
                            .getReference("/announcements/" + imgName)
                            .getMetadata();

                    while (!metaTask.isComplete()) { }

                    if (metaTask.isSuccessful()) {
                        StorageMetadata meta = metaTask.getResult();
                        String imgName2 = imgName + "_" + meta.getUpdatedTimeMillis();
                        if (AppUtils.shouldGetFile(imgName2, activity)) {
                            Task<Uri> uriTask = FirebaseStorage.getInstance()
                                    .getReference("/announcements/" + imgName)
                                    .getDownloadUrl();

                            while (!uriTask.isComplete()) { }

                            if (uriTask.isSuccessful()) {
                                try {
                                    img = Picasso.with(activity)
                                            .load(uriTask.getResult())
                                            .resizeDimen(R.dimen.img_height, R.dimen.img_height)
                                            .centerInside()
                                            .get();

                                    AppUtils.saveImg(img, imgName, meta.getUpdatedTimeMillis(), activity);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            img = AppUtils.getImg(imgName2, activity);
                        }
                    }
                }

                ClubAnnouncement announ = new ClubAnnouncement(doc.getData(), img, doc.getId());
                announs.add(announ);
            }
        }

        return announs;
    }

    @Override
    protected void onPostExecute(List<ClubAnnouncement> announs) {
        if(announs != null){
            if(!this.isCancelled()){
                isFinished = true;
                getter.updateAnnouns(announs);
            }
        }else{
            getter.setOffline();
        }
    }

    public boolean isFinished() {
        return isFinished;
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        this.cancel(true);
    }
}
