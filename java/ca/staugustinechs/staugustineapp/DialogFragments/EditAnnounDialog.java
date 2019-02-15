package ca.staugustinechs.staugustineapp.DialogFragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import ca.staugustinechs.staugustineapp.Activities.ClubDetails;

public class EditAnnounDialog extends DialogFragment {

    private AlertDialog dialog;
    private ClubDetails clubDetails;
    private int mode = 0;
    private String id;

    public EditAnnounDialog(){

    }

    public void setClubDetails(ClubDetails clubDetails){
        this.clubDetails = clubDetails;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void setId(String id){
        this.id = id;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (mode == 0) {
            builder.setItems(new String[]{"Edit", "Delete"}, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        AddClubAnnounDialog addDialog = new AddClubAnnounDialog();
                        addDialog.setClubDetails(clubDetails);
                        addDialog.setId(id);
                        addDialog.show(clubDetails.getSupportFragmentManager(), "addClubDialog2");
                    } else if (which == 1) {
                        EditAnnounDialog newDialog = new EditAnnounDialog();
                        newDialog.setClubDetails(clubDetails);
                        newDialog.setId(id);
                        newDialog.setMode(1);
                        newDialog.show(clubDetails.getSupportFragmentManager(), "editAnnounDialog2");
                    }
                    dialog.dismiss();
                }
            });
            this.dialog = builder.create();
        } else if (mode == 1) {
            //DELETE
            builder.setMessage("Are you sure you want to delete this announcement?")
                    .setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            clubDetails.deleteAnnoun(id);
                        }
                    }).setNegativeButton("Absolutely Not!", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            this.dialog = builder.create();
        }

        return this.dialog;
    }

}
