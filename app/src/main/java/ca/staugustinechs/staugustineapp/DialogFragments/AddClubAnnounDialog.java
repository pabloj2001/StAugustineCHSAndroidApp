package ca.staugustinechs.staugustineapp.DialogFragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import ca.staugustinechs.staugustineapp.Activities.ClubDetails;
import ca.staugustinechs.staugustineapp.Activities.Main;
import ca.staugustinechs.staugustineapp.Objects.ClubAnnouncement;
import ca.staugustinechs.staugustineapp.R;

import static android.app.Activity.RESULT_OK;

public class AddClubAnnounDialog extends DialogFragment {

    private AlertDialog dialog;
    private ClubDetails clubDetails;
    private String id;
    private int pickPhotoCode = 123;
    private Uri selectedImage;
    private ImageView imgPreview;

    public AddClubAnnounDialog(){

    }

    public void setClubDetails(ClubDetails clubDetails){
        this.clubDetails = clubDetails;
    }

    public void setId(String id){
        this.id = id;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if(savedInstanceState == null && dialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            // Get the view
            final View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_addclubannoun, null);

            Button chooseImg = (Button) view.findViewById(R.id.dcd_announImg);
            chooseImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    AddClubAnnounDialog.this.startActivityForResult(pickPhoto, pickPhotoCode);
                }
            });

            final EditText title = (EditText) view.getRootView().findViewById(R.id.dcd_announTitle);
            final EditText content = (EditText) view.getRootView().findViewById(R.id.dcd_announContent);
            imgPreview = view.getRootView().findViewById(R.id.dcd_imgPreview);

            if(id != null && !id.isEmpty()){
                ClubAnnouncement announ = null;
                for(ClubAnnouncement anAnnoun : clubDetails.getAnnouns()){
                    if(anAnnoun.getId().equals(id)){
                        announ = anAnnoun;
                        break;
                    }
                }

                if(announ != null){
                    title.setText(announ.getTitle());
                    content.setText(announ.getContent());
                    if(announ.getImgName() != null && !announ.getImgName().isEmpty()){
                        final Button dltAnnoun = view.getRootView().findViewById(R.id.dcd_removeImg);
                        dltAnnoun.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                //DELETE
                                builder.setMessage("Are you sure you want to delete this announcement's image?")
                                        .setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                clubDetails.deleteImg(id);
                                                dltAnnoun.setVisibility(View.GONE);
                                                dialog.dismiss();
                                            }
                                        }).setNegativeButton("Absolutely Not!", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        });
                                builder.create().show();
                            }
                        });
                        dltAnnoun.setVisibility(View.VISIBLE);
                    }

                    builder.setTitle("Update Announcement");
                }else{
                    return null;
                }
            }else{
                builder.setTitle("Create New Announcement");
            }

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(view)
                    // Add action buttons
                    .setPositiveButton("Post", null)
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            this.dialog = builder.create();

            this.dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(final DialogInterface dialog) {
                    ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // POST ANNOUNCEMENT
                            if((title.getText().length() <= 0 || (title.getText().length() > 50)
                                        && Main.PROFILE.getStatus() != Main.DEV)){
                                Toast.makeText(content.getContext(),
                                        "Title can't be empty or over 50 characters!", Toast.LENGTH_LONG).show();
                            }else if(content.getText().length() > 300 && Main.PROFILE.getStatus() != Main.DEV){
                                Toast.makeText(content.getContext(),
                                        "Content can't be over 300 characters!", Toast.LENGTH_LONG).show();
                            }else{
                                if(id != null && !id.isEmpty()){
                                    clubDetails.updateAnnouncement(id, title.getText().toString().trim(),
                                            content.getText().toString().trim(), selectedImage);
                                }else{
                                    clubDetails.postAnnouncement(title.getText().toString().trim(),
                                            content.getText().toString().trim(), selectedImage);
                                }
                                dialog.dismiss();
                            }
                        }
                    });
                }
            });
        }
        return this.dialog;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == pickPhotoCode){
            if(resultCode == RESULT_OK){
                selectedImage = data.getData();
                Picasso.with(this.dialog.getContext())
                        .load(selectedImage)
                        .into(imgPreview);
            }
        }
    }

}
