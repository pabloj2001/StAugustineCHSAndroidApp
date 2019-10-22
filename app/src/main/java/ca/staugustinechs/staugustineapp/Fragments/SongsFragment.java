package ca.staugustinechs.staugustineapp.Fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.staugustinechs.staugustineapp.Activities.Main;
import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.AsyncTasks.GetSongsTask;
import ca.staugustinechs.staugustineapp.AsyncTasks.GetUserTask;
import ca.staugustinechs.staugustineapp.DialogFragments.RequestSongDialog;
import ca.staugustinechs.staugustineapp.Objects.SongItem;
import ca.staugustinechs.staugustineapp.Objects.UserProfile;
import ca.staugustinechs.staugustineapp.R;
import ca.staugustinechs.staugustineapp.RVAdapters.RViewAdapter_Songs;

public class SongsFragment extends Fragment implements View.OnClickListener, OnFailureListener, SwipeRefreshLayout.OnRefreshListener {

    public final String UPVOTES_FILE = "upvotes.dat";
    public List<String> UPVOTES = null, SUPERVOTES = null;

    private GetSongsTask songsTask;
    private View offline;
    private ProgressBar loadingCircle;
    private RelativeLayout layout;
    private View songsGroup;
    private RecyclerView rv;
    private SwipeRefreshLayout refreshLayout;
    private FloatingActionButton requestSongBtn;
    private RViewAdapter_Songs adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_songs, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        offline = getLayoutInflater().inflate(R.layout.offline_layout, null);
        layout = getView().findViewById(R.id.songsLayout);
        loadingCircle = getView().findViewById(R.id.songsLoadingCircle);
        loadingCircle.getIndeterminateDrawable().setTint(AppUtils.ACCENT_COLOR);
        refreshLayout = getView().findViewById(R.id.songsRefresh);
        refreshLayout.setColorSchemeColors(AppUtils.ACCENT_COLOR);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setEnabled(false);
        songsGroup = getView().findViewById(R.id.songsGroup);
        requestSongBtn = (FloatingActionButton) getView().findViewById(R.id.addSong);
        requestSongBtn.setBackgroundTintList(AppUtils.ACCENT_COLORSL);

        //IF SONG THEME EXISTS SHOW THEME BANNER
        if(AppUtils.SONG_REQUEST_THEME != null && !AppUtils.SONG_REQUEST_THEME.isEmpty()){
            TextView theme = getView().findViewById(R.id.songsTheme);
            theme.setText("THEME: " + AppUtils.SONG_REQUEST_THEME);
            theme.setTextColor(AppUtils.ACCENT_COLOR);
            theme.setBackgroundColor(AppUtils.PRIMARY_DARK_COLOR);

            refreshLayout.setPadding(0, 24, 0, 0);
            theme.setVisibility(View.VISIBLE);
        }

        if(AppUtils.isNetworkAvailable(this.getActivity())){
            rv = (RecyclerView) getView().findViewById(R.id.rv);
            rv.setHasFixedSize(true);

            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext()){
                @Override
                public boolean canScrollVertically(){
                    return false;
                }
            };
            rv.setLayoutManager(layoutManager);

            //GET ALL SONGS FROM DB
            songsTask = new GetSongsTask(this);
            songsTask.execute();

            requestSongBtn.setOnClickListener(this);
        }else{
            setOffline();
        }
    }

    @Override
    public void onClick(View v) {
        if(v.equals(requestSongBtn)){
            if(rv.getAdapter().getItemCount() < AppUtils.MAX_SONGS || Main.PROFILE.getStatus() == 2){
                if(Main.PROFILE.getPoints() >= 10){
                    RequestSongDialog dialog = new RequestSongDialog();
                    dialog.setSongsFragment(this);
                    dialog.show(this.getFragmentManager(), "requestSongDialog");
                }else{
                    Snackbar.make(getView(), "Sorry you don't have enough Points to request a song.",
                            Snackbar.LENGTH_LONG).show();
                }
            }else{
                Snackbar.make(getView(), "Sorry there can only be " + AppUtils.MAX_SONGS + " songs requested at once.",
                        Snackbar.LENGTH_LONG).show();
            }
        }
    }

    public synchronized void updateSongs(List<SongItem> songs) {
        if(songs != null){
            if(adapter == null){
                adapter = new RViewAdapter_Songs(songs, this);
                if(rv != null){
                    rv.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
            }else{
                adapter.updateSongs(songs);
            }

            if(AppUtils.SHOW_USERS_ON_SONGS){
                //GET USERS FOR EACH SONG
                List<String> users = new ArrayList<String>();
                for(SongItem song : songs){
                    if(!adapter.cointainsSuggestor(song.getSuggestor())){
                        users.add(song.getSuggestor());
                    }
                }

                if(users.size() > 0){
                    refreshLayout.setEnabled(false);
                    refreshLayout.setRefreshing(true);
                    //ONCE AGAIN DON'T DO THIS
                    @SuppressLint("StaticFieldLeak")
                    GetUserTask task = new GetUserTask(this.getActivity(), users){
                        @Override
                        protected void onPostExecute(List<UserProfile> users) {
                            adapter.addSuggestors(users);
                            refreshLayout.setEnabled(true);
                            refreshLayout.setRefreshing(false);
                        }
                    };
                    task.execute();
                }else{
                    refreshLayout.setEnabled(true);
                    refreshLayout.setRefreshing(false);
                }
            }else{
                refreshLayout.setEnabled(true);
                refreshLayout.setRefreshing(false);
            }

            View error = getView().findViewById(R.id.songsError);
            if(songs.size() > 0){
                //MAKE SONGS VISIBLE
                songsGroup.setVisibility(View.VISIBLE);
                //MAKE ERROR INVISIBLE
                error.setVisibility(View.GONE);
            }else{
                songsGroup.setVisibility(View.GONE);
                error.setVisibility(View.VISIBLE);
            }

            //MAKE OTHER VIEWS INVISIBLE
            loadingCircle.setVisibility(View.GONE);
            layout.removeView(offline);
            //MAKE BUTTON VISIBLE
            ((View) requestSongBtn).setVisibility(View.VISIBLE);
        }else{
            setOffline();
        }
    }

    private void setOffline(){
        loadingCircle.setVisibility(View.GONE);
        songsGroup.setVisibility(View.GONE);
        ((View) requestSongBtn).setVisibility(View.GONE);
       // ((View) superVoteBtn).setVisibility(View.GONE);

        refreshLayout.setRefreshing(false);
        refreshLayout.setEnabled(true);

        layout.removeView(offline);
        layout.addView(offline);
    }

    public void requestSong(final String title, final String artist) {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("name", title);
        data.put("artist", artist);
        data.put("suggestor", FirebaseAuth.getInstance().getUid());
        data.put("date", new Timestamp(new Date()));
        data.put("upvotes", 0);

        Main.PROFILE.updatePoints(-AppUtils.REQUEST_SONG_COST, false, null, SongsFragment.this);

        FirebaseFirestore.getInstance().collection("songs").add(data)
                .addOnFailureListener(SongsFragment.this)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if(getView() != null){
                            if(task.isSuccessful()){
                                Snackbar.make(getView(), "Succesfully requested song!",
                                        Snackbar.LENGTH_LONG).show();
                                refresh();
                            }else{
                                Snackbar.make(getView(), "Unable to request song",
                                        Snackbar.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    public void superVote(final int cost, final int vote, final String songId){
        final DocumentReference ref = FirebaseFirestore.getInstance()
                .collection("songs").document(songId);
        FirebaseFirestore.getInstance().runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                double newUpvotes = transaction.get(ref).getDouble("upvotes") + vote;
                transaction.update(ref, "upvotes", newUpvotes);
                return null;
            }
        }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Main.PROFILE.updatePoints(-cost, false, null, null);

                    if (getView() != null) {
                        Snackbar.make(getView(), "You super voted a song! :D", Snackbar.LENGTH_LONG).show();
                    }

                    SUPERVOTES.add(songId);
                    refresh();
                    saveUpvotes();
                } else {
                    if (getView() != null) {
                        Snackbar.make(getView(), "Couldn't super vote right now :(", Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    public void removeSong(String songId) {
        FirebaseFirestore.getInstance().collection("songs")
                .document(songId).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    onRefresh();
                }
            }
        });
    }

    public synchronized void loadUpvotes(List<SongItem> songItems){
        File file = new File(this.getActivity().getFilesDir(), UPVOTES_FILE);
        if (file.exists()) {
            Map<String, String> data = AppUtils.loadMapFile(UPVOTES_FILE, this.getActivity());
            if(data == null || data.isEmpty()){
                file.delete();
                SUPERVOTES = new ArrayList<String>();
                UPVOTES = new ArrayList<String>();
                return;
            }

            SUPERVOTES = new ArrayList<String>();
            String superVotes = data.get("superVotes");
            if(superVotes != null){
                for(String songId : superVotes.split(";")) {
                    if(songId != null && !songId.isEmpty() && !songId.equals("null")){
                        for(int i = 0; i < songItems.size(); i++){
                            if(songItems.get(i).getId().equals(songId)){
                                SUPERVOTES.add(songId);
                                break;
                            }
                        }
                    }
                }
            }

            UPVOTES = new ArrayList<String>();
            String upvotes = data.get("upvotes");
            if(upvotes != null){
                for(String songId : upvotes.split(";")) {
                    if(songId != null && !songId.isEmpty() && !songId.equals("null")){
                        for(int i = 0; i < songItems.size(); i++){
                            if(songItems.get(i).getId().equals(songId)){
                                UPVOTES.add(songId);
                                break;
                            }
                        }
                    }
                }
            }
        } else {
            SUPERVOTES = new ArrayList<String>();
            UPVOTES = new ArrayList<String>();
        }
    }

    public synchronized void saveUpvotes(){
        Map<String, String> data = new HashMap<String, String>();
        if (UPVOTES != null && UPVOTES.size() > 0) {
            String upvotesData = AppUtils.combineWithRegex(UPVOTES.toArray(new String[]{}));
            data.put("upvotes", upvotesData);
        }

        if (SUPERVOTES != null && SUPERVOTES.size() > 0) {
            String superVotesData = AppUtils.combineWithRegex(SUPERVOTES.toArray(new String[]{}));
            data.put("superVotes", superVotesData);
        }

        if(!data.isEmpty()){
            AppUtils.saveMapFile(UPVOTES_FILE, data, this.getActivity());
        }
    }

    private void saveAndExit(){
        if(songsTask != null){
            songsTask.cancel(true);
        }

        this.saveUpvotes();
    }

    @Override
    public void onDestroy(){
        saveAndExit();
        super.onDestroy();
    }

    @Override
    public void onPause(){
        saveAndExit();
        super.onPause();
    }

    @Override
    public void onDetach(){
        saveAndExit();
        super.onDetach();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if(!hidden){
            onRefresh();
        }else{
            saveAndExit();
        }
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        e.printStackTrace();
    }

    public void refresh(){
        refreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                onRefresh();
            }
        }, 600L);
    }

    @Override
    public void onRefresh() {
        refreshLayout.setRefreshing(true);
        songsTask = new GetSongsTask(SongsFragment.this);
        songsTask.execute();
    }
}
