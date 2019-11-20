package ca.staugustinechs.staugustineapp.AsyncTasks;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

import ca.staugustinechs.staugustineapp.Fragments.HomeFragment;

public class GetQuoteTask extends AsyncTask<String, Void, String> {

    private String method;
    private String title;
    private HomeFragment homeFragment;

    public GetQuoteTask(HomeFragment hfrag) {
        this.homeFragment = hfrag;
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            Document doc = Jsoup.connect("https://www.biblegateway.com/").get(); // connects

            // elements takes more than 1, element is only 1
            Elements texts = doc.select("div.votd-verse-component");

            for (Element text: texts) {
                method = text.select("div.votd-box p").text();
                title = texts.select("div.votd-box a[class]").text();
            }

        } catch (IOException e) { // catch IOException
            e.printStackTrace();
        }
        return method;
    }

    @Override
    protected void onPostExecute(String quotestring) {
        homeFragment.updateQuote(quotestring);
    }


}
