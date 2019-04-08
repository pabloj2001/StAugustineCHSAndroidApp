package ca.staugustinechs.staugustineapp.Activities;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.Fragments.IconSelectFragment;
import ca.staugustinechs.staugustineapp.Fragments.SignUpFragment;
import ca.staugustinechs.staugustineapp.Objects.ProfileIcon;
import ca.staugustinechs.staugustineapp.Objects.UserProfile;
import ca.staugustinechs.staugustineapp.R;

public class SignUp extends AppCompatActivity {

    public static final String SIGNUP_FILE = "_SIGNUP_FILE.dat";
    private int page = 0;
    private Fragment[] pages;
    private ViewPager suPager;
    private TextView suPrivacy;
    private Button suNextBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //SET STATUS BAR COLOR
        getWindow().setNavigationBarColor(AppUtils.PRIMARY_DARK_COLOR);
        getWindow().setStatusBarColor(AppUtils.PRIMARY_DARK_COLOR);

        this.getSupportActionBar().setTitle("Sign Up");
        this.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(AppUtils.PRIMARY_COLOR));

        pages = new Fragment[3];
        pages[0] = SignUpFragment.newInstance(SignUpFragment.PROFILE_PIC);
        pages[1] = SignUpFragment.newInstance(SignUpFragment.CLASSES);
        pages[2] = SignUpFragment.newInstance(SignUpFragment.PREFS);

        //LOAD ALL COURSE OPTIONS FOR CHANGING SCHEDULE FRAGMENT
        SignUpFragment.loadCourses();

        suPager = (ViewPager) findViewById(R.id.suPager);
        suPager.setOffscreenPageLimit(3);
        suPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int i) {
                return pages[i];
            }

            @Override
            public int getCount() {
                return pages.length;
            }
        });

        suPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                pageChanged(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        suPrivacy = findViewById(R.id.suPrivacy);
        suPrivacy.setText(Html.fromHtml("By clicking 'LET'S GO!' I agree to this app's " +
                "<a href=\"http://app.staugustinechs.ca/privacy\">Privacy Policy</a>."));
        suPrivacy.setMovementMethod(LinkMovementMethod.getInstance());

        suNextBtn = (Button) findViewById(R.id.suNextBtn);
        suNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(page == pages.length - 1){
                    //Main.PROFILE.setIcon(((IconSelectFragment) pages[0]).getSelectedIcon());
                    String[] classes = ((SignUpFragment) pages[1]).getClasses();
                    boolean[] prefs = ((SignUpFragment) pages[2]).getPrefs();
                    if(classes == null){
                        Snackbar.make(suNextBtn.getRootView(), "Some of your classes might be invalid!",
                                Snackbar.LENGTH_LONG).show();
                    }else{
                        Snackbar.make(suNextBtn.getRootView(), "Setting you up!", Snackbar.LENGTH_LONG).show();
                        updateUser(((IconSelectFragment) pages[0]).getSelectedIcon(), classes, prefs);
                    }
                }else{
                    suPager.setCurrentItem(page + 1);
                }
            }
        });
    }

    private void pageChanged(int i){
        page = i;
        if (page == pages.length - 1) {
            suPrivacy.setVisibility(View.VISIBLE);
            suNextBtn.setText("LET'S GO!");
        }else{
            suPrivacy.setVisibility(View.GONE);
            suNextBtn.setText("NEXT");
        }
        suNextBtn.requestFocus();
    }

    private void updateUser(final ProfileIcon icon, String[] classes, boolean[] prefs){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String email = user.getEmail();
        final Map<String, Object> data = new HashMap<String, Object>();
        data.put("classes", Arrays.asList(classes));
        List<Integer> picsOwned = new ArrayList<Integer>();
        picsOwned.add(icon.getId());
        data.put("picsOwned", picsOwned);
        data.put("showClasses", prefs[0]);
        data.put("showClubs", prefs[1]);
        String gradYear = email.substring(email.indexOf("@") - 2, email.indexOf("@"));
        if(gradYear.matches("[0-9]+")){
            data.put("gradYear", Integer.parseInt(gradYear));
            data.put("status", 0L);
        }else{
            data.put("gradYear", -1L);
            data.put("status", 1L);
        }
        data.put("points", (long) AppUtils.STARTING_POINTS);
        data.put("notifications", Arrays.asList("general"));
        data.put("clubs", new ArrayList<String>());
        data.put("badges", new ArrayList<String>());

        FirebaseFirestore.getInstance().collection("users")
                .document(user.getUid())
                .set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    final Map<String, Object> vitalData = new HashMap<String, Object>();
                    vitalData.put("email", email);
                    vitalData.put("name", user.getDisplayName());
                    vitalData.put("profilePic", icon.getId());
                    vitalData.put("msgToken", "");

                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(user.getUid())
                            .collection("info").document("vital")
                            .set(vitalData).addOnCompleteListener(new OnCompleteListener<Void>(){
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            //SAVE THAT USER HAS SIGNED UP
                            Map<String, String> signUpData = new HashMap<String, String>();
                            signUpData.put("signedUp", "true");
                            AppUtils.saveMapFile(user.getUid() + SignUp.SIGNUP_FILE,
                                    signUpData, SignUp.this);

                            Main.PROFILE = new UserProfile(user.getUid(), vitalData, data);
                            Main.PROFILE.setLocalIcon(icon);

                            FirebaseAnalytics.getInstance(SignUp.this)
                                    .setUserProperty("grade",Main.PROFILE.getGradYear() + "");

                            SignUp.this.startActivity(new Intent(SignUp.this, Main.class));
                            SignUp.this.finish();
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(page > 0){
            suPager.setCurrentItem(page - 1);
        }else{
            super.onBackPressed();
        }
    }
}
