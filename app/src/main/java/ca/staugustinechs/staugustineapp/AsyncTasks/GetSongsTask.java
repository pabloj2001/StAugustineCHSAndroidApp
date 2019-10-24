package ca.staugustinechs.staugustineapp.AsyncTasks;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.Fragments.SongsFragment;
import ca.staugustinechs.staugustineapp.Objects.SongItem;

public class GetSongsTask extends AsyncTask<String, Void, List<SongItem>> implements OnFailureListener{

    SongsFragment songsFragment;

    public GetSongsTask(SongsFragment songsFragment){
        this.songsFragment = songsFragment;
    }

    @Override
    protected List<SongItem> doInBackground(String... strings) {
        if(AppUtils.isNetworkAvailable(songsFragment.getActivity())){
            Task<QuerySnapshot> task = FirebaseFirestore.getInstance()
                    .collection("songs")
                    .get().addOnFailureListener(this);

            while(!task.isComplete()){ }

            if(task.isSuccessful()){
                List<SongItem> songs = new ArrayList<SongItem>();
                for(DocumentSnapshot doc : task.getResult().getDocuments()){
                    songs.add(new SongItem(doc.getId(), doc.getData()));
                }

                Collections.sort(songs, new Comparator<SongItem>() {
                    @Override
                    public int compare(SongItem o1, SongItem o2) {
                        if(o1.getUpvotes() > o2.getUpvotes()){
                            return -1;
                        }else{
                            return 1;
                        }
                    }
                });

                return songs;
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(List<SongItem> s) {
        if(!this.isCancelled()){
            songsFragment.updateSongs(s);
        }
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        this.cancel(true);
    }
}
