package ca.staugustinechs.staugustineapp.DialogFragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.internal.BaselineLayout;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import ca.staugustinechs.staugustineapp.Activities.BadgeScanner;
import ca.staugustinechs.staugustineapp.Activities.ClubDetails;
import ca.staugustinechs.staugustineapp.Activities.Login;
import ca.staugustinechs.staugustineapp.Activities.Main;
import ca.staugustinechs.staugustineapp.Fragments.IconSelectFragment;
import ca.staugustinechs.staugustineapp.Objects.Badge;
import ca.staugustinechs.staugustineapp.Objects.ProfileIcon;
import ca.staugustinechs.staugustineapp.Objects.UserProfile;

public class BadgeSelectDialog extends DialogFragment {

    private Activity activity;
    private boolean isAdmin, inProfiles = false;
    private Badge badge;

    public BadgeSelectDialog(){

    }

    public void setActivity(Activity activity){
        this.activity = activity;
    }

    public void setIsAdmin(boolean isAdmin){
        this.isAdmin = isAdmin;
    }

    public void inProfiles(boolean inProfiles){
        this.inProfiles = inProfiles;
    }

    public void setBadge(Badge badge){
        this.badge = badge;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //BUILD A VIEW
        builder.setView(createView());

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        return builder.create();
    }

    private LinearLayout createView(){
        LinearLayout layout = new LinearLayout(this.getActivity());
        layout.setPadding(8, 8, 8, 0);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setBaselineAligned(false);
        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        ImageView img = new ImageView(this.getActivity());
        img.setId(View.generateViewId());
        img.setImageBitmap(badge.getImg());
        img.setPadding(0, 42, 0, 0);
        img.setForegroundGravity(Gravity.CENTER_HORIZONTAL);
        img.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.addView(img);

        TextView desc = new TextView(this.getActivity());
        desc.setId(View.generateViewId());
        desc.setText(badge.getDesc());
        desc.setTextSize(18);
        desc.setPadding(0, 24, 0, 16);
        desc.setGravity(Gravity.CENTER_HORIZONTAL);
        desc.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        desc.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.addView(desc);

        if(inProfiles){
            //NEED TO GET CLUB NAME HERE
            /*TextView club = new TextView(this.getActivity());
            club.setId(View.generateViewId());
            club.setText(badge.getClub());
            club.setTextSize(14);
            club.setPadding(0, 16, 0, 0);
            club.setGravity(Gravity.CENTER_HORIZONTAL);
            club.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
            club.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            layout.addView(club);*/
        }else if(isAdmin){
            /*if(isMemberBadge){
                Button edit = new Button(this.getActivity());
                edit.setId(View.generateViewId());
                edit.setText("EDIT");
                edit.setTextSize(21);
               // edit.setPadding(0, 46, 0, 0);
                edit.setGravity(Gravity.CENTER_HORIZONTAL);
                edit.setTextAlignment(TextView.TEXT_ALIGNMENT_GRAVITY);
                edit.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

                edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });

                layout.addView(edit);
            }*/

            if(badge.canGiveaway()){
                Button giveAway = new Button(this.getActivity());
                giveAway.setId(View.generateViewId());
                giveAway.setText("GIVE AWAY!");
                giveAway.setTextSize(21);
                // edit.setPadding(0, 6, 0, 0);
                giveAway.setGravity(Gravity.CENTER_HORIZONTAL);
                giveAway.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

                giveAway.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(activity, BadgeScanner.class);
                        intent.putExtra("BADGE", badge.getId());
                        activity.startActivity(intent);
                    }
                });

                layout.addView(giveAway);
            }
        }

        return layout;
    }
}
