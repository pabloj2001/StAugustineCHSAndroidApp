package ca.staugustinechs.staugustineapp.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ca.staugustinechs.staugustineapp.Activities.Main;
import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.AsyncTasks.GetProfileIconsTask;
import ca.staugustinechs.staugustineapp.DialogFragments.IconSelectDialog;
import ca.staugustinechs.staugustineapp.Interfaces.IconGetter;
import ca.staugustinechs.staugustineapp.Objects.ProfileIcon;
import ca.staugustinechs.staugustineapp.R;
import ca.staugustinechs.staugustineapp.RVAdapters.RViewAdapter_Icons;

public class IconSelectFragment extends Fragment implements View.OnClickListener, IconGetter{

    private RecyclerView isOwnedIcons, isIcons;
    private LinearLayout isOSGroup, isASGroup;
    private TextView isPoints;
    private ProgressBar loadingCircle;

    private List<ProfileIcon> icons, ownedIcons;
    private GetProfileIconsTask profileIconsTask;
    private ProfileIcon selectedIcon;
    private boolean signup = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_iconselect, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if(savedInstanceState == null){
            View divider1 = view.findViewById(R.id.isDivider1);
            divider1.setBackgroundColor(AppUtils.PRIMARY_COLOR);
            View divider2 = view.findViewById(R.id.isDivider2);
            divider2.setBackgroundColor(AppUtils.PRIMARY_COLOR);

            loadingCircle = getView().findViewById(R.id.isLoadingCircle);
            loadingCircle.getIndeterminateDrawable().setTint(AppUtils.ACCENT_COLOR);

            this.isOSGroup = view.findViewById(R.id.isOSGroup);
            this.isASGroup = view.findViewById(R.id.isASGroup);
            this.isPoints = view.findViewById(R.id.isPoints);

            this.signup = getArguments().getBoolean("SIGNUP");

            isIcons = (RecyclerView) getView().findViewById(R.id.isIcons);
            isIcons.setHasFixedSize(true);
            isIcons.setLayoutManager(new GridLayoutManager(getContext(), 3){
                @Override
                public boolean canScrollVertically() {
                    return false;
                }
            });

            if(!signup){
                isOwnedIcons = (RecyclerView) getView().findViewById(R.id.isOwnedIcons);
                isOwnedIcons.setHasFixedSize(true);
                isOwnedIcons.setLayoutManager(new GridLayoutManager(getContext(), 3){
                    @Override
                    public boolean canScrollVertically() {
                        return false;
                    }
                });
            }else{
                isIcons.setPadding(0, 0, 0, AppUtils.dpToPx(70, this.getActivity()));
                TextView isAvailableText = getView().findViewById(R.id.isAvailableText);
                isAvailableText.setText("Choose a Profile Picture\n(you can change it later)");
                isAvailableText.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
                isAvailableText.setPadding(12, 0, 12, 8);
                isAvailableText.setTextSize(21f);
            }

            if(signup){
                profileIconsTask = new GetProfileIconsTask(this, GetProfileIconsTask.IconGetType.BYRARITY);
                profileIconsTask.execute(0);
            }else{
                profileIconsTask = new GetProfileIconsTask(this, GetProfileIconsTask.IconGetType.ALL);
                profileIconsTask.execute();
            }
        }
    }

    @Override
    public void updateIcons(List<ProfileIcon> icons) {
        this.icons = icons;
        //SELECT CURRENT ICON
        this.setSelectedIcon(Main.PROFILE != null && Main.PROFILE.getIcon() != null
                ? Main.PROFILE.getIcon() : icons.get(0));

        if(!signup){
            //GET OWNED ICONS
            ownedIcons = new ArrayList<ProfileIcon>();
            for(ProfileIcon icon : icons){
                if(icon.isOwned()){
                    ownedIcons.add(icon);
                }
            }
            this.icons.removeAll(ownedIcons);
        }

        //ARRANGE ICONS BY COST
        Collections.sort(this.icons, new Comparator<ProfileIcon>() {
            @Override
            public int compare(ProfileIcon o1, ProfileIcon o2) {
                if(o1.getCost() < o2.getCost()){
                    return -1;
                }else if(o1.getCost() > o2.getCost()){
                    return 1;
                }else{
                    return 0;
                }
            }
        });

        //REMOVE LOADING CIRCLE
        loadingCircle.setVisibility(View.GONE);

        if(!signup){
            //SHOW OWNED ICONS
            RViewAdapter_Icons rvAdapter = new RViewAdapter_Icons(this.ownedIcons, this);
            isOwnedIcons.setAdapter(rvAdapter);
            this.isOSGroup.setVisibility(View.VISIBLE);

            //DISPLAY POINTS
            isPoints.setText("POINTS: " + Main.PROFILE.getPoints());
        }

        //MAKE HEADING GROUP VISIBLE
        View isHeadingGroup = getView().findViewById(R.id.isHeadingGroup);
        isHeadingGroup.setVisibility(View.VISIBLE);

        //SHOW ALL OTHER ICONS
        RViewAdapter_Icons rvAdapter2 = new RViewAdapter_Icons(this.icons, this);
        isIcons.setAdapter(rvAdapter2);
        this.isASGroup.setVisibility(View.VISIBLE);
    }

    public void purchaseIcon(final ProfileIcon icon){
        Snackbar.make(getView(), "Purchasing icon...", Snackbar.LENGTH_LONG).show();

        Main.PROFILE.updatePoints(-icon.getCost(), false, null, null);

        FirebaseFirestore.getInstance().collection("users").document(Main.PROFILE.getUid())
                .update("picsOwned", FieldValue.arrayUnion(icon.getId()))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Main.PROFILE.getPicsOwned().add(icon.getId());
                            Main.PROFILE.setIcon(icon);
                            Main.UPDATE_ICON = true;

                            profileIconsTask = new GetProfileIconsTask(IconSelectFragment.this,
                                    GetProfileIconsTask.IconGetType.ALL);
                            profileIconsTask.execute();
                        }else{
                            Snackbar.make(getView(), "An error has occurred, couldn't purchase profile picture.",
                                    Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void setSelectedIcon(ProfileIcon icon){
        this.selectedIcon = icon;
        //Bitmap img = Bitmap.createScaledBitmap(icon.getImg(), 600, 600, false);
        ((ImageView) getView().findViewById(R.id.isPreview)).setImageBitmap(icon.getImg());
    }

    @Override
    public void onClick(View v) {
        if(ownedIcons != null){
            for(final ProfileIcon icon : ownedIcons){
                if(icon.getId() == (int) v.getTag()){
                    FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getUid())
                            .update("profilePic", icon.getId())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Main.PROFILE.setIcon(icon);
                                        Main.UPDATE_ICON = true;
                                        setSelectedIcon(icon);
                                    }else{
                                        Snackbar.make(getView(), "An error has occurred, couldn't set profile picture.",
                                                Snackbar.LENGTH_LONG).show();
                                    }
                                }
                            });
                    return;
                }
            }
        }

        for(ProfileIcon icon : icons){
            if(icon.getId() == (int) v.getTag()){
                if(!signup){
                    IconSelectDialog dialog = new IconSelectDialog();
                    dialog.setIconSelectFragment(this);
                    dialog.setIcon(icon);
                    dialog.show(this.getFragmentManager(), "iconSelectDialog");
                    return;
                }else{
                    setSelectedIcon(icon);
                }
            }
        }
    }

    public ProfileIcon getSelectedIcon() {
        return selectedIcon;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if(hidden){
            if(profileIconsTask != null){
                profileIconsTask.cancel(true);
            }
        }
    }

    @Override
    public void onDestroy(){
        if(profileIconsTask != null){
            profileIconsTask.cancel(true);
        }
        super.onDestroy();
    }
}
