package ca.staugustinechs.staugustineapp.RVAdapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ca.staugustinechs.staugustineapp.Objects.CafMenuItem;
import ca.staugustinechs.staugustineapp.R;

public class RViewAdapter_CafMenu extends RecyclerView.Adapter<RViewAdapter_CafMenu.ViewHolder> {
    private List<CafMenuItem> items;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView cmItemName;
        TextView cmItemPrice;
        ImageView cmImage;

        public ViewHolder(View v) {
            super(v);
            cmItemName = (TextView) itemView.findViewById(R.id.cmItemName);
            cmItemPrice = (TextView) itemView.findViewById(R.id.cmItemPrice);
            cmImage = itemView.findViewById(R.id.cmImage);
        }
    }

    public RViewAdapter_CafMenu(List<CafMenuItem> items) {
        this.items = items;
        Collections.sort(this.items, new Comparator<CafMenuItem>() {
            @Override
            public int compare(CafMenuItem o1, CafMenuItem o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
    }

    @NonNull
    @Override
    public RViewAdapter_CafMenu.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_cafitem, parent, false);
        RViewAdapter_CafMenu.ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final RViewAdapter_CafMenu.ViewHolder holder, final int position) {
        holder.cmItemName.setText(items.get(position).getName());
        holder.cmImage.setImageBitmap(items.get(position).getImage());

        DecimalFormat format = new DecimalFormat("0.00");
        String strPrice = format.format(items.get(position).getPrice());

        holder.cmItemPrice.setText("$" + strPrice);
        //holder.cmItemPrice.setTextColor(AppUtils.PRIMARY_COLOR);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return items.size();
    }
}