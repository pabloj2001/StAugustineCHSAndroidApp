package ca.staugustinechs.staugustineapp.DialogFragments;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import ca.staugustinechs.staugustineapp.Activities.Main;
import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.Fragments.IconSelectFragment;
import ca.staugustinechs.staugustineapp.Fragments.SongsFragment;
import ca.staugustinechs.staugustineapp.Objects.ProfileIcon;
import ca.staugustinechs.staugustineapp.Objects.SongItem;
import ca.staugustinechs.staugustineapp.R;

public class SuperVoteDialog extends DialogFragment {

    private SongsFragment songsFragment;
    private SongItem song;

    public SuperVoteDialog(){

    }

    public void setSongsFragment(SongsFragment songsFragment){
        this.songsFragment = songsFragment;
    }

    public void setSong(SongItem song) {
        this.song = song;
    }

    @TargetApi(26)
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Super Vote! (" + song.getTitle() + ")");

        final View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_supervote, null);
        final TextView cost = (TextView) view.getRootView().findViewById(R.id.dsv_cost);
        final TextView vote = (TextView) view.getRootView().findViewById(R.id.dsv_vote);
        final SeekBar seekBar = (SeekBar) view.getRootView().findViewById(R.id.dsv_seekBar);

        TextView desc = view.getRootView().findViewById(R.id.dsv_desc);
        CharSequence descText = desc.getText();
        desc.setText(descText + "You currently have " + Main.PROFILE.getPoints() + " Points.");

        if(Build.VERSION.SDK_INT > 25) {
            seekBar.setMin(AppUtils.SUPER_VOTE_MIN);
        }else{
            seekBar.setProgress(AppUtils.SUPER_VOTE_MIN);
        }
        seekBar.setMax(Main.PROFILE.getPoints() + AppUtils.SUPER_VOTE_MIN);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                cost.setText("Cost: " + progress + " Points");
                vote.setText("Votes: " + (int) Math.ceil(progress / AppUtils.SUPER_VOTE_MULT));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(Build.VERSION.SDK_INT <= 25 && seekBar.getProgress() < AppUtils.SUPER_VOTE_MIN){
                    seekBar.setProgress(AppUtils.SUPER_VOTE_MIN);
                }
            }
        });

        cost.setText("Cost: " + seekBar.getProgress() + " Points");
        vote.setText("Votes: " + (int) Math.ceil(seekBar.getProgress() / AppUtils.SUPER_VOTE_MULT));

        //BUILD A VIEW
        builder.setView(view);

        builder.setPositiveButton("Purchase", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                final int barProgress = seekBar.getProgress();
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());

                final int votes = (int) Math.ceil(barProgress / AppUtils.SUPER_VOTE_MULT);
                alertBuilder.setMessage("Are you sure you want to spend " + barProgress +
                        " Points to upvote \"" + song.getTitle() +
                        "\" by " + votes + " votes?" +
                        " (You will have " + (Main.PROFILE.getPoints() - barProgress) + " Points remaining).");

                alertBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog2, int which) {
                        songsFragment.superVote(barProgress, votes, song.getId());
                    }
                });

                alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog2, int which) {
                        dialog2.cancel();
                        if (dialog != null) {
                            dialog.cancel();
                        }
                    }
                });

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
}
