package ca.staugustinechs.staugustineapp.RVAdapters;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.Fragments.IconSelectFragment;
import ca.staugustinechs.staugustineapp.Objects.ProfileIcon;
import ca.staugustinechs.staugustineapp.R;

public class RViewAdapter_Icons extends RecyclerView.Adapter<RViewAdapter_Icons.ViewHolder>{

    private List<ProfileIcon> icons;
    private IconSelectFragment iconsSelectFragment;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        ImageView icon;

        public ViewHolder(View v) {
            super(v);
            icon = (ImageView) itemView.findViewById(R.id.cvIcon);
        }
    }

    public RViewAdapter_Icons(List<ProfileIcon> icons, IconSelectFragment iconsSelectFragment) {
        this.icons = icons;
        this.iconsSelectFragment = iconsSelectFragment;
    }

    @Override
    public RViewAdapter_Icons.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_icons, parent, false);
        RViewAdapter_Icons.ViewHolder vh = new RViewAdapter_Icons.ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RViewAdapter_Icons.ViewHolder holder, int position) {
        if(icons.get(position) != null && icons.get(position).getImg() != null){
            int px = AppUtils.getDimen(R.dimen.icon_select_size, iconsSelectFragment.getActivity());
            holder.icon.setImageBitmap(Bitmap.createScaledBitmap(icons.get(position).getImg(), px, px, false));
            holder.icon.setTag(icons.get(position).getId());
            holder.icon.setOnClickListener(iconsSelectFragment);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return icons.size();
    }

}
