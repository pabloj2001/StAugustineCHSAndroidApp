package ca.staugustinechs.staugustineapp.Objects;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.staugustinechs.staugustineapp.Activities.ClubMemberList;
import ca.staugustinechs.staugustineapp.Activities.Main;
import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.MessagingService;
import ca.staugustinechs.staugustineapp.R;

public class ClubItem implements Serializable {

    private Bitmap img;
    private String name, desc, id, imgName, clubBadge;
    private List<String> admins, members, pendingList;
    private int joinPref = 0;

    public ClubItem(String id, Map<String, Object> data, Bitmap img, String imgName){
        this.id = id;
        this.name = (String) data.get("name");
        this.desc = (String) data.get("desc");
        this.imgName = imgName.isEmpty() ? (String) data.get("img") : imgName;
        this.joinPref = (int) (long) data.get("joinPref");
        this.admins = (List<String>) data.get("admins");
        this.members = (List<String>) data.get("members");
        this.pendingList = (List<String>) data.get("pending");
        this.clubBadge = (String) data.get("clubBadge");
        this.img = img;
    }

    public ClubItem pack(){
        img = null;
        return this;
    }

    public void unpack(Activity activity){
        Bitmap bmp = AppUtils.getImg(imgName, activity);
        img = Bitmap.createScaledBitmap(bmp, (int) Math.floor(bmp.getWidth() * 1.1),
                (int) Math.floor(bmp.getHeight() * 1.1), false);
    }

    public String getId() {
        return id;
    }

    public Bitmap getImg() {
        return img;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImgName(){
        return imgName;
    }

    public List<String> getAdmins() {
        return admins;
    }

    public List<String> getMembers() {
        return members;
    }

    public List<String> getPendingList() {
        return pendingList;
    }

    public int getJoinPref(){
        return joinPref;
    }

    public void setJoinPref(int joinPref) {
        this.joinPref = joinPref;
    }

    public String getClubBadge(){
        return clubBadge;
    }

    public void makeAdmin(UserProfile user) {
        if(members.contains(user.getUid())){
            FirebaseFirestore.getInstance().collection("clubs")
                    .document(this.getId())
                    .update("admins", FieldValue.arrayUnion(user.getUid()));

            FirebaseFirestore.getInstance().collection("clubs")
                    .document(getId())
                    .update("members", FieldValue.arrayRemove(user.getUid()));

            MessagingService.sendMessageToUser(user.getMessagingToken(), "You're Now An Admin!",
                    "Congratulations, you're now an Admin of " + this.getName() + "!");

            this.members.remove(user.getUid());
            this.admins.add(user.getUid());
        }
    }

    public int removeMember(UserProfile user) {
        //UNSUBRSCRIBE USER FROM NOTIFICATIONS
        if(user.getUid().equals(Main.PROFILE.getUid())){
            Main.PROFILE.removeNotification(this.getId(), null);
        }else{
            MessagingService.unsubscribe(this.getId(), user.getUid(), user.getMessagingToken(), null);
        }

        //TAKE AWAY BADGE
        user.removeBadge(clubBadge, null);
        //TAKE AWAY POINTS FROM USER IF THE CLUB JOIN PREF IS 1
        if(this.getJoinPref() == 1 && (getMembers().contains(user.getUid())
                || getAdmins().contains(user.getUid()))){
            user.updatePoints(-AppUtils.JOINING_CLUB_POINTS, true, null, null);
        }

        //REMOVE USER FROM CLUB DEPENDING WHETHER USER IS IN PENDING, MEMBER, OR ADMIN LIST
        if(this.getMembers().contains(user.getUid())){
            FirebaseFirestore.getInstance().collection("clubs")
                    .document(this.getId())
                    .update("members", FieldValue.arrayRemove(user.getUid()));
            removeClubFromUser(user.getUid());
            this.members.remove(user.getUid());
            return ClubMemberList.MEMBERS;
        }else if(this.getAdmins().contains(user.getUid())){
            FirebaseFirestore.getInstance().collection("clubs")
                    .document(this.getId())
                    .update("admins", FieldValue.arrayRemove(user.getUid()));
            this.admins.remove(user.getUid());
            removeClubFromUser(user.getUid());
            return ClubMemberList.ADMINS;
        }else if(this.getPendingList().contains(user.getUid())){
            FirebaseFirestore.getInstance().collection("clubs")
                    .document(this.getId())
                    .update("pending", FieldValue.arrayRemove(user.getUid()));
            this.pendingList.remove(user.getUid());
            removeClubFromUser(user.getUid());
            return ClubMemberList.PENDING;
        }

        return -1;
    }

    public void demoteAdmin(UserProfile user) {
        if(admins.contains(user.getUid())){
            FirebaseFirestore.getInstance().collection("clubs")
                    .document(this.getId())
                    .update("members", FieldValue.arrayUnion(user.getUid()));

            FirebaseFirestore.getInstance().collection("clubs")
                    .document(this.getId())
                    .update("admins", FieldValue.arrayRemove(user.getUid()));

            this.admins.remove(user.getUid());
            this.members.add(user.getUid());
        }
    }

    public void acceptUser(UserProfile user) {
        if(pendingList.contains(user.getUid())){
            FirebaseFirestore.getInstance().collection("clubs")
                    .document(this.getId())
                    .update("members", FieldValue.arrayUnion(user.getUid()));

            FirebaseFirestore.getInstance().collection("clubs")
                    .document(this.getId())
                    .update("pending", FieldValue.arrayRemove(user.getUid()));

            this.addClubToUser(user.getUid());

            //SUB USER TO NOTIFICATIONS
            MessagingService.subscribe(this.getId(), user.getUid(), user.getMessagingToken(), null);
            MessagingService.sendMessageToUser(user.getMessagingToken(), "Welcome To The Club!",
                    "You've been accepted into " + this.getName() + "! Yay!");

            //GIVE USER BADGE
            user.giveBadge(clubBadge, null);
            //GIVE USER POINTS
            user.updatePoints(AppUtils.JOINING_CLUB_POINTS, false, null, null);

            this.pendingList.remove(user.getUid());
            this.members.add(user.getUid());
        }
    }

    public void addUser(String userId, final OnCompleteListener listener) {
        FirebaseFirestore.getInstance().collection("clubs")
                .document(this.getId())
                .update("members", FieldValue.arrayUnion(userId));

        this.addClubToUser(userId);

        if(userId.equals(Main.PROFILE.getUid())){
            Main.PROFILE.addNotification(this.getId(), listener);
        }else{
            FirebaseFirestore.getInstance().collection("users")
                    .document(userId).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                DocumentSnapshot doc = task.getResult();
                                MessagingService.subscribe(getId(), doc.getId(),
                                        doc.getString("msgToken"), listener);
                            }
                        }
                    });
        }

        //GIVE USER BADGE
        if(clubBadge != null && !clubBadge.isEmpty()){
            FirebaseFirestore.getInstance().collection("users")
                    .document(userId).update("badges", FieldValue.arrayUnion(clubBadge));
        }

        this.members.add(userId);
    }

    private void removeClubFromUser(String userId){
        DocumentReference doc = FirebaseFirestore.getInstance()
                .collection("users").document(userId);
        doc.update("clubs", FieldValue.arrayRemove(this.getId()));
        doc.update("notifications", FieldValue.arrayRemove(this.getId()));
    }

    private void addClubToUser(String userId){
        DocumentReference doc = FirebaseFirestore.getInstance()
                .collection("users").document(userId);
        doc.update("clubs", FieldValue.arrayUnion(this.getId()));
        doc.update("notifications", FieldValue.arrayUnion(this.getId()));
    }

    public static void createClub(String name, String desc, Uri selectedImage, int currentlyChecked,
                                  final OnCompleteListener completeListener,
                                  final OnFailureListener failureListener, Activity activity) {
        final Map<String, Object> data = new HashMap<String, Object>();
        data.put("name", name);
        data.put("desc", desc);
        data.put("joinPref", currentlyChecked);
        List<String> admins = new ArrayList<String>();
        admins.add(Main.PROFILE.getUid());
        data.put("admins", admins);
        data.put("members", new ArrayList<String>());
        data.put("pending", new ArrayList<String>());
        String imgName = AppUtils.getRandomKey(20);
        data.put("img", imgName);
        data.put("clubBadge", "");

        byte[] imgBytes = AppUtils.getImgBytes(selectedImage, (int) activity.getResources().getDimension(R.dimen.banner_width),
                (int) activity.getResources().getDimension(R.dimen.banner_height), activity);
        AppUtils.uploadImg(imgName, imgBytes, "clubBanners/", new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful()){
                    DocumentReference doc = FirebaseFirestore.getInstance()
                            .collection("clubs").document();

                    doc.set(data).addOnCompleteListener(completeListener)
                            .addOnFailureListener(failureListener);

                    FirebaseFirestore.getInstance().collection("users")
                            .document(Main.PROFILE.getUid())
                            .update("clubs", FieldValue.arrayUnion(doc.getId()));

                    Main.PROFILE.addNotification(doc.getId(), null);
                }else{
                    failureListener.onFailure(task.getException());
                }
            }
        });
    }
}
