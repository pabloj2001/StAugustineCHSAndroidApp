package ca.staugustinechs.staugustineapp.Fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.AsyncTasks.GetSpiritPointsTask;
import ca.staugustinechs.staugustineapp.R;

public class SpiritMetersFragment extends Fragment implements View.OnClickListener {
    private CardView[] levelCards = new CardView[4];
    private boolean[] expanded = new boolean[4];
    private TextView grade9text, grade10text, grade11text, grade12text;
    private ProgressBar grade9bar, grade10bar, grade11bar, grade12bar;
    private GetSpiritPointsTask task;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_spiritmeters, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if(AppUtils.isNetworkAvailable(this.getActivity())){
            grade9text = view.findViewById(R.id.grade9text);
            grade10text = view.findViewById(R.id.grade10text);
            grade11text = view.findViewById(R.id.grade11text);
            grade12text = view.findViewById(R.id.grade12text);

            grade9bar = view.findViewById(R.id.grade9bar);
            grade9bar.setProgressTintList(AppUtils.ACCENT_COLORSL);
            grade10bar = view.findViewById(R.id.grade10bar);
            grade10bar.setProgressTintList(AppUtils.ACCENT_COLORSL);
            grade11bar = view.findViewById(R.id.grade11bar);
            grade11bar.setProgressTintList(AppUtils.ACCENT_COLORSL);
            grade12bar = view.findViewById(R.id.grade12bar);
            grade12bar.setProgressTintList(AppUtils.ACCENT_COLORSL);

            task = new GetSpiritPointsTask(this);
            task.execute();
        } else {
            setOffline();
        }

        initializeCards(view);
    }

    public void updatePoints(int[] points) {
        //MAKE VIEWS VISIBLE

        //GET GRADE WITH HIGHEST POINTS
        int highest = 0;
        for(int gradePoints : points){
            if(gradePoints > highest){
                highest = gradePoints;
            }
        }

        //SET MAX AND UPDATE PROGRESS
        grade9text.setText("Grade 9 - " + points[0] + " Points");
        grade9bar.setMax(highest);

        grade10text.setText("Grade 10 - " + points[1] + " Points");
        grade10bar.setMax(highest);

        grade11text.setText("Grade 11 - " + points[2] + " Points");
        grade11bar.setMax(highest);

        grade12text.setText("Grade 12 - " + points[3] + " Points");
        grade12bar.setMax(highest);

        if(Build.VERSION.SDK_INT > 23) {
            grade9bar.setProgress(points[0], true);
            grade10bar.setProgress(points[1], true);
            grade11bar.setProgress(points[2], true);
            grade12bar.setProgress(points[3], true);
        }else{
            grade9bar.setProgress(points[0]);
            grade10bar.setProgress(points[1]);
            grade11bar.setProgress(points[2]);
            grade12bar.setProgress(points[3]);
        }
    }

    public void setOffline(){

    }

    private void initializeCards(View view) {
        String[] spiritLevelTitles = getResources().getStringArray(R.array.spirit_level_titles);
        String[] spiritLevelDesc = getResources().getStringArray(R.array.spirit_level_desc);
        levelCards[0] = view.findViewById(R.id.level_1);
        levelCards[1] = view.findViewById(R.id.level_2);
        levelCards[2] = view.findViewById(R.id.level_3);
        levelCards[3] = view.findViewById(R.id.level_4);

        for (int i = 0; i < levelCards.length; i++) {
            expanded[i] = false;
            TextView titleTv = levelCards[i].findViewById(R.id.question);
            TextView bodyTv = levelCards[i].findViewById(R.id.answer);
            ImageView expandCollapse = levelCards[i].findViewById(R.id.expand_collapse);
            titleTv.setText(spiritLevelTitles[i]);
            bodyTv.setText(spiritLevelDesc[i]);
            expandCollapse.setBackgroundResource(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO
                    ? R.drawable.ic_expand_24dp : R.drawable.ic_expand_dark_24dp);
            levelCards[i].setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        int index = 0;
        for (int i = 0; i < levelCards.length; i++) {
            if (view.equals(levelCards[i])) {
                index = i;
            }
        }

        expanded[index] = !expanded[index];
        TextView bodyTv = levelCards[index].findViewById(R.id.answer);
        ImageView expandCollapse = levelCards[index].findViewById(R.id.expand_collapse);
        bodyTv.setVisibility(expanded[index] ? View.VISIBLE : View.GONE);

        if (expanded[index]) {
            expandCollapse.setBackgroundResource(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO
                    ? R.drawable.ic_collapse_24dp : R.drawable.ic_collapse_dark_24dp);
        } else {
            expandCollapse.setBackgroundResource(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO
                    ? R.drawable.ic_expand_24dp : R.drawable.ic_expand_dark_24dp);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if(hidden){
            if(task != null){
                task.cancel(true);
            }
        }
    }

    @Override
    public void onDetach(){
        onHiddenChanged(true);
        super.onDetach();
    }
}
