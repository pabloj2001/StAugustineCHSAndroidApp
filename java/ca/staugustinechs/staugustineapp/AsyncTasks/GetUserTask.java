package ca.staugustinechs.staugustineapp.AsyncTasks;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ca.staugustinechs.staugustineapp.Activities.Profile;
import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.Objects.ProfileIcon;
import ca.staugustinechs.staugustineapp.Objects.UserProfile;
import ca.staugustinechs.staugustineapp.Interfaces.UserGetter;

public class GetUserTask extends AsyncTask<String, Void, List<UserProfile>> implements OnFailureListener {

    private Activity activity;
    private String email;
    private List<String> users;
    private boolean getImg = true;

    public GetUserTask(Activity activity, String email){
        this.activity = activity;
        this.email = email;
    }

    public GetUserTask(Activity activity, List<String> users){
        this.activity = activity;
        this.users = users;
    }

    public void dontGetImg(){
        this.getImg = false;
    }

    @Override
    protected List<UserProfile> doInBackground(String... strings) {
        if(AppUtils.isNetworkAvailable(activity)){
            if(users != null && !users.isEmpty()){
                return getUsers();
            }else{
                return getUser();
            }
        }

        return null;
    }

    private List<UserProfile> getUser(){
        Task<QuerySnapshot> userTask = FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("email", email).get();

        while (!userTask.isComplete()) { }

        if(userTask.isSuccessful()){
            List<DocumentSnapshot> docs = userTask.getResult().getDocuments();
            if(docs.size() > 0){
                UserProfile user = new UserProfile(docs.get(0).getId(), docs.get(0).getData());

                if(getImg){
                    GetProfileIconsTask getProfileIconsTask = new GetProfileIconsTask(activity,
                            GetProfileIconsTask.IconGetType.SINGLE);
                    List<ProfileIcon> icons = getProfileIconsTask.getProfileIcons(new Integer[]{user.getProfilePic()});
                    user.setIcon(icons.get(0));
                }

                return Arrays.asList(user);
            }
        }

        return null;
    }

    private List<UserProfile> getUsers(){
        List<UserProfile> userList = new ArrayList<UserProfile>();
        List<Integer> pics = new ArrayList<Integer>();

        if(!users.isEmpty()) {
            for (String userId : users) {
                if(userId != null && !userId.isEmpty()){
                    Task<DocumentSnapshot> userTask = FirebaseFirestore.getInstance()
                            .collection("users").document(userId).get();

                    while (!userTask.isComplete()) { }

                    if (userTask.isSuccessful()) {
                        DocumentSnapshot doc = userTask.getResult();
                        if(doc.exists()){
                            UserProfile user = new UserProfile(doc.getId(), doc.getData());
                            pics.add(user.getProfilePic());
                            userList.add(user);
                        }
                    }
                }
            }

            if(getImg && !userList.isEmpty() && pics.size() > 0){
                GetProfileIconsTask getProfileIconsTask = new GetProfileIconsTask(activity,
                        GetProfileIconsTask.IconGetType.MULTIPLE);
                List<ProfileIcon> icons = getProfileIconsTask.getProfileIcons(pics.toArray(new Integer[]{}));

                for (int a = 0; a < userList.size(); a++) {
                    userList.get(a).setIcon(icons.get(a));
                }
            }
        }

        return userList;
    }

    @Override
    protected void onPostExecute(List<UserProfile> users) {
        if (!this.isCancelled()) {
            if(activity instanceof UserGetter){
                ((UserGetter) activity).updateProfile(users);
            }
        }
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        this.cancel(true);
    }
}
