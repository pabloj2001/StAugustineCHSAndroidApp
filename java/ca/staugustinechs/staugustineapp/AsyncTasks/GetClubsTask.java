package ca.staugustinechs.staugustineapp.AsyncTasks;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ca.staugustinechs.staugustineapp.Activities.Profile;
import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.Fragments.ClubsFragment;
import ca.staugustinechs.staugustineapp.R;
import ca.staugustinechs.staugustineapp.Objects.ClubItem;

public class GetClubsTask extends AsyncTask<String, Void, List<ClubItem>> implements OnFailureListener {

    public static final int JOINED = 0, OTHERS = 1;
    private ClubsFragment clubsFragment;
    private int mode = 0;
    private Profile profile;
    private Activity activity;

    //CONSTRUCTOR FOR GETTING CLUBS FROM THE CLUB FRAGMENT
    public GetClubsTask(ClubsFragment clubsFragment, int mode){
        this.clubsFragment = clubsFragment;
        this.mode = mode;
    }

    //CONSTRUCTOR FOR GETTING CLUBS THAT A USER IS A PART OF
    public GetClubsTask(Profile profile){
        this.profile = profile;
    }

    @Override
    protected List<ClubItem> doInBackground(String... strings) {
        activity = clubsFragment != null ? clubsFragment.getActivity() : profile;
        if(AppUtils.isNetworkAvailable(activity)){
            if(strings.length > 0){
                return getClubs(strings[0]);
            }else{
                return getClubs();
            }
        }

        return null;
    }

    private List<ClubItem> getClubs(){
        return getClubs(FirebaseAuth.getInstance().getUid());
    }

    private List<ClubItem> getClubs(String userId) {
        //GET CLUBS WHERE USER IS AN ADMIN
        Task<QuerySnapshot> clubAdminTask = FirebaseFirestore.getInstance().collection("clubs")
                .whereArrayContains("admins", userId).get()
                .addOnFailureListener(this);

        while (!clubAdminTask.isComplete()) { }

        //GET CLUBS WHERE USER IS A MEMBER
        Task<QuerySnapshot> clubMemberTask = FirebaseFirestore.getInstance().collection("clubs")
                .whereArrayContains("members", userId).get()
                .addOnFailureListener(this);

        while (!clubMemberTask.isComplete()) { }

        if (clubAdminTask.isSuccessful() && clubMemberTask.isSuccessful()) {
            List<DocumentSnapshot> queryDocs = clubAdminTask.getResult().getDocuments();
            //COMBINE THE ADMINS AND MEMBERS DOCUMENTS
            queryDocs.addAll(clubMemberTask.getResult().getDocuments());
            if(mode == 0){
                //IF WE ARE ONLY GETTING THE CLUBS THE USER IS A PART OF, GET AND RETURN THOSE
                return getClubItems(queryDocs);
            }else{
                //IF WE ARE GETTING THE CLUBS THE USER IS NOT A PART OF,
                //GET ALL THE CLUBS
                Task<QuerySnapshot> clubAllTask = FirebaseFirestore.getInstance()
                        .collection("clubs").get()
                        .addOnFailureListener(this);

                while (!clubAllTask.isComplete()) { }

                if(clubAllTask.isSuccessful()){
                    List<DocumentSnapshot> allDocs = clubAllTask.getResult().getDocuments();
                    //REMOVE THE CLUBS THE USER IS A PART OF FROM ALL OF THE CLUBS
                    allDocs.removeAll(queryDocs);
                    //GET AND RETURN THE CLUBS
                    return getClubItems(allDocs);
                }
            }
        }
        return null;
    }

    private List<ClubItem> getClubItems(List<DocumentSnapshot> docs){
        List<ClubItem> clubItems = new ArrayList<ClubItem>();
        for(final DocumentSnapshot doc : docs){
            String imgName = (String) doc.get("img");
            Bitmap img = null;
            String imgName2 = "";
            if(!imgName.isEmpty()){
                //GET THE BANNER FOR EACH CLUBS
                Task<StorageMetadata> metaTask = FirebaseStorage.getInstance()
                        .getReference("/clubBanners/" + imgName)
                        .getMetadata();

                while (!metaTask.isComplete()) { }

                if(metaTask.isSuccessful()) {
                    StorageMetadata meta = metaTask.getResult();
                    imgName2 = imgName + "_" + meta.getUpdatedTimeMillis();
                    if (AppUtils.shouldGetFile(imgName2, activity)) {
                        Task<Uri> uriTask = FirebaseStorage.getInstance()
                                .getReference("/clubBanners/" + imgName).getDownloadUrl();

                        while (!uriTask.isComplete()) { }

                        if (uriTask.isSuccessful()) {
                            try {
                                Bitmap bmp = Picasso.with(activity)
                                        .load(uriTask.getResult())
                                        .get();

                                int width = AppUtils.getDimen(R.dimen.banner_width, activity);
                                int height = AppUtils.getDimen(R.dimen.banner_height, activity);
                                img = Bitmap.createScaledBitmap(bmp, width, height, false);

                                AppUtils.saveImg(img, imgName, meta.getUpdatedTimeMillis(), activity);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }else{
                        img = AppUtils.getImg(imgName2, activity);
                    }
                }
            }

            //MAKE THE CLUBS ITEM FROM THE DATA AND BANNER IMAGE
            ClubItem club = new ClubItem(doc.getId(), doc.getData(), img, imgName2);
            clubItems.add(club);
        }
        //RETURN THE LIST OF THE CLUBS
        return clubItems;
    }

    @Override
    protected void onPostExecute(List<ClubItem> clubItems) {
        if(clubItems != null){
            if(!this.isCancelled()){
                //RETURN LIST OF CLUBS TO CALLING CLASS
                if(clubsFragment != null){
                    clubsFragment.updateClubs(clubItems);
                }else{
                    profile.updateClubs(clubItems);
                }
            }
        }else{
            if(clubsFragment != null) {
                clubsFragment.setOffline();
            }else{
                profile.setOffline();
            }
        }
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        this.cancel(true);
    }
}
