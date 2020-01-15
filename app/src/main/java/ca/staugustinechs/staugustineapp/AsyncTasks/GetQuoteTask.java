package ca.staugustinechs.staugustineapp.AsyncTasks;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

import ca.staugustinechs.staugustineapp.Fragments.HomeFragment;

public class GetQuoteTask extends AsyncTask<String, Void, String> {

    private String quoteverse;
    private String quotescripture;
    private String quotedata;
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
                quoteverse = text.select("div.votd-box p").text();
                quotescripture = text.select("div.votd-box a[class]").text();
                quotedata = quoteverse + "\n" + quotescripture;

            }

        } catch (IOException e) { // catch IOException
            e.printStackTrace();
        }
        return quotedata;
    }

    @Override
    protected void onPostExecute(String quotestring) {
        homeFragment.updateQuote(quotestring);
    }


}
