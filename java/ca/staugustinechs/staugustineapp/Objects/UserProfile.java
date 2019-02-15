package ca.staugustinechs.staugustineapp.Objects;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.staugustinechs.staugustineapp.Activities.Main;
import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.Fragments.SongsFragment;
import ca.staugustinechs.staugustineapp.MessagingService;

public class UserProfile {

    private String uid, name, email;
    private List<String> schedule, clubs, badges;
    private int profilePic, status, points, gradYear;
    private List<Integer> picsOwned;
    private boolean showClasses, showClubs;
    private String messagingToken;
    private ProfileIcon icon;
    private List<String> notifications;

    public UserProfile(String uid, Map<String, Object> data){
        this.uid = uid;
        email = (String) data.get("email");
        name = (String) data.get("name");
        schedule = (List<String>) data.get("classes");
        clubs = (List<String>) data.get("clubs");
        badges = (List<String>) data.get("badges");

        if(data.get("profilePic") instanceof Integer){
            profilePic = (int) data.get("profilePic");
        }else{
            profilePic = Math.toIntExact((Long) data.get("profilePic"));
        }

        picsOwned = new ArrayList<Integer>();
        if(((List) data.get("picsOwned")).get(0) instanceof Integer){
            for(Integer pic : (List<Integer>) data.get("picsOwned")){
                picsOwned.add(Math.toIntExact(pic));
            }
        }else{
            for(Long pic : (List<Long>) data.get("picsOwned")){
                picsOwned.add(Math.toIntExact(pic));
            }
        }

        showClasses = (boolean) data.get("showClasses");
        showClubs = (boolean) data.get("showClubs");

        if(data.get("status") instanceof Integer){
            status = (int) data.get("status");
        }else{
            status = Math.toIntExact((Long) data.get("status"));
        }

        Object pointsObj = data.get("points");
        if(pointsObj instanceof Long){
            points = Math.toIntExact((Long) pointsObj);
        }else if(pointsObj instanceof Double){
            points = (int) Math.round((Double) pointsObj);
        }else if(pointsObj instanceof Integer){
            points = (int) pointsObj;
        }

        if(data.containsKey("msgToken")){
            messagingToken = (String) data.get("msgToken");
        }

        if(data.containsKey("notifications")){
            notifications = (List<String>) data.get("notifications");
        }

        if(data.containsKey("gradYear")){
            Object gradYearObj = data.get("gradYear");
            if(gradYearObj instanceof Long){
                gradYear = Math.toIntExact((Long) gradYearObj);
            }else if(gradYearObj instanceof Double){
                gradYear = (int) Math.round((Double) gradYearObj);
            }else if(gradYearObj instanceof Integer){
                gradYear = (int) gradYearObj;
            }
        }else{
            gradYear = -1;
        }
    }

    public String getEmail(){
        return email;
    }

    public String getName() {
        return name;
    }

    public List<String> getSchedule() {
        return schedule;
    }

    public List<String> getClubs() {
        return clubs;
    }

    public List<String> getBadges() {
        return badges;
    }

    public int getProfilePic() {
        return profilePic;
    }

    public List<Integer> getPicsOwned() {
        return picsOwned;
    }

    public boolean showClasses() {
        return showClasses;
    }

    public boolean showClubs() {
        return showClubs;
    }

    public int getStatus(){
        return status;
    }

    public int getPoints(){
        return points;
    }

    public String getMessagingToken() {
        return messagingToken;
    }

    public ProfileIcon getIcon(){
        return icon;
    }

    public void setIcon(ProfileIcon icon){
        this.icon = icon;
        FirebaseFirestore.getInstance().collection("users")
                .document(this.getUid()).update("profilePic", icon.getId());
    }

    public String getUid() {
        return uid;
    }

    public void updatePoints(final int points, OnCompleteListener listener, OnFailureListener failureListener) {
        this.points += points;

        final DocumentReference ref = FirebaseFirestore.getInstance()
                .collection("users").document(this.getUid());
        FirebaseFirestore.getInstance().runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                double newPoints = transaction.get(ref).getDouble("points") + points;
                transaction.update(ref, "points", newPoints);
                return null;
            }
        });

        if(points > 0 && this.getGradYear() > 0){
            final DocumentReference ref2 = FirebaseFirestore.getInstance()
                    .collection("info").document("spiritPoints");
            FirebaseFirestore.getInstance().runTransaction(new Transaction.Function<Void>() {
                @Override
                public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                    double newPoints = transaction.get(ref2).getDouble(gradYear + "") + points;
                    transaction.update(ref2, gradYear + "", newPoints);
                    return null;
                }
            });
        }
    }

    public void setPrefs(boolean showClasses, boolean showClubs) {
        this.showClasses = showClasses;
        this.showClubs = showClubs;
    }

    public void setSchedule(List<String> classes){
        this.schedule = classes;
    }

    public List<String> getNotifications() {
        return notifications;
    }

    public int getGradYear(){
        return gradYear;
    }

    public void removeNotification(final String topic, final OnCompleteListener listener){
        MessagingService.unsubscribe(topic, new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful()){
                    notifications.remove(topic);
                }
                if(listener != null){
                    listener.onComplete(task);
                }
            }
        });
    }

    public void addNotification(final String topic, final OnCompleteListener listener){
        MessagingService.subscribe(topic, new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful()){
                    notifications.add(topic);
                }
                if(listener != null){
                    listener.onComplete(task);
                }
            }
        });
    }
}
