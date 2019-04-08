package ca.staugustinechs.staugustineapp.RVAdapters;

import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.staugustinechs.staugustineapp.Activities.Main;
import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.DialogFragments.SuperVoteDialog;
import ca.staugustinechs.staugustineapp.Fragments.SongsFragment;
import ca.staugustinechs.staugustineapp.Objects.CafMenuItem;
import ca.staugustinechs.staugustineapp.Objects.SongItem;
import ca.staugustinechs.staugustineapp.R;

public class RViewAdapter_CafMenu extends RecyclerView.Adapter<RViewAdapter_CafMenu.ViewHolder> {
    private List<CafMenuItem> items;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView cmItemName;
        TextView cmItemPrice;

        public ViewHolder(View v) {
            super(v);
            cmItemName = (TextView) itemView.findViewById(R.id.cmItemName);
            cmItemPrice = (TextView) itemView.findViewById(R.id.cmItemPrice);
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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_cafmenu, parent, false);
        RViewAdapter_CafMenu.ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final RViewAdapter_CafMenu.ViewHolder holder, final int position) {
        holder.cmItemName.setText(items.get(position).getName());

        DecimalFormat format = new DecimalFormat("0.00");
        String strPrice = format.format(items.get(position).getPrice());

        holder.cmItemPrice.setText("$" + strPrice);
        holder.cmItemPrice.setTextColor(AppUtils.PRIMARY_COLOR);
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