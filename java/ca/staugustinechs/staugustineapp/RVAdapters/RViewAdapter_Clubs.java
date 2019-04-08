package ca.staugustinechs.staugustineapp.RVAdapters;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ca.staugustinechs.staugustineapp.Activities.ClubDetails;
import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.Objects.ClubItem;
import ca.staugustinechs.staugustineapp.R;

public class RViewAdapter_Clubs extends RecyclerView.Adapter<RViewAdapter_Clubs.ViewHolder> {
    private List<ClubItem> extraItems;
    private Activity activity;
    private boolean inProfile;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        ImageView extraImg;
        TextView extraName;
        RelativeLayout clubGroup;

        public ViewHolder(View v) {
            super(v);
            extraImg = (ImageView) itemView.findViewById(R.id.extraImg);
            extraImg.setBackgroundTintList(AppUtils.PRIMARY_COLORSL);
            extraName = (TextView) itemView.findViewById(R.id.extraName);
            extraName.setBackgroundColor(AppUtils.PRIMARY_COLOR);
            clubGroup = (RelativeLayout) itemView.findViewById(R.id.clubGroup);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public RViewAdapter_Clubs(List<ClubItem> extraItems, Activity activity, boolean inProfile) {
        this.activity = activity;
        this.extraItems = extraItems;
        this.inProfile = inProfile;

        //SORT CLUBS USER IS A PART OF BY ALPHABETICAL ORDER
        Collections.sort(this.extraItems, new Comparator<ClubItem>() {
            @Override
            public int compare(ClubItem o1, ClubItem o2) {
                for(int i = 0; i < o1.getName().length(); i++){
                    if(i == o2.getName().length()){
                        return 1;
                    }else if(i < o2.getName().length()){
                        if(o1.getName().charAt(i) < o2.getName().charAt(i)){
                            return -1;
                        }else if(o1.getName().charAt(i) > o2.getName().charAt(i)){
                            return 1;
                        }
                    }
                }
                return 0;
            }
        });
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RViewAdapter_Clubs.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_clubs, parent, false);
        RViewAdapter_Clubs.ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RViewAdapter_Clubs.ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        if(!inProfile){
            Bitmap bmp = extraItems.get(position).getImg();
            holder.extraImg.setImageBitmap(bmp);
        }else{
            holder.extraName.setTextSize(24f);
        }

        holder.extraName.setText(extraItems.get(position).getName());

        holder.clubGroup.setTag(position);

        holder.clubGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClubItem club = extraItems.get((int) v.getTag());
                club.pack();

                Intent intent = new Intent(activity, ClubDetails.class);
                intent.putExtra("club", club);
                activity.startActivity(intent);

                club.unpack(activity);
            }
        });
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return extraItems.size();
    }
}