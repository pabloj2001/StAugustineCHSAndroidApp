package ca.staugustinechs.staugustineapp.Fragments;

import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import ca.staugustinechs.staugustineapp.Activities.ClubDetails;
import ca.staugustinechs.staugustineapp.Activities.Main;
import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.AsyncTasks.GetClubAnnounsTask;
import ca.staugustinechs.staugustineapp.AsyncTasks.GetWebsiteTask;
import ca.staugustinechs.staugustineapp.Interfaces.ClubAnnounGetter;
import ca.staugustinechs.staugustineapp.Objects.ClubAnnouncement;
import ca.staugustinechs.staugustineapp.Objects.NewsItem;
import ca.staugustinechs.staugustineapp.Objects.SongItem;
import ca.staugustinechs.staugustineapp.R;
import ca.staugustinechs.staugustineapp.RVAdapters.RViewAdapter_ClubAnnouns;
import ca.staugustinechs.staugustineapp.RVAdapters.RViewAdapter_Home;

public class HomeFragment extends Fragment implements ClubAnnounGetter {

    private String CALENDAR_URL = "https://calendar.google.com/calendar/r?" +
            "cid=ycdsbk12.ca_f456pem6p0idarcilfuqiakaa8@group.calendar.google.com" +
            "&cid=ycdsbk12.ca_4tepqngmnt9htbg435bmbpf3tg%40group.calendar.google.com";

    private Main main;
    private RecyclerView announcements;
    private LinearLayout layout;
    private View offline;
    private View dateGroupView;
    private ProgressBar progressBar;
    private GetWebsiteTask task;
    private List<GetClubAnnounsTask> clubAnnounTasks;
    private SwipeRefreshLayout homeSwipeRefresh;
    private ProgressBar homeClubLoading;
    private Button calendar;
    private NestedScrollView homeScrollView;
    private RecyclerView rv;
    private RViewAdapter_ClubAnnouns rvAdapter;
    private View clubGroup;
    private TextView announError;
    private boolean newsLoaded = false;
    private boolean requestedRefresh = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if(savedInstanceState == null){
            layout = (LinearLayout) view.findViewById(R.id.homeLayout);
            offline = getLayoutInflater().inflate(R.layout.offline_layout, null);

            dateGroupView = (View) view.findViewById(R.id.dateGroup);
            dateGroupView.setBackgroundColor(AppUtils.ACCENT_COLOR);
            progressBar = (ProgressBar) view.findViewById(R.id.homeLoadingCircle);
            progressBar.getIndeterminateDrawable().setTint(AppUtils.ACCENT_COLOR);
            clubGroup = view.findViewById(R.id.homeClubGroup);
            homeClubLoading = view.findViewById(R.id.homeClubLoading);
            homeClubLoading.getIndeterminateDrawable().setTint(AppUtils.ACCENT_COLOR);
            announError = view.findViewById(R.id.homeAnnounError);

            homeSwipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.homeSwipeRefresh);
            homeSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    requestedRefresh = true;
                    refreshAnnouns();
                }
            });
            homeSwipeRefresh.setColorSchemeColors(AppUtils.ACCENT_COLOR);
            homeSwipeRefresh.setEnabled(false);

            homeScrollView = (NestedScrollView) view.findViewById(R.id.homeScrollView);

            announcements = (RecyclerView) view.findViewById(R.id.homeAnnouncements);
            announcements.setHasFixedSize(true);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext()){
                @Override
                public boolean canScrollVertically() {
                    return false;
                }
            };
            announcements.setLayoutManager(layoutManager);

            //GET CALENDAR
            calendar = (Button) getView().findViewById(R.id.calendar);
            calendar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean calendarAdded = AppUtils.loadCalendarAdded(getActivity());
                    if(calendarAdded){
                        Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
                        builder.appendPath("time");
                        ContentUris.appendId(builder, Calendar.getInstance().getTimeInMillis());
                        Intent intent = new Intent(Intent.ACTION_VIEW).setData(builder.build());
                        startActivity(intent);
                    }else{
                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                        builder.setToolbarColor(AppUtils.PRIMARY_COLOR);
                        CustomTabsIntent customTabsIntent = builder.build();
                        customTabsIntent.launchUrl(getContext(), Uri.parse(CALENDAR_URL));
                        AppUtils.saveCalendarAdded(true, getActivity());
                    }
                }
            });

            TextView homeClubAnnounsHeader = view.findViewById(R.id.homeClubAnnounsHeader);
            homeClubAnnounsHeader.setTextColor(AppUtils.PRIMARY_COLOR);

            //CLUB ANNOUN RECYCLER VIEW
            rv = (RecyclerView) view.findViewById(R.id.rv);
            rv.setHasFixedSize(true);

            // use a linear layout manager
            LinearLayoutManager layoutManager2 = new LinearLayoutManager(this.getContext()){
                @Override
                public boolean canScrollVertically() {
                    return false;
                }
            };
            rv.setLayoutManager(layoutManager2);
        }
    }

    public void updateAnnouncements(String source){
        if(!this.isHidden() && getView() != null){
            source = source != null && source.contains("<title>St Augustine CHS") ? source : null;

            List<NewsItem> newsItems = getNewsItems(source);
            if (newsItems.size() > 0) {
                RViewAdapter_Home adapter = new RViewAdapter_Home(newsItems);
                announcements.setAdapter(adapter);
                newsLoaded = true;
            } else if (AppUtils.isNetworkAvailable(this.getActivity())) {
                newsLoaded = true;
            } else {
                setOffline();
                newsLoaded = false;
            }

            //UPDATE DATE
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("EST"));
            //cal.setTimeInMillis(System.currentTimeMillis() + (1000 * 60 * 60 * 24));
            DateFormat date = new SimpleDateFormat("EEEE, MMMM d, yyyy");
            String today = date.format(cal.getTime());

            TextView dateView = (TextView) getView().findViewById(R.id.date);
            dateView.setText(today);

            updateDay(today);
            showViews();
        }
    }

    @Override
    public void updateAnnouns(List<ClubAnnouncement> rawClubAnnouns) {
        System.out.println("UPDATING CLUB ANNOUNS");
        if(rawClubAnnouns != null){
            List<ClubAnnouncement> clubAnnouns = new ArrayList<ClubAnnouncement>();

            long DAY_IN_MS = 1000 * 60 * 60 * 24;
            Date lastWeek = new Date(System.currentTimeMillis() - (7 * DAY_IN_MS));
            for(ClubAnnouncement announ : rawClubAnnouns){
                if(announ.getDate().after(lastWeek)){
                    clubAnnouns.add(announ);
                }
            }

            System.out.println("UPDATING CLUB ANNOUNS");
            if(rvAdapter == null){
                rvAdapter = new RViewAdapter_ClubAnnouns(clubAnnouns, null);
                rv.setAdapter(rvAdapter);
                showViews();
            }else if(!rawClubAnnouns.isEmpty()){
                rvAdapter.addItems(clubAnnouns);
                // clubGroup.setVisibility(View.VISIBLE);
                //homeClubLoading.setVisibility(View.VISIBLE);
            }
        }

        //CHECK IF ALL TASKS ARE FINISHED
        boolean allFinished = true;
        for(GetClubAnnounsTask task : clubAnnounTasks){
            if(!task.isFinished()){
                allFinished = false;
            }
        }

        if(allFinished){
            homeClubLoading.setVisibility(View.GONE);
            //SCROLL UP
            onHiddenChanged(false);
        }
    }

    private void showViews(){
        System.out.println("SHOW VIEWS: " + newsLoaded + ", " + rvAdapter);
        if(newsLoaded && ((Main.PROFILE.getClubs() == null || Main.PROFILE.getClubs().isEmpty())
                || rvAdapter != null)){
            System.out.println("ALL LOADED");
            progressBar.setVisibility(View.GONE);
            dateGroupView.setVisibility(View.VISIBLE);
            calendar.setVisibility(View.VISIBLE);
            if(announcements.getAdapter() != null) {
                announcements.setVisibility(View.VISIBLE);
                announError.setVisibility(View.GONE);
            }else{
                announcements.setVisibility(View.GONE);
                announError.setVisibility(View.VISIBLE);
            }

            if(Main.PROFILE.getClubs() != null && (rvAdapter != null && rvAdapter.getItemCount() > 0)){
                clubGroup.setVisibility(View.VISIBLE);
                homeClubLoading.setVisibility(View.VISIBLE);
            }

            //SCROLL UP
            onHiddenChanged(false);

            homeSwipeRefresh.setRefreshing(false);
            homeSwipeRefresh.setEnabled(true);
        }
    }

    private List<NewsItem> getNewsItems(String content) {
        layout.removeView(offline);
        if(content != null){
            String[][] newsItems = this.processNewsSite(content);

            List<NewsItem> news = new ArrayList<NewsItem>();
            for(int i = 0; i < newsItems.length; i++){
                news.add(new NewsItem(newsItems[i][0], newsItems[i][1]));
            }

            return news;
        }else{
            if(AppUtils.isNetworkAvailable(this.getActivity())){
                Crashlytics.log("Couldn't retrieve website!");
            }
            return new ArrayList<NewsItem>();
        }
    }

    private void getTopSong(){
        FirebaseFirestore.getInstance().collection("songs")
                .orderBy("upvotes", Query.Direction.DESCENDING).limit(1).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            List<DocumentSnapshot> query = task.getResult().getDocuments();
                            if(query.size() > 0){
                                DocumentSnapshot doc = query.get(0);
                                SongItem song = new SongItem(doc.getId(), doc.getData());
                                //SHOW SONG IN TEXTVIEW
                            }
                        }
                    }
                });
    }

    private String[][] processNewsSite(String content){
        //THE STRING content IS THE WEBSITE CODE WE DOWNLOADED
        //GET STRING BETWEEN 'ancmnt = "' and '".split(",");' AND SPLIT THE DIFFERENT NEWS ITEMS (SEPARATED BY COMMAS)
        String[] news = content.substring(content.indexOf("ancmnt = \"") + "ancmnt = \"".length(),
                content.indexOf("\".split(\",\");")).split(",");
        //CREATE ARRAY WHERE THE NEWS WILL GO: FIRST ROW IS TITLE AND SECOND IS DESCRIPTION
        String[][] finalNews = new String[news.length][2];

        for(int i = 0; i < news.length; i++){
            //SPLIT THE TITLE AND DESCRIPTION OF EACH NEWS (SEPARATED BY '%24%25-%25%24')
            String[] item = news[i].split("%24%25-%25%24");
            try {
                //DECODE THE TITLE AND PUT IT INTO FIRST ROW OF CORRESPOINDING INDEX IN THE FINAL ARRAY
                finalNews[i][0] = URLDecoder.decode(item[0], "UTF-8");
                if(item.length > 1){
                    //IF DESCRIPTION EXISTS, DECODE THAT AND PUT IT INTO THE SECOND ROW
                    finalNews[i][1] = URLDecoder.decode(item[1], "UTF-8");
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        //RETURNS THE ARRAY CONTAINING EACH OF THE NEWS, WITH THE TITLE AND DESCRIPTION SEPARATED
        return finalNews;
    }

    private void updateDay(final String today){
        FirebaseFirestore.getInstance().collection("info")
                .document("dayNumber").get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        TextView dayView = (TextView) getView().findViewById(R.id.dayNum);
                        if (task.isSuccessful()) {
                            DocumentSnapshot doc = task.getResult();

                            TextView snowDay = getView().findViewById(R.id.snowDay);
                            if (doc.getBoolean("snowDay")) {
                                snowDay.setVisibility(View.VISIBLE);
                            }else{
                                snowDay.setVisibility(View.GONE);
                            }

                            boolean haveFun = doc.getBoolean("haveFun");
                            String dayNum = doc.getString("dayNumber");
                            if (haveFun) {
                                dayView.setText(dayNum);
                            } else {
                                if (!today.contains("Saturday") && !today.contains("Sunday")) {
                                    if (dayNum.trim().equals("1") || dayNum.trim().equals("2")) {
                                        dayView.setText("Day " + dayNum);
                                    } else {
                                        dayView.setVisibility(View.GONE);
                                        return;
                                    }
                                } else {
                                    if (dayNum.trim().equals("1") || dayNum.trim().equals("2")) {
                                        int finalDay = 1;
                                        if (dayNum.trim().equals("1")) {
                                            finalDay = 2;
                                        }
                                        dayView.setText("On Monday it will be a Day " + finalDay);
                                    } else {
                                        dayView.setVisibility(View.GONE);
                                        return;
                                    }
                                }
                            }
                            dayView.setVisibility(View.VISIBLE);
                        } else {
                            dayView.setVisibility(View.GONE);
                        }
                    }
                });
    }

    @Override
    public void setOffline() {
        cancelTasks(true);

        dateGroupView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        announcements.setAdapter(null);
        announcements.setVisibility(View.GONE);
        announError.setVisibility(View.GONE);
        clubGroup.setVisibility(View.GONE);
        calendar.setVisibility(View.GONE);
        clubGroup.setVisibility(View.GONE);
        homeClubLoading.setVisibility(View.GONE);

        homeSwipeRefresh.setRefreshing(false);
        homeSwipeRefresh.setEnabled(true);

        layout.removeView(offline);
        layout.addView(offline);
    }

    public void refreshAnnouns(){
        if(!homeSwipeRefresh.isRefreshing() || requestedRefresh){
            requestedRefresh = false;
            if(AppUtils.isNetworkAvailable(this.getActivity()) && Main.PROFILE != null){
                if(!this.isHidden()){
                    System.out.println("REFRESHING");
                    if(progressBar.getVisibility() == View.GONE){
                        main.refreshProfile();
                    }

                    cancelTasks(true);
                    newsLoaded = false;

                    task = new GetWebsiteTask(this);
                    task.execute(getResources().getString(R.string.newsSiteUrl));

                    rvAdapter = null;
                    clubAnnounTasks = new ArrayList<GetClubAnnounsTask>();

                    if(Main.PROFILE.getClubs() != null){
                        for(String clubId : Main.PROFILE.getClubs()){
                            GetClubAnnounsTask announsTask = new GetClubAnnounsTask(clubId, this.getActivity(), this);
                            announsTask.execute();
                            clubAnnounTasks.add(announsTask);
                        }
                        homeClubLoading.setVisibility(View.VISIBLE);
                    }
                }
            }else{
                if(Main.PROFILE == null){
                    main.refreshProfile();
                }else{
                    setOffline();
                }
            }
        }
    }

    public void cancelTasks(boolean all){
        if(task != null){
            task.cancel(false);
        }

        if(all){
            if(clubAnnounTasks != null && !clubAnnounTasks.isEmpty()) {
                for (GetClubAnnounsTask announTask : clubAnnounTasks) {
                    announTask.cancel(true);
                }
            }
        }
    }

    public void setMain(Main main){
        this.main = main;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if(hidden){
            //cancelTasks(false);
            homeSwipeRefresh.setRefreshing(false);
        }else{
           /* if(homeScrollView != null){
                homeScrollView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        homeScrollView.requestFocus();
                        homeScrollView.fullScroll(View.FOCUS_UP);
                    }
                }, 10L);
            }*/
        }
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onResume(){
        super.onResume();
        if(Main.PROFILE != null && progressBar != null
                && progressBar.getVisibility() == View.VISIBLE){
            refreshAnnouns();
        }
        onHiddenChanged(false);
    }
}