package ca.staugustinechs.staugustineapp.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import ca.staugustinechs.staugustineapp.Activities.Main;
import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.AsyncTasks.GetClubsTask;
import ca.staugustinechs.staugustineapp.DialogFragments.EditClubDialog;
import ca.staugustinechs.staugustineapp.Objects.ClubItem;
import ca.staugustinechs.staugustineapp.R;
import ca.staugustinechs.staugustineapp.RVAdapters.RViewAdapter_Clubs;

public class ClubsFragment extends Fragment implements MenuItem.OnMenuItemClickListener {

    public static boolean REFRESH_CLUBS = false;

    private RecyclerView rv;
    private GetClubsTask getClubsTask;
    private LinearLayout layout;
    private View offline;
    private ProgressBar progressBar;
    private NestedScrollView extrasGroup;
    private List<ClubItem> clubs;
    private int mode = GetClubsTask.JOINED;
    private SwipeRefreshLayout extrasSwipeRefresh;
    private boolean isTeacher;
    private int itemCreateClub;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isTeacher = Main.PROFILE != null && Main.PROFILE.getStatus() >= Main.TEACHER;
        setHasOptionsMenu(isTeacher);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_clubs, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if(bundle != null){
            mode = bundle.getInt("mode");
        }

        rv = (RecyclerView) getView().findViewById(R.id.rv);

        layout = (LinearLayout) getView().findViewById(R.id.extrasLayout);
        offline = getLayoutInflater().inflate(R.layout.offline_layout, null);
        progressBar = (ProgressBar) getView().findViewById(R.id.extrasLoadingCircle);
        progressBar.getIndeterminateDrawable().setTint(AppUtils.ACCENT_COLOR);
        extrasGroup = getView().findViewById(R.id.extrasGroup);

        extrasSwipeRefresh = (SwipeRefreshLayout) getView().findViewById(R.id.extrasSwipeRefresh);

        if(Main.PROFILE != null){
            extrasSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    refreshClubs();
                }
            });
            extrasSwipeRefresh.setColorSchemeColors(AppUtils.ACCENT_COLOR);
            extrasSwipeRefresh.setEnabled(false);

            Button extrasBtn = (Button) getView().findViewById(R.id.extrasBtn);
            extrasBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    swapClubMode();
                }
            });

            if(mode == GetClubsTask.OTHERS){
               /*LinearLayout innerLayout = getView().findViewById(R.id.extrasInnerLayout);
                innerLayout.removeView(extrasBtn);
                innerLayout.addView(extrasBtn, 0);*/
                extrasBtn.setText("BACK TO MY CLUBS");
            }

            rv.setHasFixedSize(false);
            // use a linear layout manager
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext()){
                @Override
                public boolean canScrollVertically() {
                    return false;
                }
            };
            rv.setLayoutManager(layoutManager);

            //GET CLUBS
            getClubsTask = new GetClubsTask(this, mode);
            getClubsTask.execute();

            this.clubs = new ArrayList<ClubItem>();
        }else{
            setOffline();
        }
    }

    private void swapClubMode(){
        ClubsFragment fragment = new ClubsFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("mode", mode == GetClubsTask.JOINED ? GetClubsTask.OTHERS : GetClubsTask.JOINED);
        fragment.setArguments(bundle);

        FragmentTransaction rmTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        rmTransaction.remove(ClubsFragment.this);
        rmTransaction.commit();

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.main_fragment_container, fragment, ClubsFragment.this.getClass().getName());
        transaction.commit();
    }

    public void setOffline(){
        progressBar.setVisibility(View.GONE);
        extrasGroup.setVisibility(View.GONE);

        extrasSwipeRefresh.setRefreshing(false);
        extrasSwipeRefresh.setEnabled(true);

        layout.removeView(offline);
        layout.addView(offline);
    }

    public void updateClubs(List<ClubItem> clubs){
        this.clubs = clubs;

        layout.removeView(offline);
        progressBar.setVisibility(View.GONE);
        extrasGroup.setVisibility(View.VISIBLE);

        RViewAdapter_Clubs adapter = new RViewAdapter_Clubs(this.clubs, this.getActivity(), false);
        rv.setAdapter(adapter);

        extrasSwipeRefresh.setRefreshing(false);
        extrasSwipeRefresh.setEnabled(true);
    }

    public void refreshClubs(){
        extrasSwipeRefresh.setRefreshing(true);

        getClubsTask = new GetClubsTask(this, mode);
        getClubsTask.execute();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //CREATE OPTIONS MENU FOR TEACHERS TO CREATE CLUBS
        if(isTeacher){
            inflater.inflate(R.menu.options_clubs, menu);
            itemCreateClub = menu.getItem(0).setOnMenuItemClickListener(this).getItemId();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if(item.getItemId() == itemCreateClub){
            EditClubDialog dialog = new EditClubDialog();
            dialog.setClubsFragment(this);
            dialog.show(this.getFragmentManager(), "CreateClubDialog");
            return true;
        }
        return false;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if(!hidden){
           /* if(getClubsTask != null){
                getClubsTask.cancel(true);
                getClubsTask = null;
            }
        }else{*/
            extrasSwipeRefresh.requestFocus();
            extrasGroup.fullScroll(View.FOCUS_UP);
        }
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onResume() {
        if(ClubsFragment.REFRESH_CLUBS){
            ClubsFragment.REFRESH_CLUBS = false;
            this.extrasSwipeRefresh.postDelayed(new Runnable() {
                @Override
                public void run() {
                    refreshClubs();
                }
            }, 600L);
        }
        super.onResume();
    }
}