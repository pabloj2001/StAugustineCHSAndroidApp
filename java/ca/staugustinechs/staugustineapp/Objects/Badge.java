package ca.staugustinechs.staugustineapp.Objects;

import android.graphics.Bitmap;

import java.util.Map;

public class Badge {

    private String id, club, desc, imgName;
    private Bitmap img;
    private boolean giveaway;

    public Badge(String id, Map<String, Object> data, String imgName, Bitmap img){
        this.id = id;
        this.club = (String) data.get("club");
        this.desc = (String) data.get("desc");
        this.img = img;
        this.giveaway = (Boolean) data.get("giveaway");
        this.imgName = imgName;
    }

    public String getId(){
        return id;
    }

    public String getClub() {
        return club;
    }

    public String getDesc() {
        return desc;
    }

    public Bitmap getImg() {
        return img;
    }

    public boolean canGiveaway(){
        return giveaway;
    }

    public String getImgName() {
        return imgName;
    }
}
