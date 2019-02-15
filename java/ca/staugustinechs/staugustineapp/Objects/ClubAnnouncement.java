package ca.staugustinechs.staugustineapp.Objects;

import android.graphics.Bitmap;

import com.google.firebase.Timestamp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class ClubAnnouncement {

    String title, content, id, imgName, club, clubName;
    Date date;
    Bitmap img;

    public ClubAnnouncement(Map<String, Object> data, Bitmap img, String id){
        this.title = (String) data.get("title");
        this.content = (String) data.get("content");
        this.imgName = (String) data.get("img");
        this.date = ((Timestamp) data.get("date")).toDate();
        this.img = img;
        this.club = (String) data.get("club");
        this.clubName = (String) data.get("clubName");
        this.id = id;
    }

    public String getTitle(){
        return title;
    }

    public String getContent() {
        return content;
    }

    public Date getDate() {
        return date;
    }

    public Bitmap getImg() {
        return img;
    }

    public String getId(){
        return id;
    }

    public String getImgName(){
        return imgName;
    }

    public String getClub(){
        return club;
    }

    public String getClubName() {
        return clubName;
    }
}
