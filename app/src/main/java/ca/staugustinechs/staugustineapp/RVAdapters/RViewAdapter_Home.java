package ca.staugustinechs.staugustineapp.RVAdapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ca.staugustinechs.staugustineapp.Objects.NewsItem;
import ca.staugustinechs.staugustineapp.R;

public class RViewAdapter_Home extends RecyclerView.Adapter<RViewAdapter_Home.ViewHolder> {
    private List<NewsItem> newsItems;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView newsAuthor;
        TextView newsBlurb;

        public ViewHolder(View v) {
            super(v);
            newsAuthor = (TextView) itemView.findViewById(R.id.newsAuthor);
            newsBlurb = (TextView) itemView.findViewById(R.id.newsBlurb);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public RViewAdapter_Home(List<NewsItem> newsItems) {
        this.newsItems = newsItems;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RViewAdapter_Home.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_home, parent, false);
        RViewAdapter_Home.ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RViewAdapter_Home.ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.newsAuthor.setText(newsItems.get(position).getTitle());
        holder.newsBlurb.setText(newsItems.get(position).getBlurb());
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return newsItems.size();
    }
}