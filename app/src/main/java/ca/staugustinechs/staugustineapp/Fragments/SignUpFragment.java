package ca.staugustinechs.staugustineapp.Fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import ca.staugustinechs.staugustineapp.Activities.Main;
import ca.staugustinechs.staugustineapp.R;

public class SignUpFragment extends Fragment {

    public final static int PROFILE_PIC = 0, CLASSES = 1, PREFS = 3;
    private static List<String> allCourses;
    private int type;
    private LinearLayout layout, editTextsLayout;
    private Switch spClassesPrivate, spClubsPrivate;

    public static Fragment newInstance(int type){
        if(type == PROFILE_PIC){
            IconSelectFragment fragment = new IconSelectFragment();

            Bundle bundle = new Bundle();
            bundle.putBoolean("SIGNUP", true);
            fragment.setArguments(bundle);

            return fragment;
        }else{
            SignUpFragment fragment = new SignUpFragment();

            Bundle bundle = new Bundle();
            bundle.putInt("TYPE", type);
            fragment.setArguments(bundle);

            return fragment;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        type = this.getArguments().getInt("TYPE");

        int layout = 0;
        switch (type){
            case CLASSES:
                layout = R.layout.fragment_signup2;
                break;
            case PREFS:
                layout = R.layout.fragment_signup3;
                break;
        }

        return inflater.inflate(layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        switch (type){
            case CLASSES:
                layout = view.findViewById(R.id.su2Layout);
                editTextsLayout = createTextEdit(false, this.getActivity());
                layout.addView(editTextsLayout);
                break;
            case PREFS:
                layout = view.findViewById(R.id.su3Layout);
                spClassesPrivate = layout.findViewById(R.id.spClassesPrivate);
                spClubsPrivate = layout.findViewById(R.id.spClubsPrivate);
                break;
        }
    }

    public static LinearLayout createTextEdit(boolean editClasses, Activity activity){
        LinearLayout layout = new LinearLayout(activity);
        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);

        TextView desc = new TextView(activity);
        desc.setId(View.generateViewId());
        desc.setTextSize(18f);
        desc.setText("Enter your Day 1 Courses");
        desc.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        desc.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        desc.setPadding(0, 4, 0, 0);
        layout.addView(desc);

        TextView sem1 = new TextView(activity);
        sem1.setId(View.generateViewId());
        sem1.setTextSize(18f);
        sem1.setText("Semester 1");
        sem1.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        sem1.setPadding(0, 12, 0, 0);
        layout.addView(sem1);

        EditText[] editTexts = new EditText[8];
        for(int i = 0; i < 4; i++){
            editTexts[i] = new EditText(activity);
            editTexts[i].setId(View.generateViewId());
            editTexts[i].setTag(i);
            editTexts[i].setPadding(4, 16, 4, 16);
            editTexts[i].setMinWidth(500);
            editTexts[i].setMaxWidth(500);
            editTexts[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            editTexts[i].setHint("Period " + (i + 1));

            if(editClasses && Main.PROFILE != null){
                editTexts[i].setText(Main.PROFILE.getSchedule().get(i));
            }

            layout.addView(editTexts[i]);
        }

        TextView sem2 = new TextView(activity);
        sem2.setId(View.generateViewId());
        sem2.setTextSize(18f);
        sem2.setText("Semester 2");
        sem2.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        sem2.setPadding(0, 16, 0, 0);
        layout.addView(sem2);

        for(int i = 4; i < 8; i++){
            editTexts[i] = new EditText(activity);
            editTexts[i].setId(View.generateViewId());
            editTexts[i].setTag(i);
            editTexts[i].setPadding(4, 16, 4, 16);
            editTexts[i].setMinWidth(500);
            editTexts[i].setMaxWidth(500);
            editTexts[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            editTexts[i].setHint("Period " + (i - 3));

            if(editClasses && Main.PROFILE != null){
                editTexts[i].setText(Main.PROFILE.getSchedule().get(i));
            }

            layout.addView(editTexts[i]);
        }

        return layout;
    }

    public String[] getClasses(){
        return SignUpFragment.getClasses(editTextsLayout);
    }

    public static void loadCourses(){
        if(allCourses == null){
            FirebaseFirestore.getInstance().collection("info")
                    .document("courses").get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                allCourses = (List<String>) task.getResult().get("courses");
                            }
                        }
                    });
        }
    }

    public static String[] getClasses(LinearLayout layout){
        if(allCourses != null && layout != null){
            List<EditText> editTexts = new ArrayList<EditText>();
            for(int i = 0; i < layout.getChildCount(); i++){
                View child = layout.getChildAt(i);
                if(child instanceof EditText){
                    editTexts.add(((EditText) child));
                }
            }

            String[] classes = new String[8];
            for(int i = 0; i < editTexts.size(); i++){
                String text = editTexts.get(i).getText().toString();
                if(text.length() > 0 && !text.equalsIgnoreCase("SPARE")) {
                    if (text.length() == 6 && allCourses.contains(text.toUpperCase())) {
                        classes[i] = text.toUpperCase();
                    } else if(text.length() == 7 && allCourses.contains(text.substring(0, 6).toUpperCase())
                            && text.substring(6).matches("[a-zA-Z]+")){
                        classes[i] = text.substring(0, 6).toUpperCase() + text.substring(6).toLowerCase();
                    }else {
                        return null;
                    }
                }else{
                    classes[i] = "SPARE";
                }
            }

            return classes;
        }else{
            return null;
        }
    }

    public boolean[] getPrefs(){
        boolean[] prefs = new boolean[3];
        prefs[0] = !spClassesPrivate.isChecked();
        prefs[1] = !spClubsPrivate.isChecked();
        return prefs;
    }

}
