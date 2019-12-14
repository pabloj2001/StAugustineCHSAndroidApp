package ca.staugustinechs.staugustineapp.Objects;

import android.graphics.Bitmap;

public class CafMenuItem {

    private String name;
    private Bitmap image;
    private double price;

    public CafMenuItem(String name, double price, Bitmap img) {
        this.name = name;
        this.price = price;
        this.image = img;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public Bitmap getImage() {
        return image;
    }
}
