package ca.staugustinechs.staugustineapp.Objects;

import android.graphics.Bitmap;

import ca.staugustinechs.staugustineapp.AppUtils;

public class ProfileIcon {

    private int id;
    private Bitmap img;
    private int rarity, cost;
    private boolean owned;

    public ProfileIcon(int id, Bitmap img, int rarity, boolean owned){
        this.id = id;
        this.img = img;
        this.rarity = rarity;
        this.cost = AppUtils.PIC_COSTS != null ? AppUtils.PIC_COSTS[rarity] : 9999;
        this.owned = owned;
    }

    public int getId(){
        return id;
    }

    public Bitmap getImg() {
        return img;
    }

    public int getRarity(){
        return rarity;
    }

    public int getCost(){
        return cost;
    }

    public boolean isOwned() {
        return owned;
    }
}
