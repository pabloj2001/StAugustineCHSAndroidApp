package ca.staugustinechs.staugustineapp.Activities;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.List;

import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.AsyncTasks.GetProfileIconsTask;
import ca.staugustinechs.staugustineapp.AsyncTasks.GetUserTask;
import ca.staugustinechs.staugustineapp.Fragments.IconSelectFragment;
import ca.staugustinechs.staugustineapp.Interfaces.UserGetter;
import ca.staugustinechs.staugustineapp.MessagingService;
import ca.staugustinechs.staugustineapp.Objects.ClubItem;
import ca.staugustinechs.staugustineapp.Objects.ProfileIcon;
import ca.staugustinechs.staugustineapp.Objects.UserProfile;
import ca.staugustinechs.staugustineapp.R;
import ca.staugustinechs.staugustineapp.RVAdapters.RViewAdapter_MemberList;

public class ClubMemberList extends AppCompatActivity {

    public static int MEMBERS = 0, ADMINS = 1, PENDING = 2;

    private ClubItem club;
    private boolean pendingList, isAdmin;
    private TextView cmlMemberText, cmlAdminText;
    private RecyclerView cmlMemberList, cmlAdminList;
    private GetUserTask memberTask, adminTask;
    private ProgressBar cmlLoadingCircle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clubmemberlist);
        this.getSupportActionBar().setTitle("Club Member List");
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(AppUtils.PRIMARY_COLOR));

        cmlMemberText = findViewById(R.id.cmlMemberText);
        cmlMemberText.setTextColor(AppUtils.PRIMARY_COLOR);
        cmlAdminText = findViewById(R.id.cmlAdminText);
        cmlAdminText.setTextColor(AppUtils.PRIMARY_COLOR);
        cmlMemberList = findViewById(R.id.cmlMemberList);
        cmlAdminList = findViewById(R.id.cmlAdminList);

        cmlLoadingCircle = findViewById(R.id.cmlLoadingCircle);
        cmlLoadingCircle.getIndeterminateDrawable().setTint(AppUtils.ACCENT_COLOR);

        club = (ClubItem) getIntent().getExtras().getSerializable("CLUB");
        pendingList = getIntent().getExtras().getBoolean("PENDING");
        isAdmin = club.getAdmins().contains(Main.PROFILE.getUid()) || Main.PROFILE.getStatus() == Main.DEV;

        if(!pendingList){
            this.getMemberProfiles();
            this.getAdminProfiles();
        }else{
            if(club.getPendingList().isEmpty()){
                //SHOW TEXT SAYING THE LIST IS EMPTY
                TextView cmlPendingError = findViewById(R.id.cmlPendingError);
                cmlPendingError.setVisibility(View.VISIBLE);
                updateViews();
            }else{
                this.getMemberProfiles();
            }
            cmlMemberText.setText("Pending Members");
            cmlAdminText.setVisibility(View.GONE);
            cmlAdminList.setVisibility(View.GONE);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this){
            @Override
            public boolean canScrollVertically(){
                return false;
            }
        };
        cmlMemberList.setLayoutManager(layoutManager);

        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this){
            @Override
            public boolean canScrollVertically(){
                return false;
            }
        };
        cmlAdminList.setLayoutManager(layoutManager2);
    }

    private void getMemberProfiles(){
        memberTask = new GetUserTask(this, pendingList ? club.getPendingList() : club.getMembers()){
            //BAD PRACTICE KIDS. OVERRIDING A METHOD LIKE THIS IS PROBABLY NOT THE BEST WAY TO DO IT.
            @Override
            protected void onPostExecute(List<UserProfile> users) {
                if(users == null){
                    users = new ArrayList<UserProfile>();
                }

                RViewAdapter_MemberList adapter = new RViewAdapter_MemberList(users,
                        pendingList ? ClubMemberList.PENDING : ClubMemberList.MEMBERS,
                        isAdmin, ClubMemberList.this);
                cmlMemberList.setAdapter(adapter);
                updateViews();
            }
        };
        memberTask.execute();
    }

    private void getAdminProfiles(){
        adminTask = new GetUserTask(this, club.getAdmins()){
            @Override
            protected void onPostExecute(List<UserProfile> users) {
                if(users == null){
                    users = new ArrayList<UserProfile>();
                }

                RViewAdapter_MemberList adapter = new RViewAdapter_MemberList(users,
                        ClubMemberList.ADMINS, isAdmin, ClubMemberList.this);
                cmlAdminList.setAdapter(adapter);
                updateViews();
            }
        };
        adminTask.execute();
    }

    private void updateViews(){
        if(pendingList || (cmlAdminList.getAdapter() != null && cmlMemberList.getAdapter() != null)){
            cmlLoadingCircle.setVisibility(View.GONE);
            View cmlMembersGroup = findViewById(R.id.cmlMembersGroup);
            cmlMembersGroup.setVisibility(View.VISIBLE);
        }
    }

    public void promote(UserProfile user) {
        club.makeAdmin(user);

        ((RViewAdapter_MemberList) cmlMemberList.getAdapter()).removeItem(user);
        cmlMemberList.getAdapter().notifyDataSetChanged();
        ((RViewAdapter_MemberList) cmlAdminList.getAdapter()).addItem(user);
        cmlAdminList.getAdapter().notifyDataSetChanged();
        ClubDetails.NEEDS_UPDATE = true;
    }

    public void removeUser(UserProfile user) {
        int type = club.removeMember(user);
        if(type == ClubMemberList.ADMINS){
            ((RViewAdapter_MemberList) cmlAdminList.getAdapter()).removeItem(user);
            cmlAdminList.getAdapter().notifyDataSetChanged();
        }else if(type == ClubMemberList.MEMBERS || type == ClubMemberList.PENDING){
            ((RViewAdapter_MemberList) cmlMemberList.getAdapter()).removeItem(user);
            cmlMemberList.getAdapter().notifyDataSetChanged();
        }

        ClubDetails.NEEDS_UPDATE = true;
    }

    public void demote(UserProfile user) {
        club.demoteAdmin(user);

        ((RViewAdapter_MemberList) cmlAdminList.getAdapter()).removeItem(user);
        cmlAdminList.getAdapter().notifyDataSetChanged();
        ((RViewAdapter_MemberList) cmlMemberList.getAdapter()).addItem(user);
        cmlMemberList.getAdapter().notifyDataSetChanged();
        ClubDetails.NEEDS_UPDATE = true;
    }

    public void accept(UserProfile user) {
        club.acceptUser(user);

        ((RViewAdapter_MemberList) cmlMemberList.getAdapter()).removeItem(user);
        cmlMemberList.getAdapter().notifyDataSetChanged();
        ClubDetails.NEEDS_UPDATE = true;
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
    protected void onDestroy() {
        if(memberTask != null){
            memberTask.cancel(true);
        }

        if(adminTask != null){
            adminTask.cancel(true);
        }

        System.gc();
        super.onDestroy();
    }
}
