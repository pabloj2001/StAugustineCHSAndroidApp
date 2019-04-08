package ca.staugustinechs.staugustineapp.AsyncTasks;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

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
import java.util.List;

import ca.staugustinechs.staugustineapp.Activities.ClubDetails;
import ca.staugustinechs.staugustineapp.Activities.Profile;
import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.Interfaces.BadgeGetter;
import ca.staugustinechs.staugustineapp.Objects.Badge;
import ca.staugustinechs.staugustineapp.Objects.ClubItem;
import ca.staugustinechs.staugustineapp.R;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class GetBadgesTask extends AsyncTask<String, Void, List<Badge>> implements OnFailureListener {

    private ClubDetails clubDetails;
    private ClubItem club;
    private Profile profile;
    private Activity activity;

    public GetBadgesTask(ClubDetails clubDetails){
        this.clubDetails = clubDetails;
        this.club = clubDetails.getClub();
    }

    public GetBadgesTask(Profile profile){
        this.profile = profile;
    }

    @Override
    protected List<Badge> doInBackground(String... strings) {
        this.activity = clubDetails == null ? profile : clubDetails;
        if(AppUtils.isNetworkAvailable(activity)){
            return getBadges();
        }

        return null;
    }

    private List<Badge> getBadges() {
        List<DocumentSnapshot> docs = new ArrayList<DocumentSnapshot>();;
        if(clubDetails != null){
            Task<QuerySnapshot> querySnapshotTask = FirebaseFirestore.getInstance().collection("badges")
                    .whereEqualTo("club", club.getId()).get()
                    .addOnFailureListener(this);

            while (!querySnapshotTask.isComplete()) { }

            if (querySnapshotTask.isSuccessful()) {
                docs = querySnapshotTask.getResult().getDocuments();
            }
        }else{
            for(String badgeId : profile.getProfile().getBadges()){
                Task<DocumentSnapshot> docSnapshotTask = FirebaseFirestore.getInstance()
                        .collection("badges")
                        .document(badgeId).get()
                        .addOnFailureListener(this);

                while (!docSnapshotTask.isComplete()) { }

                if (docSnapshotTask.isSuccessful()) {
                    docs.add(docSnapshotTask.getResult());
                }
            }
        }

        if (docs != null) {
            List<Badge> badges = new ArrayList<Badge>();
            for (DocumentSnapshot doc : docs) {
                String imgName = doc.getString("img");
                Bitmap img = null;
                if (imgName != null && !imgName.isEmpty()) {
                    Task<StorageMetadata> metaTask = FirebaseStorage.getInstance()
                            .getReference("/badges/" + imgName)
                            .getMetadata();

                    while (!metaTask.isComplete()) { }

                    if (metaTask.isSuccessful()) {
                        StorageMetadata meta = metaTask.getResult();
                        String imgName2 = imgName + "_" + meta.getUpdatedTimeMillis();
                        if (AppUtils.shouldGetFile(imgName2, activity)) {
                            Task<Uri> uriTask = FirebaseStorage.getInstance()
                                    .getReference("/badges/" + imgName)
                                    .getDownloadUrl();

                            while (!uriTask.isComplete()) { }

                            if (uriTask.isSuccessful()) {
                                try {
                                    img = Picasso.with(activity)
                                            .load(uriTask.getResult())
                                            .transform(new CropCircleTransformation())
                                            .resizeDimen(R.dimen.badge_size, R.dimen.badge_size)
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

                if(doc.getData() != null){
                    Badge badge = new Badge(doc.getId(), doc.getData(), imgName, img);
                    badges.add(badge);
                }
            }

            return badges;
        }

        return null;
    }

    @Override
    protected void onPostExecute(List<Badge> badges) {
        if(badges != null){
            if(!this.isCancelled()){
                ((BadgeGetter) activity).updateBadges(badges);
            }
        }else{
            ((BadgeGetter) activity).setOffline();
        }
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        this.cancel(true);
    }
}
