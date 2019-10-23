package ca.staugustinechs.staugustineapp.RVAdapters;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.staugustinechs.staugustineapp.Activities.Main;
import ca.staugustinechs.staugustineapp.DialogFragments.SuperVoteDialog;
import ca.staugustinechs.staugustineapp.Fragments.SongsFragment;
import ca.staugustinechs.staugustineapp.Objects.SongItem;
import ca.staugustinechs.staugustineapp.Objects.UserProfile;
import ca.staugustinechs.staugustineapp.R;

public class RViewAdapter_Songs extends RecyclerView.Adapter<RViewAdapter_Songs.ViewHolder> {
    private List<SongItem> songItems;
    private SongsFragment songsFragment;
    private boolean upvoting;
    private Map<String, UserProfile> userMap = new HashMap<String, UserProfile>();

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView songTitle;
        TextView songArtist;
        TextView songUpvotes;
        RelativeLayout upvoteGroup, songGroup;
        ImageView suggestorImg;
        TextView suggestor;

        public ViewHolder(View v) {
            super(v);
            songTitle = (TextView) itemView.findViewById(R.id.songTitle);
            songArtist = (TextView) itemView.findViewById(R.id.songArtist);
            songUpvotes = (TextView) itemView.findViewById(R.id.songUpvotes);
            upvoteGroup = (RelativeLayout) itemView.findViewById(R.id.upvoteGroup);
            songGroup = itemView.findViewById(R.id.songSongGroup);
            suggestorImg = itemView.findViewById(R.id.songSuggestorImg);
            suggestor = (TextView) itemView.findViewById(R.id.songSuggestor);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public RViewAdapter_Songs(List<SongItem> songItems, SongsFragment songsFragment) {
        this.songItems = songItems;
        this.songsFragment = songsFragment;

        loadSongUpvotes();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RViewAdapter_Songs.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_songs, parent, false);
        RViewAdapter_Songs.ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public synchronized void onBindViewHolder(final RViewAdapter_Songs.ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        SongItem songItem = songItems.get(position);

        holder.songTitle.setText(songItem.getTitle());
        holder.songArtist.setText(songItem.getArtist());
        holder.songUpvotes.setText(songItem.getUpvotes() + "");
        holder.upvoteGroup.setTag(position);

        if(songItem.isSuperVoted()){
            changeColors(holder.upvoteGroup, android.R.color.holo_red_light);
        }else if(songItem.isClicked()){
            changeColors(holder.upvoteGroup, android.R.color.holo_green_light);
        }else{
            if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                changeColors(holder.upvoteGroup, android.R.color.white);
            } else if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO) {
                changeColors(holder.upvoteGroup, android.R.color.black);
            }
            holder.upvoteGroup.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(Main.PROFILE.getPoints() >= 10){
                        SuperVoteDialog dialog = new SuperVoteDialog();
                        dialog.setSongsFragment(songsFragment);
                        dialog.setSong(songItems.get((int) v.getTag()));
                        dialog.show(songsFragment.getFragmentManager(), "superVoteDialog2");
                    }else{
                        Snackbar.make(v, "Sorry you don't have enough Points to super vote.",
                                Snackbar.LENGTH_LONG).show();
                    }

                    return true;
                }
            });
        }

        if(!songItem.isSuperVoted()){
            holder.upvoteGroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!upvoting){
                        //WE'RE IN THE PROCESS OF UPVOTING
                        upvoting = true;

                        //GET THE SONG USER WANTS TO UPVOTE/UNVOTE
                        SongItem songItem = songItems.get((int) v.getTag());
                        //UPVOTE OR UNVOTE IT
                        if(songItem.isClicked()){
                            upvote(songItem, -1, v);
                        }else{
                            upvote(songItem, 1, v);
                        }

                        //CHANGE TO TRANSITION COLOR
                        changeColors(v, android.R.color.holo_orange_light);
                    }
                }
            });
        }

        holder.songGroup.setTag(position);
        if(Main.PROFILE.getStatus() >= Main.TEACHER){
            holder.songGroup.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View v) {
                    if(songsFragment.getActivity() != null){
                        AlertDialog.Builder builder = new AlertDialog.Builder(songsFragment.getActivity());
                        //CREATE "DELETE SONG" BUTTON POP UP
                        builder.setItems(new String[]{"Delete Song"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(songsFragment.getActivity() != null){
                                    //CREATE ASK DELETE SONG DIALOG
                                    AlertDialog.Builder builder2 = new AlertDialog.Builder(songsFragment.getActivity());
                                    builder2.setTitle("Delete Song");
                                    builder2.setMessage("Do you wish to delete \"" +
                                            songItems.get((int) v.getTag()).getTitle() + "\"?");
                                    builder2.setNegativeButton("No!", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                                    builder2.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //DELETE THE SONG
                                            songsFragment.removeSong(songItems.get((int) v.getTag()).getId());
                                        }
                                    });
                                    builder2.create().show();
                                }
                            }
                        });
                        builder.create().show();
                        return true;
                    }
                    return false;
                }
            });
        }

        //DISPLAY THE USER THAT REQUESTED THE SONG IF AVAILABLE
        if(userMap.containsKey(songItem.getSuggestor())){
            UserProfile user = userMap.get(songItem.getSuggestor());
            holder.suggestorImg.setImageBitmap(user.getIcon().getImg());
            holder.suggestor.setText(user.getName());
            holder.suggestorImg.setVisibility(View.VISIBLE);
            holder.suggestor.setVisibility(View.VISIBLE);
        }
    }

    private void upvote(final SongItem songItem, final int mod, final View view){
        if(Main.PROFILE != null){
            final DocumentReference ref = FirebaseFirestore.getInstance()
                    .collection("songs").document(songItem.getId());
            FirebaseFirestore.getInstance().runTransaction(new Transaction.Function<Void>() {
                @Override
                public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                    double newUpvotes = transaction.get(ref).getDouble("upvotes")
                            + (Main.PROFILE.getStatus() == Main.DEV ? 5 * mod : mod);
                    transaction.update(ref, "upvotes", newUpvotes <= 0 ? 0 : newUpvotes);
                    return null;
                }
            }).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        if(mod > 0){
                            changeColors(view, android.R.color.holo_green_light);
                            songItem.setClicked(true);
                            songsFragment.UPVOTES.add(songItem.getId());
                            Snackbar.make(view, "You upvoted a song! :D", Snackbar.LENGTH_LONG).show();
                        }else{
                            changeColors(view, android.R.color.black);
                            songItem.setClicked(false);
                            songsFragment.UPVOTES.remove(songItem.getId());
                            Snackbar.make(view, "You unvoted a song", Snackbar.LENGTH_LONG).show();
                        }
                        //REFRESH
                        songsFragment.refresh();
                        //SAVE UPVOTED SONGS
                        songsFragment.saveUpvotes();
                    }else{
                        Snackbar.make(view, "Couldn't upvote right now :(", Snackbar.LENGTH_LONG).show();
                        changeColors(view, android.R.color.black);
                    }
                    //UPVOTE IS COMPLETE
                    upvoting = false;
                }
            });
        }
    }

    private void changeColors(View v, int color){
        ((ImageView) ((RelativeLayout) v).getChildAt(0)).getDrawable()
                .setTint(songsFragment.getActivity().getColor(color));
        ((TextView) ((RelativeLayout) v).getChildAt(1))
                .setTextColor(songsFragment.getActivity().getColor(color));
    }

    public void updateSongs(List<SongItem> songs){
        this.songItems = songs;
        loadSongUpvotes();
        this.notifyDataSetChanged();
    }

    public void addSuggestors(List<UserProfile> users) {
        if(users != null){
            for(UserProfile user : users){
                userMap.put(user.getUid(), user);
            }
            this.notifyDataSetChanged();
        }
    }

    public boolean cointainsSuggestor(String user){
        return userMap.containsKey(user);
    }

    public synchronized void loadSongUpvotes(){
        if(songItems != null && songItems.size() > 0){
            if(songsFragment.UPVOTES == null || songsFragment.SUPERVOTES == null){
                songsFragment.loadUpvotes(songItems);
            }

            for(SongItem song : songItems){
                if(songsFragment.SUPERVOTES.contains(song.getId())){
                    song.setSuperVoted(true);
                }else if(songsFragment.UPVOTES.contains(song.getId())){
                    song.setClicked(true);
                }else{
                    song.setSuperVoted(false);
                    song.setClicked(false);
                }
            }

            boolean exists = false;
            for(String songId : songsFragment.SUPERVOTES){
                for(SongItem song : songItems){
                    if(song.getId().equals(songId)){
                        exists = true;
                        break;
                    }
                }

                if(!exists){
                    songsFragment.SUPERVOTES.remove(songId);
                }
            }

            exists = false;
            for(String songId : songsFragment.UPVOTES){
                for(SongItem song : songItems){
                    if(song.getId().equals(songId)){
                        exists = true;
                        break;
                    }
                }

                if(!exists){
                    songsFragment.UPVOTES.remove(songId);
                }
            }
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return songItems.size();
    }
}