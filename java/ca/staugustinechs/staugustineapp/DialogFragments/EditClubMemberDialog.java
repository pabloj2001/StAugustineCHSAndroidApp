package ca.staugustinechs.staugustineapp.DialogFragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import ca.staugustinechs.staugustineapp.Activities.ClubDetails;
import ca.staugustinechs.staugustineapp.Activities.ClubMemberList;
import ca.staugustinechs.staugustineapp.Objects.UserProfile;
import ca.staugustinechs.staugustineapp.RVAdapters.RViewAdapter_MemberList;

import static ca.staugustinechs.staugustineapp.Activities.ClubDetails.NEEDS_UPDATE;

public class EditClubMemberDialog extends DialogFragment {

    private final String REMOVE_MEMBER = "Are you sure you want to remove this member from the club?";
    private final String PROMOTE = "Are you sure you want to promote this member to an admin of the club? " +
            "(they will have as much power as you do right now).";
    private final String DEMOTE = "Are you sure you want to demote this admin to a normal member of the club?";

    private AlertDialog dialog;
    private ClubMemberList clubMemberList;
    private int mode = 0;
    private UserProfile user;

    public EditClubMemberDialog(){

    }

    public void setClubMemberList(ClubMemberList clubMemberList){
        this.clubMemberList = clubMemberList;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void setUserProfile(UserProfile user){
        this.user = user;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (mode == ClubMemberList.MEMBERS) {
            builder.setItems(new String[]{"Promote To Admin!", "Kick From Club"},
                    new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        showConfirmation(PROMOTE, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                clubMemberList.promote(user);
                            }
                        });
                    } else if (which == 1) {
                        showConfirmation(REMOVE_MEMBER, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                clubMemberList.removeUser(user);
                            }
                        });
                    }
                    dialog.dismiss();
                }
            });
            this.dialog = builder.create();
        } else if (mode == ClubMemberList.ADMINS) {
            builder.setItems(new String[]{"Demote From Admin", "Kick From Club"},
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {
                                showConfirmation(DEMOTE, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        clubMemberList.demote(user);
                                    }
                                });
                            } else if (which == 1) {
                                showConfirmation(REMOVE_MEMBER, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        clubMemberList.removeUser(user);
                                    }
                                });
                            }
                            dialog.dismiss();
                        }
                    });
            this.dialog = builder.create();
        } else if (mode == ClubMemberList.PENDING) {
            builder.setItems(new String[]{"Accept Into Club!", "Deny"},
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {
                                clubMemberList.accept(user);
                            } else if (which == 1) {
                                clubMemberList.removeUser(user);
                            }
                            dialog.dismiss();
                        }
                    });
            this.dialog = builder.create();
        }

        return this.dialog;
    }

    private void showConfirmation(String text, DialogInterface.OnClickListener listener){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(text);
        builder.setPositiveButton("Yes", listener);
        builder.setNegativeButton("No!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

}
