package ca.staugustinechs.staugustineapp.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.inappmessaging.FirebaseInAppMessaging;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.AsyncTasks.GetClubsTask;
import ca.staugustinechs.staugustineapp.AsyncTasks.GetProfileIconsTask;
import ca.staugustinechs.staugustineapp.AsyncTasks.GetUserTask;
import ca.staugustinechs.staugustineapp.Fragments.CafMenuFragment;
import ca.staugustinechs.staugustineapp.Fragments.ClubsFragment;
import ca.staugustinechs.staugustineapp.Fragments.HomeFragment;
import ca.staugustinechs.staugustineapp.Fragments.SettingsFragment;
import ca.staugustinechs.staugustineapp.Fragments.SongsFragment;
import ca.staugustinechs.staugustineapp.Fragments.SpiritMetersFragment;
import ca.staugustinechs.staugustineapp.Fragments.TitanTagFragment;
import ca.staugustinechs.staugustineapp.Interfaces.IconGetter;
import ca.staugustinechs.staugustineapp.MessagingService;
import ca.staugustinechs.staugustineapp.Objects.ProfileIcon;
import ca.staugustinechs.staugustineapp.Objects.SongItem;
import ca.staugustinechs.staugustineapp.Objects.UserProfile;
import ca.staugustinechs.staugustineapp.R;
import ca.staugustinechs.staugustineapp.Interfaces.UserGetter;

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
                //GO TO LOGIN ACTIVITY
                Intent intent = new Intent(this, Login.class);
                startActivity(intent);
                finish();
            } else if (AppUtils.shouldGetFile(FirebaseAuth.getInstance().getUid() + SignUp.SIGNUP_FILE,
                    this)) {
                //GO TO SIGN UP ACTIVITY
                Intent intent = new Intent(this, SignUp.class);
                startActivity(intent);
                finish();
            } else {
                setContentView(R.layout.activity_home);

                //SET CRASHLYTICS IDENTIFIER
                Crashlytics.setUserIdentifier(FirebaseAuth.getInstance().getUid());

                //REGISTER MSG ID WITH FIRESTORE
                FirebaseInstanceId.getInstance().getInstanceId()
                        .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                            @Override
                            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                if (task.isSuccessful()) {
                                    MessagingService.registerToken(task.getResult().getToken());
                                }
                            }
                        });

                //SET TOOLBAR
                toolbar = (Toolbar) findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);

                if (AppUtils.APP_ONLINE) {
                    //CREATE NOTIFICATIONS CHANNEL
                    MessagingService.createNotificationChannel(this);

                    //SET HOME FRAGMENT
                    homeFragment = new HomeFragment();
                    homeFragment.setMain(this);
                    changeFragment(homeFragment);

                    //GET USER DATA
                    refreshProfile();

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
        userTask = new GetUserTask(this,
                Arrays.asList(FirebaseAuth.getInstance().getUid()), true);
        userTask.execute();
    }

    @Override
    public void updateProfile(List<UserProfile> users) {
        if (users != null && !users.isEmpty() && users.get(0) != null
                && users.get(0).getIcon().getImg() != null) {
            Main.PROFILE = users.get(0);
            updateUserInfo();

            homeFragment.refreshAnnouns();

           /* if (Main.REGISTER_TOKEN) {
                MessagingService.registerToken();
            } else if ((Main.PROFILE.getMessagingToken() == null
                    || Main.PROFILE.getMessagingToken().isEmpty())) {
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
            }*/
        } else {
            homeFragment.setOffline();
        }
    }

    private void updateUserInfo() {
        ImageView profilePic = (ImageView) findViewById(R.id.nav_profilePic);
        int px = AppUtils.getDeviceDimen(R.dimen.icon_size, this);
        profilePic.setImageBitmap(Bitmap.createScaledBitmap(Main.PROFILE.getIcon().getImg(), px, px, false));

        TextView name = (TextView) findViewById(R.id.nav_studentName);
        name.setText(Main.PROFILE.getName());

        TextView email = (TextView) findViewById(R.id.nav_grade);
        email.setText(Main.PROFILE.getEmail().substring(0, Main.PROFILE.getEmail().indexOf("@")));

        View navGroup = findViewById(R.id.navGroup);
        navGroup.setOnClickListener(this);

        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    @Override
    public void updateIcons(List<ProfileIcon> icons) {
        Main.PROFILE.setLocalIcon(icons.get(0));
        updateUserInfo();
    }

    @Override
    public void onBackPressed() {
        if(drawer != null){
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
                toolbar.setTitle(R.string.app_name);
                changeFragment(homeFragment);
                break;
            case R.id.nav_extras:
                toolbar.setTitle("Clubs");
                changeFragment(new ClubsFragment());
                break;
            case R.id.nav_caf:
                toolbar.setTitle("Cafeteria Menu");
                changeFragment(new CafMenuFragment());
                break;
            case R.id.nav_songs:
                toolbar.setTitle("Song Requests");
                changeFragment(new SongsFragment());
                break;
            case R.id.nav_meter:
                toolbar.setTitle("Spirit Meters");
                changeFragment(new SpiritMetersFragment());
                break;
            case R.id.nav_titanTag:
                toolbar.setTitle("Titan Tag");
                changeFragment(new TitanTagFragment());
                break;
            case R.id.nav_settings:
                toolbar.setTitle("Settings");
                changeFragment(new SettingsFragment());
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.navGroup && AppUtils.isNetworkAvailable(this)) {
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
        if (UPDATE_ICON) {
            UPDATE_ICON = false;
            updateUserInfo();
        }
        super.onResume();
    }
}
