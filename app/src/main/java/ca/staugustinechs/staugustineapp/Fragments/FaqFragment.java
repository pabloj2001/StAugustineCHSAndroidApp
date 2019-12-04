package ca.staugustinechs.staugustineapp.Fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Objects;

import ca.staugustinechs.staugustineapp.Objects.QuestionAnswer;
import ca.staugustinechs.staugustineapp.R;
import ca.staugustinechs.staugustineapp.RVAdapters.RViewAdapter_Faq;

public class FaqFragment extends Fragment {

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
        final RecyclerView recycler = Objects.requireNonNull(getView()).findViewById(R.id.rv);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        questionAnswers = new ArrayList<>();

        adapter = new RViewAdapter_Faq(getContext(), questionAnswers);
        recycler.setAdapter(adapter);

        populateFaqList();
    }

    private void populateFaqList() {
        String[] questionList = getResources().getStringArray(R.array.faq_questions);
        String[] answerList = getResources().getStringArray(R.array.faq_answers);
        questionAnswers.clear();

        if (questionList.length != answerList.length) {
            //this should not happen, there should be the same number of questions and answers
            return;
        }

        for (int i = 0; i < questionList.length; i++) {
            questionAnswers.add(new QuestionAnswer(questionList[i], answerList[i]));
        }

        adapter.notifyDataSetChanged();
    }
}
