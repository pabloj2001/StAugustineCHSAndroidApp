package ca.staugustinechs.staugustineapp.DialogFragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import ca.staugustinechs.staugustineapp.Activities.ClubDetails;
import ca.staugustinechs.staugustineapp.Fragments.ClubsFragment;
import ca.staugustinechs.staugustineapp.Objects.ClubItem;
import ca.staugustinechs.staugustineapp.R;

import static android.app.Activity.RESULT_OK;

public class EditClubDialog extends DialogFragment {

    private AlertDialog dialog;
    private ClubDetails clubDetails;
    private ClubsFragment clubsFragment;
    private Uri selectedImage;
    private ImageView imgPreview;
    private boolean hasImg;
    private int currentlyChecked = 0;

    public EditClubDialog(){

    }

    public void setClubDetails(ClubDetails clubDetails){
        this.clubDetails = clubDetails;
    }

    public void setClubsFragment(ClubsFragment clubsFragment){
        this.clubsFragment = clubsFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if(savedInstanceState == null && dialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            // Get the view
            final View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_editclub, null);

            Button chooseImg = (Button) view.findViewById(R.id.dcd_clubBanner);
            chooseImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   /* Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    EditClubDialog.this.startActivityForResult(pickPhoto, pickPhotoCode);*/
                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setAspectRatio(16, 9)
                            .start(getContext(), EditClubDialog.this);
                }
            });

            final EditText name = (EditText) view.getRootView().findViewById(R.id.dcd_clubName);
            final EditText desc = (EditText) view.getRootView().findViewById(R.id.dcd_clubDesc);
            imgPreview = view.getRootView().findViewById(R.id.dcd_ecImgPreview);

            final RadioGroup radioGroup = view.getRootView().findViewById(R.id.dcd_radioButtons);
            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if(checkedId == R.id.dcd_btn0){
                        currentlyChecked = 0;
                    }else if(checkedId == R.id.dcd_btn1){
                        currentlyChecked = 1;
                    }else if(checkedId == R.id.dcd_btn2){
                        currentlyChecked = 2;
                    }
                }
            });

            if(clubDetails != null){
                //SET PREVIOUS VALUES
                name.setText(clubDetails.getClub().getName());
                desc.setText(clubDetails.getClub().getDesc());
                hasImg = clubDetails.getClub().getImg() != null;
                currentlyChecked = clubDetails.getClub().getJoinPref();
                imgPreview.setImageBitmap(clubDetails.getClub().getImg());
            }else{
                currentlyChecked = 0;
            }
            ((RadioButton) radioGroup.getChildAt(currentlyChecked)).setChecked(true);

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(view)
                    // Add action buttons
                    .setPositiveButton(clubDetails != null ? "Done" : "Create", null)
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
                            if(name.getText().length() == 0 || name.getText().length() > 50){
                                Toast.makeText(name.getContext(),
                                        "Name can't be empty or over 50 characters!", Toast.LENGTH_LONG).show();
                            }else if(name.getText().toString().contains("\n")){
                                Toast.makeText(name.getContext(),
                                        "Name can't contain return keys!", Toast.LENGTH_LONG).show();
                            }else if(desc.getText().length() == 0 || desc.getText().length() > 300){
                                Toast.makeText(name.getContext(),
                                        "Description can't be empty or over 300 characters!", Toast.LENGTH_LONG).show();
                            }else if(!hasImg && selectedImage == null){
                                Toast.makeText(name.getContext(),
                                        "Must choose banner image!", Toast.LENGTH_LONG).show();
                            }else{
                                if(clubDetails != null){
                                    clubDetails.updateClub(name.getText().toString(),
                                            desc.getText().toString(), selectedImage, currentlyChecked);
                                    dialog.dismiss();
                                }else{
                                    ClubItem.createClub(name.getText().toString(), desc.getText().toString(),
                                            selectedImage, currentlyChecked, new OnCompleteListener() {
                                                @Override
                                                public void onComplete(@NonNull Task task) {
                                                    clubsFragment.refreshClubs();
                                                }
                                            }, null, clubsFragment.getActivity());
                                    Snackbar.make(clubsFragment.getView(), "Creating Club...",
                                            Snackbar.LENGTH_LONG).show();
                                    dialog.dismiss();
                                }
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
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            if(resultCode == RESULT_OK){
                selectedImage = CropImage.getActivityResult(data).getUri();
                Picasso.with(getContext())
                        .load(selectedImage)
                        .into(imgPreview);
            }
        }
    }

}
