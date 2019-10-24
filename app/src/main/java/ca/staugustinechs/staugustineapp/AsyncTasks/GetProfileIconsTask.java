package ca.staugustinechs.staugustineapp.AsyncTasks;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ca.staugustinechs.staugustineapp.Activities.Main;
import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.Interfaces.IconGetter;
import ca.staugustinechs.staugustineapp.Objects.ProfileIcon;
import ca.staugustinechs.staugustineapp.R;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class GetProfileIconsTask extends AsyncTask<Integer, Void, List<ProfileIcon>> implements OnFailureListener {

    public enum IconGetType{
        SINGLE, MULTIPLE, BYRARITY, ALL;
    }

    private Activity activity;
    private Fragment fragment;
    private IconGetType type;

    public GetProfileIconsTask(Activity activity, IconGetType type){
        this.activity = activity;
        this.type = type;
    }

    public GetProfileIconsTask(Fragment fragment, IconGetType type){
        this.fragment = fragment;
        this.activity = fragment.getActivity();
        this.type = type;
    }

    @Override
    protected List<ProfileIcon> doInBackground(Integer... ints) {
        if(AppUtils.isNetworkAvailable(activity)){
            return getProfileIcons(ints);
        }
        return null;
    }

    public List<ProfileIcon> getProfileIcons(Integer[] ints) {
        List<ProfileIcon> icons = new ArrayList<ProfileIcon>();

        Task<DocumentSnapshot> picsSnapshotTask = FirebaseFirestore.getInstance().collection("info")
                .document("profilePics").get().addOnFailureListener(this);

        while (!picsSnapshotTask.isComplete()) { }

        if (picsSnapshotTask.isSuccessful()) {
            List<Long> rarities = (List<Long>) picsSnapshotTask.getResult().get("rarities");
            List<Integer> owned = null;
            if(Main.PROFILE != null){
                owned = Main.PROFILE.getPicsOwned();
            }else{
                owned = new ArrayList<Integer>();
            }

            if(type.equals(IconGetType.SINGLE)){
                Bitmap img = getPic(ints[0]);
                icons.add(new ProfileIcon(ints[0], img,
                        AppUtils.longToInt(rarities.get(ints[0] < 0 ? 0 : ints[0])),
                        owned.contains(ints[0])));
                return icons;
            }else if(type.equals(IconGetType.MULTIPLE)){
                for(int iconId : ints){
                    if (iconId < rarities.size()) {
                        Long cost = rarities.get(iconId < 0 ? 0 : iconId);
                        if (cost != null) {
                            Bitmap img = getPic(iconId);
                            icons.add(new ProfileIcon(iconId, img, AppUtils.longToInt(cost),
                                    owned.contains(iconId)));
                        }
                    }
                }
            }else if(type.equals(IconGetType.BYRARITY)){
                for (int a = 0; a < rarities.size(); a++) {
                    int rarity = AppUtils.longToInt(rarities.get(a));
                    if (rarity == ints[0]) {
                        Bitmap img = getPic(a);
                        icons.add(new ProfileIcon(a, img, rarity, owned.contains(a)));
                    }
                }
            }else if(type.equals(IconGetType.ALL)){
                for(int i = 0; i < rarities.size(); i++){
                    if(rarities.get(i) != null){
                        Bitmap img = getPic(i);
                        icons.add(new ProfileIcon(i, img, AppUtils.longToInt(rarities.get(i)),
                                owned.contains(i)));
                    }
                }
            }
        }

        return icons;
    }

    private Bitmap getPic(int i){
        Task<StorageMetadata> metaTask = FirebaseStorage.getInstance()
                .getReference("/profilePictures/" + i + ".png")
                .getMetadata();

        while (!metaTask.isComplete()) { }

        Bitmap img = null;
        if (metaTask.isSuccessful()) {
            StorageMetadata meta = metaTask.getResult();
            String imgName = "icon" + i + "_" + meta.getUpdatedTimeMillis();
            if (AppUtils.shouldGetFile(imgName, activity)) {
                Task<Uri> uriTask = FirebaseStorage.getInstance()
                        .getReference("/profilePictures/" + i + ".png")
                        .getDownloadUrl();

                while (!uriTask.isComplete()) { }

                if (uriTask.isSuccessful()) {
                    try {
                        int px = AppUtils.getDimen(R.dimen.icon_big_size, activity);
                        img = Picasso.get()//with(activity)
                                .load(uriTask.getResult())
                                .transform(new CropCircleTransformation())
                                .resize(px, px)
                                .centerCrop()
                                .get();

                        AppUtils.saveImg(img, "icon" + i, meta.getUpdatedTimeMillis(), activity);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                img = AppUtils.getImg(imgName, activity);
            }
        }
        return img;
    }

    @Override
    protected void onPostExecute(List<ProfileIcon> icons) {
        if (!this.isCancelled()) {
            if(fragment != null){
                ((IconGetter) fragment).updateIcons(icons);
            }else{
                ((IconGetter) activity).updateIcons(icons);
            }
        }
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        this.cancel(true);
    }
}
