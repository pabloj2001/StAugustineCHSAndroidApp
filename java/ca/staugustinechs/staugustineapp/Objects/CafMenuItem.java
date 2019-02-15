package ca.staugustinechs.staugustineapp.Objects;

import java.text.DecimalFormat;

public class CafMenuItem {

    private String name;
    private double price;

    public CafMenuItem(String name, double price){
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }
}
