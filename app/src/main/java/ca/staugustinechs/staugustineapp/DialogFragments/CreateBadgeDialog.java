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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import ca.staugustinechs.staugustineapp.Activities.ClubDetails;
import ca.staugustinechs.staugustineapp.Objects.Badge;
import ca.staugustinechs.staugustineapp.R;

import static android.app.Activity.RESULT_OK;

public class CreateBadgeDialog extends DialogFragment {

    private AlertDialog dialog;
    private ClubDetails clubDetails;
    private Uri selectedImage;
    private ImageView imgPreview;
    private Badge badge;
    private boolean clubBadge = false;

    public CreateBadgeDialog(){

    }

    public void setClubDetails(ClubDetails clubDetails){
        this.clubDetails = clubDetails;
    }

    public void setBadge(Badge badge){
        this.badge = badge;
    }

    public void setClubBadge(boolean clubBadge){
        this.clubBadge = clubBadge;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if(savedInstanceState == null && dialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            // Get the view
            final View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_createbadge, null);

            //IMG CHOOSER BUTTON
            Button chooseImg = (Button) view.findViewById(R.id.dcb_badgeImg);
            chooseImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setAspectRatio(1, 1)
                            .setCropShape(CropImageView.CropShape.OVAL)
                            .start(getContext(), CreateBadgeDialog.this);
                }
            });

            //DESCRIPTION EDIT TEXT
            final EditText desc = (EditText) view.getRootView().findViewById(R.id.dcb_badgeDesc);
            //BADGE IMAGE PREVIEW
            imgPreview = view.getRootView().findViewById(R.id.dcb_imgPreview);

            if(badge != null){
                desc.setText(badge.getDesc());
                imgPreview.setImageBitmap(badge.getImg());
            }

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(view)
                    // Add action buttons
                    .setPositiveButton(badge != null ? "Update" : "Create", null)
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
                            if(desc.getText().length() == 0 || desc.getText().length() > 100){
                                Toast.makeText(desc.getContext(),
                                        "Description can't be empty or over 100 characters!", Toast.LENGTH_LONG).show();
                            }else if(selectedImage == null && badge == null){
                                Toast.makeText(desc.getContext(),
                                        "Must choose badge image!", Toast.LENGTH_LONG).show();
                            }else{
                                if(badge == null){
                                    clubDetails.createBadge(selectedImage, desc.getText().toString(), clubBadge);
                                    dialog.dismiss();
                                }else{
                                    clubDetails.updateClubBadge(selectedImage, badge.getImgName(), desc.getText().toString());
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

    /*private void getCalendarDate(final int date, View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());

        long currentTime = System.currentTimeMillis();
        if(date == 1 && startDate != null){
            currentTime = startDate.getTime();
        }
        long maxTime = currentTime + 2592000000L;

        final DatePicker datePicker = new DatePicker(view.getContext());
        datePicker.setMinDate(currentTime);
        datePicker.setMaxDate(maxTime);

        long time = 0;
        if (date == 0 && startDate != null){
            time = startDate.getTime();
        }else if(date == 1 && endDate != null){
            time = endDate.getTime();
        }

        if(time != 0){
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(time);
            datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
        }

        builder.setView(datePicker);

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setPositiveButton("Choose Date", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                if(date == 0){
                    startDate = calendar.getTime();
                }else{
                    endDate = calendar.getTime();
                }
            }
        });

        builder.create().show();
    }*/

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
