package ca.staugustinechs.staugustineapp.AsyncTasks;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import ca.staugustinechs.staugustineapp.Fragments.CafMenuFragment;
import ca.staugustinechs.staugustineapp.Objects.CafMenuItem;

public class GetCafMenuTask extends AsyncTask<String, Void, List<CafMenuItem>> {

    private CafMenuFragment cafMenuFragment;
    private boolean dailyMenu;

    public GetCafMenuTask(CafMenuFragment cafMenuFragment, boolean dailyMenu){
        this.cafMenuFragment = cafMenuFragment;
        //USED TO KNOW WHETHER TO FETCH THE REGULAR OR DAILY MENU
        this.dailyMenu = dailyMenu;
    }

    @Override
    protected List<CafMenuItem> doInBackground(String... strings) {
        //GET CAF MENU DEPENDING ON WHETHER WE'RE GETTING THE DAILY MENU OR REGULAR MENU
        Task<DocumentSnapshot> task = FirebaseFirestore.getInstance().collection("info")
                .document(dailyMenu ? "cafMenu" : "cafMenuRegular").get();

        Task<DocumentSnapshot> imageTask = FirebaseFirestore.getInstance().collection("info")
                .document("cafMenuImages").get();

        while (!task.isComplete()) {
        }

        while (!imageTask.isComplete()) {
        }

        if (task.isSuccessful() && imageTask.isSuccessful()) {
            Map<String, Object> cafImages = Objects.requireNonNull(imageTask.getResult()).getData();
            String itemName;
            List<CafMenuItem> items = new ArrayList<>();
            for (Map.Entry<String, Object> entry : (Objects.requireNonNull(Objects.requireNonNull(task.getResult()).getData())).entrySet()) {
                itemName = entry.getKey().trim();
                assert cafImages != null;
                boolean hasImage = cafImages.containsKey(itemName) &&
                        !((String) Objects.requireNonNull(cafImages.get(itemName))).equalsIgnoreCase("");
                String imgUrl = hasImage ?
                        ((String) cafImages.get(itemName)) : "https://i.kym-cdn.com/photos/images/original/001/067/012/a30.jpg_large";
                Bitmap img = null;
                try {
                    img = Picasso.get().load(imgUrl).resize(400, 300).centerCrop().get();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //GO THROUGH EACH ENTRY PAIR (KEY: ITEM NAME, VALUE: PRICE)
                //CREATE EACH CAF MENU ITEM AND SAVE THEM INTO THE ARRAY
                items.add(new CafMenuItem(entry.getKey(),
                        entry.getValue() instanceof Long ? (long) entry.getValue() : (double) entry.getValue(), img));
            }
            return items;
        }

        return null;
    }

    @Override
    protected void onPostExecute(List<CafMenuItem> items) {
        if(!this.isCancelled()){
            //RETURN ITEMS TO CAF MENU FRAGMENT
            cafMenuFragment.updateMenu(items, dailyMenu);
        }
    }
}
