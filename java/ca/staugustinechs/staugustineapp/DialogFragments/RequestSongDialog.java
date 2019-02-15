package ca.staugustinechs.staugustineapp.DialogFragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import ca.staugustinechs.staugustineapp.Activities.Main;
import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.Fragments.SongsFragment;
import ca.staugustinechs.staugustineapp.R;

public class RequestSongDialog extends DialogFragment {

    private String REQUESTMSG;
    private final String REQUESTMSG2 = "Administration will be able to see who requested what song and have the power to " +
            "ban those who request inappropriate or irrelevant content.";

    private SongsFragment songsFragment;
    private String id;

    public RequestSongDialog(){
        REQUESTMSG = "Are you sure you want to spend " + AppUtils.REQUEST_SONG_COST
                + " Points to request a song? (You currently have ";
    }

    public void setSongsFragment(SongsFragment songsFragment){
        this.songsFragment = songsFragment;
    }

    public void setId(String id){
        this.id = id;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the view
        final View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_requestsong, null);
        final EditText title = (EditText) view.getRootView().findViewById(R.id.drs_title);
        final EditText artist = (EditText) view.getRootView().findViewById(R.id.drs_artist);

        builder.setTitle("Request A Song");

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                // Add action buttons
                .setPositiveButton("Request", null)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // POST ANNOUNCEMENT
                        if(title.getText().length() == 0 || title.getText().length() > 50){
                            Toast.makeText(title.getContext(),
                                    "Invalid title!", Toast.LENGTH_LONG).show();
                        }else if(artist.getText().length() == 0 || artist.getText().length() > 50){
                            Toast.makeText(title.getContext(),
                                    "Invalid artist name!", Toast.LENGTH_LONG).show();
                        }else{
                            dialog.dismiss();
                            showConfirmDialog(title.getText().toString(), artist.getText().toString());
                        }
                    }
                });
            }
        });

        return dialog;
    }

    private void showConfirmDialog(final String title, final String artist){
        AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
        builder2.setTitle("Request A Song");

        //BUILD A VIEW
        final View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_confirmsong, null);
        final TextView text = (TextView) view.getRootView().findViewById(R.id.dcs_text1);
        text.setText(REQUESTMSG + Main.PROFILE.getPoints() + " points)");
        final TextView text2 = (TextView) view.getRootView().findViewById(R.id.dcs_text2);
        text2.setText(REQUESTMSG2);

        builder2.setView(view);

        builder2.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                songsFragment.requestSong(title.trim(), artist.trim());
            }
        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        builder2.create().show();
    }

}
