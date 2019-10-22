package ca.staugustinechs.staugustineapp.Activities;

import android.content.Intent;
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
import android.view.View;
import android.widget.Button;
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

        //SET ACTIVITY TITLE
        this.getSupportActionBar().setTitle("Sign Up");
        this.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(AppUtils.PRIMARY_COLOR));

        //INITIALIZE THE DIFFERENT SIGN UP FRAGMENTS
        pages = new Fragment[3];
        pages[0] = SignUpFragment.newInstance(SignUpFragment.PROFILE_PIC);
        pages[1] = SignUpFragment.newInstance(SignUpFragment.CLASSES);
        pages[2] = SignUpFragment.newInstance(SignUpFragment.PREFS);

        //LOAD ALL COURSE OPTIONS FOR CHANGING SCHEDULE FRAGMENT
        SignUpFragment.loadCourses();

        suPager = (ViewPager) findViewById(R.id.suPager);
        //THIS TELLS THE VIEWPAGER TO SAVE UP TO 3 FRAGMENTS OFFSCREEN
        //IN MEMORY SO THEY DON'T GET RESET AND THE USER LOSES THEIR PROGRESS
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

        //SET THE PRIVACY POLICY LINK
        suPrivacy = findViewById(R.id.suPrivacy);
        suPrivacy.setText(Html.fromHtml("By clicking 'LET'S GO!' I agree to this app's " +
                "<a href=\"http://app.staugustinechs.ca/privacy\">Privacy Policy</a>."));
        suPrivacy.setMovementMethod(LinkMovementMethod.getInstance());

        suNextBtn = (Button) findViewById(R.id.suNextBtn);
        suNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //IF THE BUTTON IS CLICKED ON THE LAST FRAGMENT,
                if(page == pages.length - 1){
                    //Main.PROFILE.setIcon(((IconSelectFragment) pages[0]).getSelectedIcon());
                    //GET THE USER'S INPUTTED CLASSES FROM THE 2ND FRAGMENT
                    String[] classes = ((SignUpFragment) pages[1]).getClasses();
                    //GET THE USER'S INPUTTED PREFERENCES FROM THE 3RD FRAGMENT
                    boolean[] prefs = ((SignUpFragment) pages[2]).getPrefs();
                    
                    //MAKE SURE CLASSES AREN'T INVALID BEFORE CONTINUING
                    if(classes == null){
                        Snackbar.make(suNextBtn.getRootView(), "Some of your classes might be invalid!",
                                Snackbar.LENGTH_LONG).show();
                        suPager.setCurrentItem(1);
                    }else{
                        Snackbar.make(suNextBtn.getRootView(), "Setting you up!", Snackbar.LENGTH_LONG).show();
                        updateUser(((IconSelectFragment) pages[0]).getSelectedIcon(), classes, prefs);
                    }
                }else{
                    //IF WE AREN'T ON THE LAST FRAGMENT, GO TO THE NEXT FRAGMENT
                    suPager.setCurrentItem(page + 1);
                }
            }
        });
    }

    private void pageChanged(int i){
        //IF WE REACH THE LAST FRAGMENT, CHANGE THE "NEXT" BUTTON TO "LET'S GO!"
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
        //GATHER MOST OF THE USER'S NECESSARY DATA TO CREATE THEIR DOCUMENT
        //AND PUT IT INTO A MAP
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //GET USER'S EMAIL
        final String email = user.getEmail();
        final Map<String, Object> data = new HashMap<String, Object>();
        //GET USER'S CLASSES
        data.put("classes", Arrays.asList(classes));
        List<Integer> picsOwned = new ArrayList<Integer>();
        //ADD THE SELECTED INITIAL PROFILE PICTURE TO THEIR LIST OF OWNED PICS
        picsOwned.add(icon.getId());
        data.put("picsOwned", picsOwned);
        //GET THE USER'S PREFERENCES
        data.put("showClasses", prefs[0]);
        data.put("showClubs", prefs[1]);
        //GET THE GRAD YEAR OF THE USER AND STTUS, BUT IF THEY'RE A
        //TEACHER SET IT TO -1 AND CHANGE THEIR STATUS TO 1 
        String gradYear = email.substring(email.indexOf("@") - 2, email.indexOf("@"));
        if(gradYear.matches("[0-9]+")){
            data.put("gradYear", Integer.parseInt(gradYear));
            data.put("status", 0L);
        }else{
            data.put("gradYear", -1L);
            data.put("status", 1L);
        }
        //SET STARTING SPIRIT POINTS
        data.put("points", (long) AppUtils.STARTING_POINTS);
        //SET THEIR DEFAULT NOTIFICATION TO "GENERAL"
        data.put("notifications", Arrays.asList("general"));
        //CREATE EMPTY CLUBS AND BADGES ARRAY
        data.put("clubs", new ArrayList<String>());
        data.put("badges", new ArrayList<String>());

        //CREATE DOCUMENT WITH THE DATA
        FirebaseFirestore.getInstance().collection("users")
                .document(user.getUid())
                .set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    //CREATE A SECOND MAP THAT WILL BE USED TO CREATE THE VITAL DATA DOC
                    final Map<String, Object> vitalData = new HashMap<String, Object>();
                    //PUT VITAL DATA IN MAP
                    vitalData.put("email", email);
                    vitalData.put("name", user.getDisplayName());
                    vitalData.put("profilePic", icon.getId());
                    vitalData.put("msgToken", "");

                    //CREATE VITAL DATA DOC
                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(user.getUid())
                            .collection("info").document("vital")
                            .set(vitalData).addOnCompleteListener(new OnCompleteListener<Void>(){
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            //SAVE LOCALLY THAT USER HAS SIGNED UP
                            Map<String, String> signUpData = new HashMap<String, String>();
                            signUpData.put("signedUp", "true");
                            AppUtils.saveMapFile(user.getUid() + SignUp.SIGNUP_FILE,
                                    signUpData, SignUp.this);

                            //CREATE USER'S LOCAL PROFILE USING DATA
                            Main.PROFILE = new UserProfile(user.getUid(), vitalData, data);
                            Main.PROFILE.setLocalIcon(icon);

                            //SET FIREBASE ANALYTICS USER PROPERTIES SO WE CAN SEE
                            //NUMBER OF STUDENTS IN EACH GRADE FROM THE FIREBASE CONSOLE
                            FirebaseAnalytics.getInstance(SignUp.this)
                                    .setUserProperty("grade", Main.PROFILE.getGradYear() + "");

                            //LAUNCH MAIN ACTIVITY
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
        //IF THE BACK KEY IS PRESSED, GO BACK A FRAGMENT
        //OR EXIT IF WE'RE ON THE FIRST FRAGMENT
        if(page > 0){
            suPager.setCurrentItem(page - 1);
        }else{
            super.onBackPressed();
        }
    }
}
