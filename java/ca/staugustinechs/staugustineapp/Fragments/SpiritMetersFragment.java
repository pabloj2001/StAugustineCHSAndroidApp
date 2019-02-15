package ca.staugustinechs.staugustineapp.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import ca.staugustinechs.staugustineapp.Activities.Login;
import ca.staugustinechs.staugustineapp.Activities.SignUp;
import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.AsyncTasks.GetSpiritPointsTask;
import ca.staugustinechs.staugustineapp.R;

public class SpiritMetersFragment extends Fragment {

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
        }else{
            setOffline();
        }
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
        grade9bar.setProgress(points[0], true);

        grade10text.setText("Grade 10 - " + points[1] + " Points");
        grade10bar.setMax(highest);
        grade10bar.setProgress(points[1], true);

        grade11text.setText("Grade 11 - " + points[2] + " Points");
        grade11bar.setMax(highest);
        grade11bar.setProgress(points[2], true);

        grade12text.setText("Grade 12 - " + points[3] + " Points");
        grade12bar.setMax(highest);
        grade12bar.setProgress(points[3], true);
    }

    public void setOffline(){

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
