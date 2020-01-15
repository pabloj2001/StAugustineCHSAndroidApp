package ca.staugustinechs.staugustineapp.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.AsyncTasks.GetQuoteTask;
import ca.staugustinechs.staugustineapp.AsyncTasks.GetUserTask;
import ca.staugustinechs.staugustineapp.Fragments.CafMenuFragment;
import ca.staugustinechs.staugustineapp.Fragments.ClubsFragment;
import ca.staugustinechs.staugustineapp.Fragments.FaqFragment;
import ca.staugustinechs.staugustineapp.Fragments.HomeFragment;
import ca.staugustinechs.staugustineapp.Fragments.SettingsFragment;
import ca.staugustinechs.staugustineapp.Fragments.SongsFragment;
import ca.staugustinechs.staugustineapp.Fragments.SpiritMetersFragment;
import ca.staugustinechs.staugustineapp.Fragments.TitanTagFragment;
import ca.staugustinechs.staugustineapp.Interfaces.IconGetter;
import ca.staugustinechs.staugustineapp.Interfaces.UserGetter;
import ca.staugustinechs.staugustineapp.MessagingService;
import ca.staugustinechs.staugustineapp.Objects.ProfileIcon;
import ca.staugustinechs.staugustineapp.Objects.UserProfile;
import ca.staugustinechs.staugustineapp.R;

public class Main extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, UserGetter, IconGetter {

    public static final String CALENDAR_ADDED = "calendarAdded.dat";
    public static final int STUDENT = 0, TEACHER = 1, DEV = 2;
    public static final int IMG_QUALITY = 100;
    public static boolean UPDATE_ICON = false;
    public static UserProfile PROFILE = null;
    public static boolean REGISTER_TOKEN = false;

    Toolbar toolbar;
    DrawerLayout drawer;
    NavigationView navigationView;

    private GetUserTask userTask;

    private final Map<String, Integer> fragments = new HashMap<String, Integer>();
    private HomeFragment homeFragment;

    private GetQuoteTask qtask;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        //INIT SOME FIREBASE SETTINGS
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        FirebaseFirestore.getInstance().setFirestoreSettings(settings);

        //FIREBASE MESSAGING SETTINGS
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);

        //SET PROPER THEME (PREVIOUS IS LAUNCH THEME)
        this.setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        this.init(savedInstanceState);
    }

    private void init(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                //THE USER ISN'T SIGNED INTO FIREBASE, GO TO LOGIN ACTIVITY
                Intent intent = new Intent(this, Login.class);
                startActivity(intent);
                finish();
            } else if (AppUtils.shouldGetFile(FirebaseAuth.getInstance().getUid() + SignUp.SIGNUP_FILE,
                    this)) {
                //IF THE USER IS SIGNED IN BUT HASN'T COMPLETED SIGNING UP
                //(THE FILE SAYING THEY HAVE SIGNED UP DOESN'T EXIST AND/OR THEY DON'T HAVE
                //A USER DOCUMENT IN FIRESTORE), GO TO THE SIGN UP ACTIVITY
                Intent intent = new Intent(this, SignUp.class);
                startActivity(intent);
                finish();
            } else {
                //SET THE MAIN ACTIVITY LAYOUT
                setContentView(R.layout.activity_home);

                //SET CRASHLYTICS IDENTIFIER
                Crashlytics.setUserIdentifier(FirebaseAuth.getInstance().getUid());

                //SET TOOLBAR
                toolbar = (Toolbar) findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);

                if (AppUtils.APP_ONLINE) {
                    //CREATE NOTIFICATIONS CHANNEL
                    MessagingService.createNotificationChannel(this);

                    //CREATE HOME FRAGMENT
                    homeFragment = new HomeFragment();
                    homeFragment.setMain(this);
                    changeFragment(homeFragment);

                    //GET USER DATA
                    refreshProfile();

                    //UPDATE QUOTE
                    updateQuote();

                    //TOOLBAR, DRAWER, AND NAVIGATION STUFF
                    drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
                    toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            drawer.openDrawer(GravityCompat.START);
                        }
                    });
                    drawer.addDrawerListener(toggle);
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                    toggle.syncState();

                    navigationView = (NavigationView) findViewById(R.id.nav_view);
                    navigationView.setCheckedItem(R.id.nav_home);
                    navigationView.setNavigationItemSelectedListener(this);

                    //UPDATE APP COLORS
                    this.updateColors();

                    try {
                        //IF USER DOESN'T HAVE THE NEWEST VERSION, SHOOT THEM A MESSAGE...
                        PackageInfo info = this.getPackageManager()
                                .getPackageInfo(this.getPackageName(), PackageManager.GET_ACTIVITIES);
                        if(!AppUtils.ANDROID_VERSION.equals(info.versionName)){
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setTitle("New Update Available!");
                            builder.setMessage("There is a new update available for the app on the Play store!" +
                                    " You should go get it.");
                            builder.setPositiveButton("Ok!", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            /*Button btn = new Button(this);
                            btn.setText("HEAD TO THE STORE!");
                            btn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            });
                            builder.setView(btn);*/
                            builder.create().show();
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }

                } else {
                    //WE HAVE MANUALLY SHUT DOWN THE APP FROM RC, DON'T LET THE USER IN.
                    toolbar.setTitle("APP CURRENTLY OFFLINE");
                    LinearLayout layout = findViewById(R.id.mainLayout);
                    layout.removeView(findViewById(R.id.main_fragment_container));
                    View offline = getLayoutInflater().inflate(R.layout.offline_layout, null);
                    layout.addView(offline);
                }
            }
        }
    }

    private void updateColors() {
        //CHANGE TOOLBAR AND DRAWER HEADING COLORS
        toolbar.setBackgroundColor(AppUtils.PRIMARY_COLOR);
        navigationView.getHeaderView(0).setBackgroundColor(AppUtils.PRIMARY_COLOR);
        //SET STATUS BAR COLOR
        getWindow().setNavigationBarColor(AppUtils.PRIMARY_DARK_COLOR);
        getWindow().setStatusBarColor(AppUtils.PRIMARY_DARK_COLOR);

        //SET DRAWER ITEMS COLORS
        ColorStateList colorStateList = new ColorStateList(new int[][]{new int[]{android.R.attr.state_checked},
                new int[]{-android.R.attr.state_checked}}, new int[]{AppUtils.PRIMARY_COLOR,
                Color.parseColor("#808080")});
        navigationView.setItemIconTintList(colorStateList);
        navigationView.setItemTextColor(colorStateList);
    }

    public void refreshProfile() {
        //GET THE USER DATA. WILL CALL OUR updateProfile METHOD WHEN DONE
        userTask = new GetUserTask(this,
                Arrays.asList(FirebaseAuth.getInstance().getUid()), true);
        userTask.execute();
    }

    public void updateQuote() {
        GetQuoteTask qtask = new GetQuoteTask(this.homeFragment);
        qtask.execute();
    }

    @Override
    public void updateProfile(List<UserProfile> users) {
        if (users != null && !users.isEmpty() && users.get(0) != null
                && users.get(0).getIcon().getImg() != null) {
            //SAVE THE USER AND UPDATE THE DRAWER
            Main.PROFILE = users.get(0);
            updateUserInfo();

            //REFRESH THE HOME PAGE ANNOUNCEMENTS
            homeFragment.refreshAnnouns();

           /* if (Main.REGISTER_TOKEN) {
                MessagingService.registerToken();
            } else if ((Main.PROFILE.getMessagingToken() == null
                    || Main.PROFILE.getMessagingToken().isEmpty())) {*/
            //GET DEVICE MESSAGE ID AND REGISTER INTO FIRESTORE
            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (task.isSuccessful()) {
                                MessagingService.registerToken(task.getResult().getToken());
                            }
                        }
                    });
            //}
        } else {
            //WE COULDN'T GET THE USER'S DOCUMENT SO THE DATABASE
            //MUST BE UNREACHABLE AT THE MOMENT (PROBABLY THE DEVICE IS OFFLINE)
            homeFragment.setOffline();
        }
    }

    private void updateUserInfo() {
        //SET THE USER'S PROFILE PICTURE IN THE DRAWER
        ImageView profilePic = (ImageView) findViewById(R.id.nav_profilePic);
        int px = AppUtils.getDeviceDimen(R.dimen.icon_size, this);
        profilePic.setImageBitmap(Bitmap.createScaledBitmap(Main.PROFILE.getIcon().getImg(), px, px, false));

        //SET THE USER'S NAME IN THE DRAWER
        TextView name = (TextView) findViewById(R.id.nav_studentName);
        name.setText(Main.PROFILE.getName());

        //SET THE USER'S EMAIL IN THE DRAWER
        TextView email = (TextView) findViewById(R.id.nav_grade);
        email.setText(Main.PROFILE.getEmail().substring(0, Main.PROFILE.getEmail().indexOf("@")));

        TextView points = (TextView) findViewById(R.id.nav_points);
        points.setText(getString(R.string.points).concat(String.valueOf(Main.PROFILE.getPoints())));

        //ALLOW THE TOP SECTION OF THE DRAWER TO BE CLICKABLE
        View navGroup = findViewById(R.id.navGroup);
        navGroup.setOnClickListener(this);

        //UNLOCK THE DRAWER SO THAT USER CAN OPEN IT
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    @Override
    public void updateIcons(List<ProfileIcon> icons) {
        //SET THE USER'S ICON
        Main.PROFILE.setLocalIcon(icons.get(0));
        updateUserInfo();
    }

    @Override
    public void onBackPressed() {
        if(drawer != null){
            //ON BACK PRESSED, CLOSE THE DRAWER IF IT IS OPEN
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
                return;
            }
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.nav_home:
                //SWITCH TO HOME FRAGMENT
                toolbar.setTitle(R.string.app_name);
                changeFragment(homeFragment);
                break;
            case R.id.nav_extras:
                //SWITCH TO CLUBS FRAGMENT
                toolbar.setTitle("Clubs");
                changeFragment(new ClubsFragment());
                break;
            case R.id.nav_caf:
                //SWITCH TO CAF MENUS FRAGMENT
                toolbar.setTitle("Cafeteria Menu");
                changeFragment(new CafMenuFragment());
                break;
            case R.id.nav_songs:
                //SWITCH TO SONGS FRAGMENT
                toolbar.setTitle("Song Requests");
                changeFragment(new SongsFragment());
                break;
            case R.id.nav_meter:
                //SWITCH TO SPIRIT METERS FRAGMENT
                toolbar.setTitle("Spirit Meters");
                changeFragment(new SpiritMetersFragment());
                break;
            case R.id.nav_titanTag:
                //SWITCH TO TITAN TAG FRAGMENT
                toolbar.setTitle("Titan Tag");
                changeFragment(new TitanTagFragment());
                break;
            case R.id.nav_settings:
                //SWITCH TO SETTINGS FRAGMENT
                toolbar.setTitle("Settings");
                changeFragment(new SettingsFragment());
                break;

            case R.id.nav_faq:
                //SWITCH TO FAQ
                toolbar.setTitle("FAQ");
                changeFragment(new FaqFragment());
                break;
            case R.id.nav_prayer:
                //launch form in browser
                AlertDialog.Builder redirectDialog = new AlertDialog.Builder(Objects.requireNonNull(this));
                redirectDialog.setTitle("Redirect Warning");
                redirectDialog.setMessage("You will be redirected to a Google form!");
                redirectDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Uri prayerForm = Uri.parse("https://forms.gle/hrNKVGsug1FpiXTg7");
                        Intent prayerIntent = new Intent(Intent.ACTION_VIEW, prayerForm);
                        if (prayerIntent.resolveActivity(getPackageManager()) != null) {
                            startActivity(prayerIntent);
                        } else {
                            Log.d("IMPLICIT_PRAYER", "No intent receivers");
                        }
                    }
                });

                redirectDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //do nothing
                    }
                });

                redirectDialog.show();
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.navGroup && AppUtils.isNetworkAvailable(this)) {
            //CLOSE THE DRAWER AND LAUNCH PROFILE ACTIVITY
            drawer.closeDrawer(GravityCompat.START);
            Intent intent = new Intent(this, Profile.class);
            intent.putExtra("USER_EMAIL", "");
            startActivity(intent);
        }
    }

    private void changeFragment(Fragment fragment) {
        if (!this.isDestroyed()) {
            //getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, fragment).commit();

            FragmentManager manager = getSupportFragmentManager();
            String fragName = fragment.getClass().getName();
            Fragment frag = manager.findFragmentByTag(fragName);

            if (frag != null) {
                //if the fragment exists, show it.
                manager.beginTransaction().show(frag).commit();
            } else {
                //if the fragment does not exist, add it to fragment manager.
                manager.beginTransaction().add(R.id.main_fragment_container, fragment, fragName).commit();
            }

            for (Fragment otherFrag : manager.getFragments()) {
                if (!otherFrag.getClass().getName().equals(fragName)) {
                    //if the other fragment is visible, hide it.
                    manager.beginTransaction().hide(otherFrag).commit();
                }
            }
        }
    }

    @Override
    public void onResume() {
        //IF THE USER HAS CHANGED THEIR ICON, UPDATE THE DRAWER WITH THE NEW ICON
        if (UPDATE_ICON) {
            UPDATE_ICON = false;
            updateUserInfo();
        }
        super.onResume();
    }
}
