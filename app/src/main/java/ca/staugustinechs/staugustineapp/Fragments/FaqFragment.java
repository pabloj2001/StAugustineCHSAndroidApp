package ca.staugustinechs.staugustineapp.Fragments;


import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.Objects.QuestionAnswer;
import ca.staugustinechs.staugustineapp.R;
import ca.staugustinechs.staugustineapp.RVAdapters.RViewAdapter_Faq;

public class FaqFragment extends Fragment {

    private GetFaqTask faqTask = null;
    private ProgressBar loadingCircle;
    private View offlineLayout, faqScroll;
    private LinearLayout faqLayout;
    private ArrayList<QuestionAnswer> questionAnswers;
    private RViewAdapter_Faq adapter;

    public FaqFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_faq, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        offlineLayout = getLayoutInflater().inflate(R.layout.offline_layout, null);
        faqScroll = getView().findViewById(R.id.faq_scroll);
        faqLayout = getView().findViewById(R.id.faq_layout);
        loadingCircle = getView().findViewById(R.id.faqLoadingCircle);
        loadingCircle.getIndeterminateDrawable().setTint(AppUtils.ACCENT_COLOR);
        loadingCircle.setVisibility(View.GONE);

        if (AppUtils.isNetworkAvailable(this.getActivity())) {
            RecyclerView recycler = Objects.requireNonNull(getView()).findViewById(R.id.rv);
            recycler.setLayoutManager(new LinearLayoutManager(getContext()));

            questionAnswers = new ArrayList<>();
            adapter = new RViewAdapter_Faq(getContext(), questionAnswers);
            recycler.setAdapter(adapter);
            faqTask = new GetFaqTask();
            faqTask.execute();
        } else {
            setOffline();
        }
    }

    private void setOffline() {
        loadingCircle.setVisibility(View.GONE);
        faqScroll.setVisibility(View.GONE);

        faqLayout.removeView(offlineLayout);
        faqLayout.addView(offlineLayout);
    }

    private void cancelTask() {
        if (faqTask != null) {
            faqTask.cancel(true);
        }
    }

    @Override
    public void onDestroy() {
        cancelTask();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        cancelTask();
        super.onPause();
    }

    @Override
    public void onDetach() {
        cancelTask();
        super.onDetach();
    }


    private class GetFaqTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingCircle.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Task<DocumentSnapshot> faqTask = FirebaseFirestore.getInstance().collection("info")
                    .document("faq").get();

            while (!faqTask.isComplete()) {
            }

            if (faqTask.isSuccessful()) {
                for (Map.Entry<String, Object> entry :
                        (Objects.requireNonNull(Objects.requireNonNull(faqTask.getResult()).getData())).entrySet()) {
                    questionAnswers.add(new QuestionAnswer(entry.getKey(), (String) entry.getValue()));
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            adapter.notifyDataSetChanged();
            loadingCircle.setVisibility(View.GONE);
        }
    }
}
