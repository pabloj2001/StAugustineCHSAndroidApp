package ca.staugustinechs.staugustineapp.Activities;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.AsyncTasks.GetUserTask;
import ca.staugustinechs.staugustineapp.Objects.ClubItem;
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
        //SET ACTIVITY TITLE, SHOW THE BACK ARROW ON THE TOP LEFT, AND CHANGE THE COLORS
        this.getSupportActionBar().setTitle("Club Member List");
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(AppUtils.PRIMARY_COLOR));

        //GET REFERENCE OF VIEWS
        cmlMemberText = findViewById(R.id.cmlMemberText);
        cmlMemberText.setTextColor(AppUtils.PRIMARY_COLOR);
        cmlAdminText = findViewById(R.id.cmlAdminText);
        cmlAdminText.setTextColor(AppUtils.PRIMARY_COLOR);
        cmlMemberList = findViewById(R.id.cmlMemberList);
        cmlAdminList = findViewById(R.id.cmlAdminList);

        cmlLoadingCircle = findViewById(R.id.cmlLoadingCircle);
        cmlLoadingCircle.getIndeterminateDrawable().setTint(AppUtils.ACCENT_COLOR);

        //LOAD CLUB
        club = (ClubItem) getIntent().getExtras().getSerializable("CLUB");
        //ARE WE SHOWING THE MEMBERS LIST OR THE PENDING LIST?
        pendingList = getIntent().getExtras().getBoolean("PENDING");
        //IS THE USER AN ADMIN? (AKA CAN THE USER MANAGE USERS?)
        isAdmin = club.getAdmins().contains(Main.PROFILE.getUid()) || Main.PROFILE.getStatus() == Main.DEV;

        if(!pendingList){
            //GET MEMBERS AND ADMINS
            this.getMemberProfiles();
            this.getAdminProfiles();
        }else{
            if(club.getPendingList().isEmpty()){
                //SHOW TEXT SAYING THE LIST IS EMPTY
                TextView cmlPendingError = findViewById(R.id.cmlPendingError);
                cmlPendingError.setVisibility(View.VISIBLE);
                updateViews();
            }else{
                //GET PENDING MEMBERS
                this.getMemberProfiles();
            }
            cmlMemberText.setText("Pending Members");
            cmlAdminText.setVisibility(View.GONE);
            cmlAdminList.setVisibility(View.GONE);
        }

        //SETUP MEMBERS AND ADMINS RECYCLERVIEW LISTS
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
        //FETCH CLUB MEMBERS OR PENDING MEMBERS DEPENDING ON IF THIS IS THE PENDING LIST
        memberTask = new GetUserTask(this, pendingList ? club.getPendingList() : club.getMembers()){
            //BAD PRACTICE KIDS. OVERRIDING A METHOD LIKE THIS IS PROBABLY NOT THE BEST WAY TO DO IT.
            //I'M JUST LAZY AND DON'T WANT TO IMPLEMENT AN INTERFACE.
            @Override
            protected void onPostExecute(List<UserProfile> users) {
                if(users == null){
                    users = new ArrayList<UserProfile>();
                }

                //SET THE RECYCLERVIEW ADAPTER FOR THE MEMBERS/PENDING LIST
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
        //GET CLUB ADMINS
        adminTask = new GetUserTask(this, club.getAdmins()){
            @Override
            protected void onPostExecute(List<UserProfile> users) {
                if(users == null){
                    users = new ArrayList<UserProfile>();
                }

                //SET THE RECYCLERVIEW ADAPTER FOR THE ADMINS LSIT
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
            //IF BOTH THE MEMBERS LIST AND ADMINS LIST HAVE LOADED, SHOW THEM
            cmlLoadingCircle.setVisibility(View.GONE);
            View cmlMembersGroup = findViewById(R.id.cmlMembersGroup);
            cmlMembersGroup.setVisibility(View.VISIBLE);
        }
    }

    public void promote(UserProfile user) {
        //MAKE SELECTED USER ADMIN
        club.makeAdmin(user);

        //REMOVE SELECTED USER FROM THE VISUAL MEMBERS LIST AND ADD THEM TO THE VISUAL ADMINS LIST 
        ((RViewAdapter_MemberList) cmlMemberList.getAdapter()).removeItem(user);
        cmlMemberList.getAdapter().notifyDataSetChanged();
        ((RViewAdapter_MemberList) cmlAdminList.getAdapter()).addItem(user);
        cmlAdminList.getAdapter().notifyDataSetChanged();
        
        //NOTIFY CLUB DETAILS ACTIVITY IT NEEDS TO UPDATE THE CLUB ONCE WE EXIT THIS ACTIVITY
        ClubDetails.NEEDS_UPDATE = true;
    }

    public void removeUser(UserProfile user) {
        //REMOVE SELECTED USER FROM CLUB
        int type = club.removeMember(user);
        if(type == ClubMemberList.ADMINS){
            //REMOVE SELECTED USER FROM VISUAL ADMINS LIST
            ((RViewAdapter_MemberList) cmlAdminList.getAdapter()).removeItem(user);
            cmlAdminList.getAdapter().notifyDataSetChanged();
        }else if(type == ClubMemberList.MEMBERS || type == ClubMemberList.PENDING){
            //REMOVE SELECTED USER FROM VISUAL MEMBERS/PENDING LIST
            ((RViewAdapter_MemberList) cmlMemberList.getAdapter()).removeItem(user);
            cmlMemberList.getAdapter().notifyDataSetChanged();
        }

        //NOTIFY CLUB DETAILS ACTIVITY IT NEEDS TO UPDATE THE CLUB ONCE WE EXIT THIS ACTIVITY
        ClubDetails.NEEDS_UPDATE = true;
    }

    public void demote(UserProfile user) {
        //MAKE SELECTED USER A MEMBER INSTEAD OF AN ADMIN 
        club.demoteAdmin(user);

        //REMOVE SELECTED USER FROM VISUAL ADMINS LIST
        ((RViewAdapter_MemberList) cmlAdminList.getAdapter()).removeItem(user);
        cmlAdminList.getAdapter().notifyDataSetChanged();
        //ADD SELECTED USER TO VISUAL MEMBERS LIST
        ((RViewAdapter_MemberList) cmlMemberList.getAdapter()).addItem(user);
        cmlMemberList.getAdapter().notifyDataSetChanged();
        
        //NOTIFY CLUB DETAILS ACTIVITY IT NEEDS TO UPDATE THE CLUB ONCE WE EXIT THIS ACTIVITY
        ClubDetails.NEEDS_UPDATE = true;
    }

    public void accept(UserProfile user) {
        //ADD SELECTED USER TO CLUB MEMBERS LIST AND REMOVE FROM PENDING LIST
        club.acceptUser(user);

        //REMOVE SELECTED USER FROM VISUAL PENDING LIST
        ((RViewAdapter_MemberList) cmlMemberList.getAdapter()).removeItem(user);
        cmlMemberList.getAdapter().notifyDataSetChanged();
        
        //NOTIFY CLUB DETAILS ACTIVITY IT NEEDS TO UPDATE THE CLUB ONCE WE EXIT THIS ACTIVITY
        ClubDetails.NEEDS_UPDATE = true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //FINISH ACTIVITY WHEN BACK ARROW IS PRESSED IN TOP LEFT CORNER
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        //CANCEL RUNNING TASKS
        if(memberTask != null){
            memberTask.cancel(true);
        }

        if(adminTask != null){
            adminTask.cancel(true);
        }

        //TELL JAVA TO MANUALLY BEGIN GARBAGE COLLECTION IN ORDER TO AVOID
        //MEMORY LEAKS AND PROBLEMS THAT MIGHT COME WITH OVERRIDING
        //THE GET USERS TASKS RESULT METHOD
        System.gc();
        super.onDestroy();
    }
}
