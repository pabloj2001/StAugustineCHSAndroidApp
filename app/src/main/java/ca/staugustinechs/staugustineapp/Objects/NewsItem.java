package ca.staugustinechs.staugustineapp.Objects;

public class NewsItem {

    String title;
    String blurb;

    public NewsItem(String title, String blurb){
        this.title = title;
        this.blurb = blurb;
    }

    public String getTitle() {
        return title;
    }

    public String getBlurb() {
        return blurb;
    }
}
