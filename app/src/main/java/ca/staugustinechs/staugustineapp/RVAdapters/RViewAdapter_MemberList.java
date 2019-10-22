package ca.staugustinechs.staugustineapp.RVAdapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ca.staugustinechs.staugustineapp.Activities.ClubMemberList;
import ca.staugustinechs.staugustineapp.Activities.Main;
import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.DialogFragments.EditClubMemberDialog;
import ca.staugustinechs.staugustineapp.Objects.UserProfile;
import ca.staugustinechs.staugustineapp.R;

public class RViewAdapter_MemberList extends RecyclerView.Adapter<RViewAdapter_MemberList.ViewHolder> {
    private List<UserProfile> members;
    private int mode;
    private boolean isAdmin;
    private ClubMemberList clubMemberList;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        View cmlGroup;
        ImageView userImg;
        TextView userName;
        TextView userEmail;

        public ViewHolder(View v) {
            super(v);
            cmlGroup = itemView.findViewById(R.id.cmlGroup);
            userImg = itemView.findViewById(R.id.cmlUserImg);
            userName = (TextView) itemView.findViewById(R.id.cmlUserName);
            userEmail = (TextView) itemView.findViewById(R.id.cmlUserEmail);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public RViewAdapter_MemberList(List<UserProfile> members, int mode, boolean isAdmin,
                                   ClubMemberList clubMemberList) {
        this.members = members;
        this.mode = mode;
        this.isAdmin = isAdmin;
        this.clubMemberList = clubMemberList;

        //SORT MEMBERS BY ALPHABETICAL ORDER
        Collections.sort(this.members, new Comparator<UserProfile>() {
            @Override
            public int compare(UserProfile o1, UserProfile o2) {
                if(o1 != null && o2 != null) {
                    for (int i = 0; i < o1.getName().length(); i++) {
                        if (i == o2.getName().length()) {
                            return 1;
                        } else if (i < o2.getName().length()) {
                            if (o1.getName().charAt(i) < o2.getName().charAt(i)) {
                                return -1;
                            } else if (o1.getName().charAt(i) > o2.getName().charAt(i)) {
                                return 1;
                            }
                        }
                    }
                }
                return 0;
            }
        });
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RViewAdapter_MemberList.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_memberlist, parent, false);
        RViewAdapter_MemberList.ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final RViewAdapter_MemberList.ViewHolder holder, final int position) {
        if(members.get(position) != null) {
            int px = AppUtils.getDeviceDimen(R.dimen.icon_small_size, clubMemberList);
            Bitmap bmp = Bitmap.createScaledBitmap(members.get(position).getIcon().getImg(),
                    px, px, false);
            holder.userImg.setImageBitmap(bmp);

            holder.userName.setText(members.get(position).getName());
            holder.userEmail.setText(members.get(position).getEmail());

            holder.cmlGroup.setTag(position);
            if (!members.get(position).getEmail().equals(Main.PROFILE.getEmail()) || Main.PROFILE.getStatus() == Main.DEV) {
                holder.cmlGroup.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if ((mode != ClubMemberList.ADMINS && isAdmin) ||
                                (mode == ClubMemberList.ADMINS && isAdmin && Main.PROFILE.getStatus() >= Main.TEACHER)) {
                            EditClubMemberDialog dialog = new EditClubMemberDialog();
                            dialog.setClubMemberList(clubMemberList);
                            dialog.setMode(mode);
                            dialog.setUserProfile(members.get((int) v.getTag()));
                            dialog.show(clubMemberList.getSupportFragmentManager(), "EditClubMemberDialog");
                            return true;
                        } else {
                            return false;
                        }
                    }
                });

                holder.cmlGroup.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //COPY USER'S EMAIL TO CLIPBOARD
                        ClipboardManager clipboard = (ClipboardManager)
                                clubMemberList.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("USER EMAIL", holder.userEmail.getText());
                        clipboard.setPrimaryClip(clip);

                        Snackbar.make(holder.cmlGroup, "Copied User's Email to Clipboard!", Snackbar.LENGTH_LONG).show();
                    }
                });
            }
            /*holder.cmlGroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(clubMemberList, Profile.class);
                    intent.putExtra("USER_EMAIL", members.get((int) v.getTag()).getEmail());
                    clubMemberList.startActivity(intent);
                }
            });*/
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void addItem(UserProfile user){
        if(!this.members.contains(user)){
            this.members.add(user);
        }
    }

    public void removeItem(UserProfile user){
        if(this.members.contains(user)){
            this.members.remove(user);
        }
    }

    @Override
    public int getItemCount() {
        return members.size();
    }
}