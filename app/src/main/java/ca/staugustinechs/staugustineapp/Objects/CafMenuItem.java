package ca.staugustinechs.staugustineapp.Objects;

public class CafMenuItem {

    private String name;
    //private Bitmap image;
    private double price;

    public CafMenuItem(String name, double price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    //public Bitmap getImage() {
    //  return image;
    //}
}
