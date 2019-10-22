package ca.staugustinechs.staugustineapp.DialogFragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import ca.staugustinechs.staugustineapp.Activities.Main;
import ca.staugustinechs.staugustineapp.Fragments.IconSelectFragment;
import ca.staugustinechs.staugustineapp.Objects.ProfileIcon;

public class IconSelectDialog extends DialogFragment {

    private IconSelectFragment iconSelectFragment;
    private ProfileIcon icon;

    public IconSelectDialog(){

    }

    public void setIconSelectFragment(IconSelectFragment iconSelectFragment){
        this.iconSelectFragment = iconSelectFragment;
    }

    public void setIcon(ProfileIcon icon){
        this.icon = icon;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Purchase Icon");

        //BUILD A VIEW
        builder.setView(createView());

        builder.setPositiveButton("Purchase", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
                if(Main.PROFILE.getPoints() >= icon.getCost()){
                    alertBuilder.setMessage("Are you sure you want to spend " + icon.getCost() +
                            " Points to purchase this profile picture?");

                    alertBuilder.setPositiveButton("Purchase", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog2, int which) {
                            iconSelectFragment.purchaseIcon(icon);
                        }
                    });

                    alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog2, int which) {
                            dialog2.cancel();
                            if(dialog != null){
                                dialog.cancel();
                            }
                        }
                    });
                }else{
                    alertBuilder.setMessage("Sorry you don't have enough Points to purchase this profile picture :/");

                    alertBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog2, int which) {
                            dialog2.cancel();
                            if(dialog != null){
                                dialog.cancel();
                            }
                        }
                    });
                }

                alertBuilder.create().show();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        return builder.create();
    }

    private LinearLayout createView(){
        LinearLayout layout = new LinearLayout(this.getActivity());
        layout.setPadding(8, 8, 8, 8);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        ImageView img = new ImageView(this.getActivity());
        img.setId(View.generateViewId());
        img.setImageBitmap(icon.getImg());
        img.setForegroundGravity(Gravity.CENTER_HORIZONTAL);
        img.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.addView(img);

        TextView cost = new TextView(this.getActivity());
        cost.setId(View.generateViewId());
        cost.setText("Cost: " + icon.getCost() + " Points");
        cost.setTextSize(32);
        cost.setPadding(0, 24, 0, 0);
        cost.setGravity(Gravity.CENTER_HORIZONTAL);
        cost.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        cost.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.addView(cost);

        return layout;
    }
}
