package ca.staugustinechs.staugustineapp.Objects;

import com.google.firebase.FirebaseApp;

import java.util.Map;

public class SongItem {

    private String id;
    private String title;
    private String artist;
    private int upvotes;
    private boolean clicked, superVoted;

    public SongItem(String id, Map<String, Object> data){
        this.title = (String) data.get("name");
        this.artist = (String) data.get("artist");
        Object upv = data.get("upvotes");
        if(upv instanceof Long){
            this.upvotes = (int) Math.toIntExact((Long) data.get("upvotes"));
        }else if(upv instanceof Double){
            this.upvotes = (int) Math.round((Double) data.get("upvotes"));
        }
        this.id = id;
    }

    public String getId(){
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public int getUpvotes() {
        return upvotes;
    }

    public boolean isClicked(){
        return clicked;
    }

    public void setClicked(boolean clicked){
        this.clicked = clicked;
    }

    public boolean isSuperVoted() {
        return superVoted;
    }

    public void setSuperVoted(boolean superVoted){
        this.superVoted = superVoted;
    }
}
