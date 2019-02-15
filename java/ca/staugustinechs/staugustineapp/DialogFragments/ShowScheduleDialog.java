package ca.staugustinechs.staugustineapp.DialogFragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;

import ca.staugustinechs.staugustineapp.Activities.Main;
import ca.staugustinechs.staugustineapp.Activities.Profile;
import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.Fragments.SignUpFragment;
import ca.staugustinechs.staugustineapp.Fragments.SongsFragment;
import ca.staugustinechs.staugustineapp.Objects.UserProfile;
import ca.staugustinechs.staugustineapp.R;

public class ShowScheduleDialog extends DialogFragment {

    private Profile profile;
    private List<Integer> classesInCommon = null;

    public ShowScheduleDialog(){

    }

    public void setProfile(Profile profile){
        this.profile = profile;
    }

    public void setClassesInCommon(List<Integer> classesInCommon){
        this.classesInCommon = classesInCommon;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Schedule");

        //BUILD A VIEW
        builder.setView(createView());
        if(classesInCommon == null){
            builder.setNegativeButton("Edit Schedule", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    createEditScheduleDialog(dialog);
                }
            });
        }
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        return builder.create();
    }

    private LinearLayout createView(){
        List<String> classes = this.profile.getProfile().getSchedule();

        LinearLayout layout = new LinearLayout(this.getActivity());
        layout.setPadding(8, 8, 8, 8);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView head1 = new TextView(this.getActivity());
        head1.setId(View.generateViewId());
        head1.setText("Semester 1");
        head1.setTextSize(21);
        head1.setGravity(Gravity.CENTER_HORIZONTAL);
        head1.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        head1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.addView(head1);

        for(int i = 0; i < classes.size() / 2; i++){
            layout.addView(getTextView(i, classes.get(i)));
        }

        TextView head2 = new TextView(this.getActivity());
        head2.setId(View.generateViewId());
        head2.setText("Semester 2");
        head2.setTextSize(21);
        head2.setGravity(Gravity.CENTER_HORIZONTAL);
        head2.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        head2.setPadding(0, 16, 0, 0);
        head2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.addView(head2);

        for(int i = 4; i < classes.size(); i++){
            layout.addView(getTextView(i, classes.get(i)));
        }

        return layout;
    }

    private TextView getTextView(int i, String text){
        TextView textView = new TextView(this.getActivity());
        textView.setId(View.generateViewId());
        textView.setText(text);
        textView.setTextSize(18);
        textView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        textView.setPadding(0, 8, 0, 0);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        if(classesInCommon != null){
            if(classesInCommon.contains(i)){
                textView.setTextColor(AppUtils.ACCENT_COLOR);
            }
        }

        return textView;
    }

    private void createEditScheduleDialog(final DialogInterface parent){
        final LinearLayout editTextsLayout = SignUpFragment.createTextEdit(true, getActivity());
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit Schedule");
        builder.setView(editTextsLayout);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setPositiveButton("Update", null);

        //FETCH ALL COURSE OPTIONS
        SignUpFragment.loadCourses();

        Dialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String[] classes = SignUpFragment.getClasses(editTextsLayout);
                                if (classes != null) {
                                    final List<String> schedule = Arrays.asList(classes);
                                    FirebaseFirestore.getInstance().collection("users")
                                            .document(Main.PROFILE.getUid())
                                            .update("classes", schedule)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        Main.PROFILE.setSchedule(schedule);
                                                        Snackbar.make(profile.findViewById(R.id.profilePic),
                                                                "Succesfully Updated Schedule!",
                                                                Snackbar.LENGTH_LONG).show();
                                                        dialog.dismiss();
                                                        parent.dismiss();
                                                    }
                                                }
                                            });
                                } else {
                                    if(((AlertDialog) dialog).getContext() != null){
                                        Toast.makeText(((AlertDialog) dialog).getContext(),
                                                "Please provide valid courses!", Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        });
            }
        });
        dialog.show();
    }
}
