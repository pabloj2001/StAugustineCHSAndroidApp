package ca.staugustinechs.staugustineapp.RVAdapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ca.staugustinechs.staugustineapp.Activities.ClubDetails;
import ca.staugustinechs.staugustineapp.Activities.Profile;
import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.DialogFragments.BadgeSelectDialog;
import ca.staugustinechs.staugustineapp.Objects.Badge;
import ca.staugustinechs.staugustineapp.R;

public class RViewAdapter_Badges extends RecyclerView.Adapter<RViewAdapter_Badges.ViewHolder> {
    private List<Badge> badgeItems;
    private Activity activity;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        ImageView badgeImg;

        public ViewHolder(View v) {
            super(v);
            badgeImg = (ImageView) itemView.findViewById(R.id.badgeImg);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public RViewAdapter_Badges(List<Badge> badgeItems, Activity activity) {
        this.badgeItems = badgeItems;
        this.activity = activity;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RViewAdapter_Badges.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_badges, parent, false);
        RViewAdapter_Badges.ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RViewAdapter_Badges.ViewHolder holder, int position) {
        Bitmap img = badgeItems.get(position).getImg();
        if (img != null) {
            int size = AppUtils.getDimen(R.dimen.badge_size, activity);
            holder.badgeImg.setImageBitmap(Bitmap.createScaledBitmap(img, size, size, false));
        } else {
            holder.badgeImg.setVisibility(View.GONE);
        }

        holder.badgeImg.setTag(position);

        holder.badgeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Badge badge = badgeItems.get((int) v.getTag());
                BadgeSelectDialog dialog = new BadgeSelectDialog();
                dialog.setActivity(activity);
                dialog.setBadge(badge);
                if (activity instanceof ClubDetails) {
                    dialog.setIsAdmin(((ClubDetails) activity).isAdmin());
                    //dialog.setIsMemberBadge(((ClubDetails) activity).getClub().getClubBadge().equals(badge.getId()));
                    dialog.show(((ClubDetails) activity).getSupportFragmentManager(), "badgeSelectDialog");
                } else {
                    dialog.inProfiles(true);
                    dialog.show(((Profile) activity).getSupportFragmentManager(), "badgeSelectDialog");
                }
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
        return badgeItems.size();
    }
}