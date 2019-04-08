package ca.staugustinechs.staugustineapp.Activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.AsyncTasks.GetBadgesTask;
import ca.staugustinechs.staugustineapp.AsyncTasks.GetClubAnnounsTask;
import ca.staugustinechs.staugustineapp.AsyncTasks.GetUserTask;
import ca.staugustinechs.staugustineapp.DialogFragments.AddClubAnnounDialog;
import ca.staugustinechs.staugustineapp.DialogFragments.CreateBadgeDialog;
import ca.staugustinechs.staugustineapp.DialogFragments.EditAnnounDialog;
import ca.staugustinechs.staugustineapp.DialogFragments.EditClubDialog;
import ca.staugustinechs.staugustineapp.Fragments.ClubsFragment;
import ca.staugustinechs.staugustineapp.Interfaces.BadgeGetter;
import ca.staugustinechs.staugustineapp.Interfaces.ClubAnnounGetter;
import ca.staugustinechs.staugustineapp.MessagingService;
import ca.staugustinechs.staugustineapp.Objects.Badge;
import ca.staugustinechs.staugustineapp.Objects.UserProfile;
import ca.staugustinechs.staugustineapp.R;
import ca.staugustinechs.staugustineapp.Objects.ClubItem;
import ca.staugustinechs.staugustineapp.Objects.ClubAnnouncement;
import ca.staugustinechs.staugustineapp.RVAdapters.RViewAdapter_Badges;
import ca.staugustinechs.staugustineapp.RVAdapters.RViewAdapter_ClubAnnouns;

public class ClubDetails extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener,
        ViewTreeObserver.OnScrollChangedListener, MenuItem.OnMenuItemClickListener, BadgeGetter, ClubAnnounGetter {

    public static boolean NEEDS_UPDATE = false;

    private RecyclerView rv, rv2;
    private ClubItem club;
    private List<Badge> badges;
    private List<ClubAnnouncement> announs;
    private float downX, downY;
    private float threshold = 250, thresholdY = 200;
    private boolean isAdmin, isMember;
    private ProgressBar cdLoadingCircle, cdLoadingCircle2;
    private NestedScrollView cdScrollView;
    private TextView cdName, cdName2, cdDesc;
    private ImageView cdBanner;
    private int cdNameY;
    private int itemMemberList, itemPendingList, itemEditClub, itemLeaveClub, itemToggleNotifs, itemClubBadge;
    private SwipeRefreshLayout cdSwipeRefresh;
    private GetBadgesTask getBadgesTask;
    private GetClubAnnounsTask getClubAnnounsTask;
    private Button cdJoinBtn;
    private ImageButton cdBadgesAdd;
    private RViewAdapter_ClubAnnouns adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clubdetails);

        //SET STATUS BAR COLOR
        getWindow().setNavigationBarColor(AppUtils.PRIMARY_DARK_COLOR);
        getWindow().setStatusBarColor(AppUtils.PRIMARY_DARK_COLOR);

        this.getSupportActionBar().setTitle("Clubs");
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(AppUtils.PRIMARY_COLOR));

        club = (ClubItem) getIntent().getSerializableExtra("club");
        club.unpack(this);

        cdBanner = (ImageView) findViewById(R.id.cdBanner);
        cdBanner.setImageBitmap(club.getImg());

        cdName = (TextView) findViewById(R.id.cdName);
        cdName.setText(club.getName());
        cdName.setBackgroundColor(AppUtils.PRIMARY_COLOR);

        cdDesc = (TextView) findViewById(R.id.cdDesc);
        cdDesc.setText(club.getDesc());

        cdName2 = (TextView) findViewById(R.id.cdName2);
        cdName2.setText(club.getName());
        cdName2.setBackgroundColor(AppUtils.PRIMARY_COLOR);

        cdScrollView = (NestedScrollView) findViewById(R.id.cdScrollView);
        cdScrollView.getViewTreeObserver().addOnScrollChangedListener(this);

        cdLoadingCircle = this.findViewById(R.id.cdLoadingCircle);
        cdLoadingCircle.getIndeterminateDrawable().setTint(AppUtils.ACCENT_COLOR);
        cdLoadingCircle2 = this.findViewById(R.id.cdLoadingCircle2);
        cdLoadingCircle2.getIndeterminateDrawable().setTint(AppUtils.ACCENT_COLOR);

        cdSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.cdSwipeRefresh);
        cdSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshContent(true);
            }
        });
        cdSwipeRefresh.setColorSchemeColors(AppUtils.ACCENT_COLOR);
        cdSwipeRefresh.setEnabled(false);

        this.isAdmin = Main.PROFILE.getStatus() == 2 || club.getAdmins().contains(FirebaseAuth.getInstance().getUid());
        this.isMember = isAdmin || club.getMembers().contains(FirebaseAuth.getInstance().getUid());

        //SHOWS BADGES TITLE
        TextView cdBadgeTitle = (TextView) findViewById(R.id.cdBadgesHeader);
        cdBadgeTitle.setTextColor(AppUtils.PRIMARY_COLOR);
        cdBadgeTitle.setVisibility(View.VISIBLE);
        cdLoadingCircle.setVisibility(View.VISIBLE);

        ////SHOW BADGES////
        rv = (RecyclerView) this.findViewById(R.id.rv2);
        rv.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }

            @Override
            public boolean canScrollHorizontally() {
                return false;
            }
        };
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rv.setLayoutManager(layoutManager);

        //GET BADGES
        getBadgesTask = new GetBadgesTask(this);
        getBadgesTask.execute();

        this.badges = new ArrayList<Badge>();

        if(isMember){
            //SHOW ANNOUNCEMENTS TITLE
            TextView cdAnnounTitle = (TextView) findViewById(R.id.cdAnnounHeader);
            cdAnnounTitle.setTextColor(AppUtils.PRIMARY_COLOR);
            cdAnnounTitle.setVisibility(View.VISIBLE);
            cdLoadingCircle2.setVisibility(View.VISIBLE);

            //GET ANNOUNCEMENTS
            getClubAnnounsTask = new GetClubAnnounsTask(this.club.getId(), this, this);
            getClubAnnounsTask.execute();

            rv2 = (RecyclerView) this.findViewById(R.id.rv);
            rv2.setHasFixedSize(true);

            // use a linear layout manager
            LinearLayoutManager layoutManager2 = new LinearLayoutManager(this){
                @Override
                public boolean canScrollVertically() {
                    return false;
                }
            };
            rv2.setLayoutManager(layoutManager2);

            if(isAdmin){
                FloatingActionButton cdAddAnnouncs = (FloatingActionButton) findViewById(R.id.cdAddAnnouncement);
                cdAddAnnouncs.setBackgroundTintList(AppUtils.ACCENT_COLORSL);
                cdAddAnnouncs.setOnClickListener(this);
                cdAddAnnouncs.show();
            }

            if(Main.PROFILE.getStatus() == Main.DEV){
                cdBadgesAdd = this.findViewById(R.id.cdBadgesAdd);
                cdBadgesAdd.setOnClickListener(this);
            }
        }

        if(!isMember || (Main.PROFILE.getStatus() == Main.DEV &&
                    !club.getAdmins().contains(FirebaseAuth.getInstance().getUid())
                    && !club.getMembers().contains(FirebaseAuth.getInstance().getUid()))){
            cdJoinBtn = (Button) findViewById(R.id.cdJoinBtn);
            cdJoinBtn.setOnClickListener(this);
            cdJoinBtn.setVisibility(View.VISIBLE);

            if(club.getJoinPref() == 0){
                cdJoinBtn.setEnabled(false);
            }else if(club.getJoinPref() == 1){
                if(club.getPendingList().contains(FirebaseAuth.getInstance().getUid())){
                    cdJoinBtn.setEnabled(false);
                }else{
                    cdJoinBtn.setEnabled(true);
                }
            }else{
                cdJoinBtn.setEnabled(true);
            }
        }
    }

    public void postAnnouncement(final String title, final String content, final Uri img){
        showPostSnack(0);
        //UPLOAD IMG
        if (img != null && !img.getPath().isEmpty()) {
            final String imgName = AppUtils.getRandomKey(20);
            byte[] imgBytes = AppUtils.getImgBytes(img, 0, 0, this);
            AppUtils.uploadImg(imgName, imgBytes, "announcements/",
                    new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> taskStorage) {
                    if(taskStorage.isSuccessful()){
                        uploadToDB(title, content, imgName);
                    }else{
                        showPostSnack(2);
                    }
                }
            });
        } else {
            uploadToDB(title, content, "");
        }
    }

    private void uploadToDB(final String title, final String content, final String imgName){
        //GATHER DATA
        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("content", content);
        data.put("date", Timestamp.now());
        data.put("creator", FirebaseAuth.getInstance().getUid());
        data.put("img", imgName);
        data.put("club", this.club.getId());
        data.put("clubName", club.getName());

        //ADD TO DB
        FirebaseFirestore.getInstance()
                .collection("announcements").add(data)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull final Task<DocumentReference> taskAnnoun) {
                        if (taskAnnoun.isSuccessful()) {
                            if (taskAnnoun.isSuccessful()) {
                                showPostSnack(1);
                                MessagingService.sendMessage(club.getId(), club.getName(),
                                        title, content, null);
                                refreshContent(true);
                            } else {
                                showPostSnack(2);
                                FirebaseFirestore.getInstance()
                                        .collection("announcements")
                                        .document(taskAnnoun.getResult().getId()).delete();
                            }
                        } else {
                            showPostSnack(2);
                            if (taskAnnoun.getResult() != null) {
                                FirebaseFirestore.getInstance()
                                        .collection("announcements")
                                        .document(taskAnnoun.getResult().getId()).delete();
                                FirebaseStorage.getInstance().getReference(imgName).delete();
                            }
                        }
                    }
                });
    }

    public void updateAnnouncement(final String id, final String title, final String content, final Uri img){
        showPostSnack(0);

        //UPLOAD IMG
        if (img != null && !img.getPath().isEmpty()) {
            deleteImg(id);

            final String imgName = AppUtils.getRandomKey(20);
            byte[] imgBytes = AppUtils.getImgBytes(img, 0, 0, this);
            AppUtils.uploadImg(imgName, imgBytes, "announcements/",
                    new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> taskStorage) {
                            if(taskStorage.isSuccessful()){
                                updateDBAnnoun(id, title, content, imgName);
                            }else{
                                showPostSnack(2);
                            }
                        }
                    });
        }else{
            updateDBAnnoun(id, title, content, null);
        }
    }

    private void updateDBAnnoun(String id, final String title, final String content, String imgName){
        //CREATE ANNOUNCEMENT
        //GATHER DATA
        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("content", content);
        data.put("creator", FirebaseAuth.getInstance().getUid());
        if (imgName != null && !imgName.isEmpty()) {
            data.put("img", imgName);
        }

        //UPDATE DB
        FirebaseFirestore.getInstance()
                .collection("announcements").document(id).update(data)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            showPostSnack(1);
                            refreshContent(true);
                        } else {
                            showPostSnack(2);
                        }
                    }
                });
    }

    public void updateClub(final String name, final String desc, final Uri img, final int checked) {
        showPostSnack(8);

        final String imgName = ((club.getImgName() != null && !club.getImgName().isEmpty())
                ? club.getImgName().split("_")[0] : AppUtils.getRandomKey(20));
        if (img != null) {
            byte[] imgBytes = AppUtils.getImgBytes(img, 1280, 720, this);
            AppUtils.uploadImg(imgName, imgBytes, "clubBanners/",
                    new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> taskStorage) {
                    if(taskStorage.isSuccessful()){
                        updateDBClub(name, desc, img, imgName, checked);
                    }else{
                        showPostSnack(10);
                    }
                }
            });
        } else {
            updateDBClub(name, desc, img, imgName, checked);
        }
    }

    private void updateDBClub(final String name, final String desc, final Uri img, String imgName, final int checked){
        //GATHER DATA
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("desc", desc);
        if (img != null) {
            data.put("img", imgName);
        }
        data.put("joinPref", checked);

        //UPDATE DB
        FirebaseFirestore.getInstance()
                .collection("clubs").document(getClub().getId()).update(data)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //IF THE CLUB BECOMES OPEN
                            if(checked == 2){
                                //ADD ALL MEMBERS FROM THE PENDING LIST
                                List<String> pendingList = club.getPendingList();
                                if(pendingList != null
                                        && !pendingList.isEmpty()){
                                    for(String userId : pendingList){
                                        club.addUser(userId, null);
                                    }
                                }
                            }

                            //IF THE CLUB BECOMES OPEN OR CLOSED
                            if(checked == 2 || checked == 0){
                                //RESET PENDING LIST
                                FirebaseFirestore.getInstance().collection("clubs")
                                        .document(club.getId()).update("pending", new ArrayList<String>());
                            }

                            showPostSnack(9);

                            //UPDATE CLUB STUFF
                            club.setJoinPref(checked);
                            club.setName(name);
                            club.setDesc(desc);
                            cdName.setText(name);
                            cdName2.setText(name);
                            cdDesc.setText(desc);
                            if(img != null){
                                Picasso.with(ClubDetails.this)
                                        .load(img)
                                        .into(cdBanner);
                            }
                            //UPDATE CLUBS WHEN USER EXITS ClubDetails
                            ClubsFragment.REFRESH_CLUBS = true;
                        } else {
                            showPostSnack(10);
                        }
                    }
                });
    }

    public void deleteAnnoun(final String id){
        showPostSnack(3);

        //DELETE ANNOUNCEMENT FROM CLUB ANNOUNCEMENTS ARRAY
        FirebaseFirestore.getInstance().collection("clubs").document(club.getId())
                .update("announcements", FieldValue.arrayRemove(id))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> clubTask) {
                        if (clubTask.isSuccessful()) {
                            //DELETE IMG IF ANY
                            deleteImg(id);

                            //DELETE ANNOUNCEMENT FROM ANNOUNCEMENTS COLLECTION
                            FirebaseFirestore.getInstance().collection("announcements").document(id)
                                    .delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                showPostSnack(4);
                                                refreshContent(false);
                                            } else {
                                                showPostSnack(5);
                                            }
                                        }
                                    });
                        }else{
                            showPostSnack(5);
                        }
                    }
                });
    }

    public void deleteImg(String id){
        //FIND ANNOUNCEMENT THAT IS GETTING DELETED
        ClubAnnouncement announ = null;
        for(ClubAnnouncement anAnnoun : announs){
            if(anAnnoun.getId().equals(id)){
                announ = anAnnoun;
                break;
            }
        }

        //DELETE ANNOUNCEMENT'S IMAGE IN STORAGE
        if(announ != null && announ.getImgName() != null && !announ.getImgName().isEmpty()){
            FirebaseFirestore.getInstance().collection("announcements")
                    .document(id).update("img", "");

            FirebaseStorage.getInstance().getReference()
                    .child("announcements/" + announ.getImgName())
                    .delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        showPostSnack(6);
                        refreshContent(false);
                    }else{
                        showPostSnack(7);
                    }
                }
            });
        }

        //DELETE ALL COPIES OF IMAGE ON THE DEVICE
        AppUtils.deleteExtraImgs(announ.getImgName(), 0, this);
    }

    private void showPostSnack(int id){
        if(this != null && !this.isDestroyed()){
            View view = (View) this.findViewById(R.id.cdAnnounHeader);
            String snack = "";
            switch (id) {
                case 0:
                    snack = "Posting Announcement... Don't Go Anywhere!!";
                    break;
                case 1:
                    snack = "Successfully Posted Announcement!";
                    break;
                case 2:
                    snack = "Failed to Post!";
                    break;
                case 3:
                    snack = "Deleting Announcement... Don't Go Anywhere!!";
                    break;
                case 4:
                    snack = "Successfully Deleted Announcement :(";
                    break;
                case 5:
                    snack = "Failed to Delete!";
                    break;
                case 6:
                    snack = "Successfully Deleted Image :(";
                    break;
                case 7:
                    snack = "Failed to Delete Image!";
                    break;
                case 8:
                    snack = "Updating Club... Don't Go Anywhere!!";
                    break;
                case 9:
                    snack = "Successfully Updated Club!";
                    break;
                case 10:
                    snack = "Failed to Update Club!";
                    break;
                case 11:
                    snack = "Creating Badge...";
                    break;
                case 12:
                    snack = "Successfully Created Badge!";
                    break;
                case 13:
                    snack = "Failed to Create Badge!";
                    break;
                case 14:
                    snack = "Updating Club Badge...";
                    break;
                case 15:
                    snack = "Successfully Updated Club Badge!";
                    break;
                case 16:
                    snack = "Couldn't Update Club Badge!";
                    break;
            }
            Snackbar.make(view, snack, Snackbar.LENGTH_LONG).show();
        }
    }

    public void announHeld(View view){
        EditAnnounDialog dialog = new EditAnnounDialog();
        dialog.setClubDetails(this);
        dialog.setId((String) view.getTag());
        dialog.setMode(0);
        dialog.show(this.getSupportFragmentManager(), "editAnnounDialog");
    }

    public void updateBadges(List<Badge> badges) {
        cdLoadingCircle.setVisibility(View.GONE);

        TextView cdBadgesError = this.findViewById(R.id.cdBadgesError);
        if(badges.isEmpty() || badges.get(0) == null){
            //NO BADGES, TELL THE USER
            if(Main.PROFILE.getStatus() == 2){
                FrameLayout.LayoutParams params2 = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                params2.gravity = Gravity.CENTER_HORIZONTAL;
                LinearLayout layout = findViewById(R.id.cdInnerBadgesGroup);
                layout.setLayoutParams(params2);

                cdBadgesAdd.setPadding(0, AppUtils.dpToPx(4, this), 0, 0);

                cdBadgesError.setText("Create New Badge");
                cdBadgesError.setPadding(0, AppUtils.dpToPx(2, this), 0, 0);

                View cdBadgesExtras = findViewById(R.id.cdBadgesExtras);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.BELOW, R.id.cdBadgesGroup);
                cdBadgesExtras.setLayoutParams(params);
            }

            cdBadgesError.setVisibility(View.VISIBLE);
        }else{
            this.badges = badges;

            cdBadgesError.setVisibility(View.GONE);

            RViewAdapter_Badges adapter = new RViewAdapter_Badges(this.badges, this);
            rv.setAdapter(adapter);

            View cdAnnountHeader = findViewById(R.id.cdAnnounHeader);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.BELOW, R.id.cdBadgesGroup);
            cdAnnountHeader.setLayoutParams(params);

            View rvView = this.findViewById(R.id.cdBadges);
            rvView.setVisibility(View.VISIBLE);
        }

        if(Main.PROFILE.getStatus() == 2){
            cdBadgesAdd.setVisibility(View.VISIBLE);
        }

        cdScrollView.requestFocus();
        cdScrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                cdScrollView.fullScroll(View.FOCUS_UP);
            }
        }, 1L);
    }

    public void updateAnnouns(List<ClubAnnouncement> announs){
        this.announs = announs;

        ////SHOW ANNOUNCEMENTS////
        cdLoadingCircle2.setVisibility(View.GONE);
        View cdAnnounError = this.findViewById(R.id.cdAnnounError);
        if(this.announs == null || this.announs.isEmpty()){
            //NO ANNOUNCEMENTS, TELL THE USER
            cdAnnounError.setVisibility(View.VISIBLE);
        }else{
            cdAnnounError.setVisibility(View.GONE);

            //SORT ANNOUNCEMENTS BY DATE
            Collections.sort(this.announs, new Comparator<ClubAnnouncement>() {
                @Override
                public int compare(ClubAnnouncement o1, ClubAnnouncement o2) {
                    if(o1.getDate().before(o2.getDate())){
                        return -1;
                    }else{
                        return 1;
                    }
                }
            });

            cdLoadingCircle2.setVisibility(View.GONE);

            if(adapter == null){
                adapter = new RViewAdapter_ClubAnnouns(this.announs, this);
                rv2.setAdapter(adapter);
            }else{
                adapter.setAnnouncements(announs);
            }

            List<String> users = new ArrayList<String>();
            for(ClubAnnouncement announ : announs){
                if(!adapter.containsCreator(announ.getCreator())) {
                    users.add(announ.getCreator());
                }
            }

            //FOR THE LAST TIME, DON'T DO THISS!!!
            @SuppressLint("StaticFieldLeak")
            GetUserTask task = new GetUserTask(this, users){
                @Override
                protected void onPostExecute(List<UserProfile> users) {
                    adapter.addCreators(users);
                }
            };
            task.execute();

            View rvView = this.findViewById(R.id.cdAnnouncements);
            rvView.setVisibility(View.VISIBLE);
        }

        cdSwipeRefresh.setRefreshing(false);
        cdSwipeRefresh.setEnabled(true);

        cdScrollView.requestFocus();
        cdScrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                cdScrollView.fullScroll(View.FOCUS_UP);
            }
        }, 1L);
    }

    public void createBadge(Uri selectedImage, String desc, final boolean clubBadge) {
        final Map<String, Object> data = new HashMap<String, Object>();
        data.put("club", club.getId());
        data.put("desc", desc);
        data.put("giveaway", false);
        String imgName = AppUtils.getRandomKey(20);
        data.put("img", imgName);
        data.put("creator", Main.PROFILE.getUid());

        showPostSnack(11);

        byte[] imgBytes = AppUtils.getImgBytes(selectedImage, 300, 300, this);
        AppUtils.uploadImg(imgName, imgBytes, "badges/",
                new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        final DocumentReference doc = FirebaseFirestore.getInstance()
                                .collection("badges").document();

                        doc.set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    if(clubBadge){
                                        //UPDATE CLUB TO HAVE CLUB BADGE
                                        FirebaseFirestore.getInstance().collection("clubs")
                                                .document(club.getId())
                                                .update("clubBadge", doc.getId())
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            showPostSnack(12);
                                                            restartClub();
                                                        }else{
                                                            showPostSnack(13);
                                                        }
                                                    }
                                                });

                                        //GIVE EXISTING MEMBERS AND ADMINS THE BADGE
                                        for(String member : club.getMembers()){
                                            FirebaseFirestore.getInstance()
                                                    .collection("users").document(member)
                                                    .update("badges", FieldValue.arrayUnion(doc.getId()));
                                        }

                                        for(String admin : club.getAdmins()){
                                            FirebaseFirestore.getInstance()
                                                    .collection("users").document(admin)
                                                    .update("badges", FieldValue.arrayUnion(doc.getId()));
                                        }
                                    }else{
                                        refreshContent(true);
                                        showPostSnack(12);
                                    }
                                }else{
                                    //DELETE BADGE IMG
                                    showPostSnack(13);
                                }
                            }
                        });
                    }
                });
    }

    public void updateClubBadge(Uri selectedImage, String imgName, String desc) {
        showPostSnack(14);

        if(selectedImage != null){
            byte[] imgBytes = AppUtils.getImgBytes(selectedImage, 300, 300, this);
            AppUtils.uploadImg(imgName, imgBytes, "badges/", null);
        }

        FirebaseFirestore.getInstance().collection("badges")
                .document(club.getClubBadge())
                .update("desc", desc)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            refreshContent(true);
                            showPostSnack(15);
                        }else{
                            showPostSnack(16);
                        }
                    }
                });
    }

    public void setOffline(){
        /*layout.addView(offline);
        progressBar.setVisibility(View.GONE);
        extrasClubs.setVisibility(View.GONE);*/
    }

    public void refreshContent(boolean refreshClub){
        cdSwipeRefresh.setRefreshing(true);

        if(refreshClub){
            //UPDATE CLUB DATA
            FirebaseFirestore.getInstance().collection("clubs").document(club.getId())
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        final DocumentSnapshot doc = task.getResult();
                        club = new ClubItem(doc.getId(), doc.getData(), club.getImg(), club.getImgName());

                        refreshContent(false);
                    }
                }
            });
        }else{
            //UPDATE CONTENT
            getClubAnnounsTask = new GetClubAnnounsTask(this.club.getId(), this, this);
            getClubAnnounsTask.execute();

            getBadgesTask = new GetBadgesTask(this);
            getBadgesTask.execute();
        }
    }

    public void restartClub(){
        ClubsFragment.REFRESH_CLUBS = true;
        if(!this.isDestroyed()){
            finish();
        }
    }

    @Override
    public void onClick(final View v) {
        if(v.getId() == R.id.cdAddAnnouncement){
            AddClubAnnounDialog dialog = new AddClubAnnounDialog();
            dialog.setClubDetails(this);
            dialog.show(this.getSupportFragmentManager(), "addClubDialog");
        }else if(v.equals(cdJoinBtn)){
            if(club.getJoinPref() == 1){
                Map<String, Object> data = new HashMap<String, Object>();
                data.put("userEmail", Main.PROFILE.getEmail());
                data.put("adminIDArr", club.getAdmins());
                data.put("clubName", club.getName());

                //SEND EMAIL TO ADMINS
                FirebaseFunctions.getInstance().getHttpsCallable("sendEmailToAdmins").call(data);

                //ADD USER TO THE CLUB'S PENDING LIST
                FirebaseFirestore.getInstance().collection("clubs")
                        .document(club.getId())
                        .update("pending", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(ClubDetails.this);
                                builder.setMessage("New members require admin approval to join this club. " +
                                        "You'll receive a notification once you have been accepted.");
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                        restartClub();
                                    }
                                });
                                builder.create().show();
                            }
                        });

                //DISABLE JOIN BUTTON
                cdJoinBtn.setEnabled(false);
                Snackbar.make(cdScrollView, "Joining club...", Snackbar.LENGTH_LONG).show();
            }else if(club.getJoinPref() == 2){
                club.addUser(Main.PROFILE.getUid(), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        restartClub();
                    }
                });

                Snackbar.make(cdScrollView, "Joining club...", Snackbar.LENGTH_LONG).show();
            }
        }else if(v.equals(cdBadgesAdd)){
            CreateBadgeDialog dialog = new CreateBadgeDialog();
            dialog.setClubDetails(this);
            dialog.show(this.getSupportFragmentManager(), "CreateBadgeDialog");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                float deltaX = event.getX() - downX;
                float deltaY = event.getY() - downY;
                if(Math.abs(deltaY) < thresholdY && (Math.abs(deltaX) > threshold && deltaX > 0)){
                    NavUtils.navigateUpFromSameTask(this);
                }
                break;
            case MotionEvent.ACTION_SCROLL:
                cdScrollView.smoothScrollBy((int) (event.getAxisValue(MotionEvent.AXIS_HSCROLL)),
                        (int) (event.getAxisValue(MotionEvent.AXIS_VSCROLL)));
                break;
        }
        return true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return onTouchEvent(event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(isAdmin){
            getMenuInflater().inflate(R.menu.options_clubadmin, menu);
            itemMemberList = menu.getItem(0).getItemId();
            menu.getItem(0).setOnMenuItemClickListener(this);
            if(club.getJoinPref() == 1){
                itemPendingList = menu.getItem(1).getItemId();
                menu.getItem(1).setOnMenuItemClickListener(this);
            }else{
                menu.getItem(1).setVisible(false);
            }
            itemEditClub = menu.getItem(2).getItemId();
            menu.getItem(2).setOnMenuItemClickListener(this);
            itemClubBadge = menu.getItem(3).getItemId();
            if(club.getClubBadge() == null || club.getClubBadge().isEmpty()){
                menu.getItem(3).setTitle("Create Club Badge");
            }
            menu.getItem(3).setOnMenuItemClickListener(this);
            itemToggleNotifs = menu.getItem(4).getItemId();
            if(!Main.PROFILE.getNotifications().contains(club.getId())){
                menu.getItem(4).setTitle("Enable Notifications");
            }
            menu.getItem(4).setOnMenuItemClickListener(this);
            itemLeaveClub = menu.getItem(5).getItemId();
            menu.getItem(5).setOnMenuItemClickListener(this);
        }else{
            if(isMember || club.getPendingList().contains(Main.PROFILE.getUid())) {
                getMenuInflater().inflate(R.menu.options_club, menu);
                itemLeaveClub = menu.getItem(2).getItemId();
                menu.getItem(2).setOnMenuItemClickListener(this);
                if (isMember) {
                    itemMemberList = menu.getItem(0).getItemId();
                    menu.getItem(0).setOnMenuItemClickListener(this);
                    itemToggleNotifs = menu.getItem(1).getItemId();
                    if(!Main.PROFILE.getNotifications().contains(club.getId())){
                        menu.getItem(1).setTitle("Enable Notifications");
                    }
                    menu.getItem(1).setOnMenuItemClickListener(this);
                } else {
                    menu.getItem(0).setVisible(false);
                    menu.getItem(2).setTitle("Cancel Application");
                }
            }
        }
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if(item.getItemId() == itemMemberList){
            Intent intent = new Intent(this, ClubMemberList.class);
            intent.putExtra("CLUB", club.pack());
            intent.putExtra("PENDING", false);
            startActivity(intent);
        }else if(item.getItemId() == itemPendingList){
            Intent intent = new Intent(this, ClubMemberList.class);
            intent.putExtra("CLUB", club.pack());
            intent.putExtra("PENDING", true);
            startActivity(intent);
        }else if(item.getItemId() == itemEditClub){
            EditClubDialog dialog = new EditClubDialog();
            dialog.setClubDetails(this);
            dialog.show(this.getSupportFragmentManager(), "editClubDialog");
        }else if(item.getItemId() == itemClubBadge){
            final CreateBadgeDialog dialog = new CreateBadgeDialog();
            dialog.setClubDetails(this);
            if(club.getClubBadge() != null && !club.getClubBadge().isEmpty()){
                for(Badge badge : this.badges){
                    if(badge.getId().equals(club.getClubBadge())){
                        dialog.setBadge(badge);
                        dialog.show(this.getSupportFragmentManager(), "createClubBadgeDialog");
                        break;
                    }
                }
            }else{
                dialog.setClubBadge(true);
                dialog.show(this.getSupportFragmentManager(), "createClubBadgeDialog");
            }
        }else if(item.getItemId() == itemToggleNotifs){
            Snackbar.make(cdBanner, "Updating Notification Preferences....", Snackbar.LENGTH_LONG).show();
            if(!Main.PROFILE.getNotifications().contains(club.getId())){
                Main.PROFILE.addNotification(club.getId(), new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            restartClub();
                        }else{
                            if(!isDestroyed()){
                                Snackbar.make(cdBanner, "Error Updating Notification Preferences!",
                                        Snackbar.LENGTH_LONG).show();
                            }
                        }
                    }
                });
            }else{
                Main.PROFILE.removeNotification(club.getId(), new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            restartClub();
                        }else{
                            if(!isDestroyed()){
                                Snackbar.make(cdBanner, "Error Updating Notification Preferences!",
                                        Snackbar.LENGTH_LONG).show();
                            }
                        }
                    }
                });
            }
        }else if(item.getItemId() == itemLeaveClub){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            if(isMember){
                builder.setMessage("Are you sure you want to leave " + club.getName().trim() + "?");
            }else{
                builder.setMessage("Are you sure you want to cancel your application to " + club.getName() + "?");
            }
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    club.removeMember(Main.PROFILE);
                    restartClub();
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton("NO!", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        }

        return true;
    }

    @Override
    public void onScrollChanged() {
        if(cdName != null && cdName2 != null){
            if(cdNameY == 0){
                cdNameY = (int) cdName.getY();
            }

            if(cdNameY < cdScrollView.getScrollY()){
                cdName2.setVisibility(View.VISIBLE);
            }else{
                cdName2.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        if(getClubAnnounsTask != null){
            getClubAnnounsTask.cancel(true);
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        if(NEEDS_UPDATE){
            refreshContent(true);
            NEEDS_UPDATE = false;
        }
        super.onResume();
    }

    public boolean isMember(){
        return isMember;
    }

    public boolean isAdmin(){
        return isAdmin;
    }

    public ClubItem getClub(){
        return club;
    }

    public List<ClubAnnouncement> getAnnouns(){
        return announs;
    }
}
