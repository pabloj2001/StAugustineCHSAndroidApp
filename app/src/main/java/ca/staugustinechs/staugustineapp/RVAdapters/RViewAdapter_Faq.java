package ca.staugustinechs.staugustineapp.RVAdapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ca.staugustinechs.staugustineapp.Objects.QuestionAnswer;
import ca.staugustinechs.staugustineapp.R;

public class RViewAdapter_Faq extends RecyclerView.Adapter<RViewAdapter_Faq.QuestionViewHolder> {
    private ArrayList<QuestionAnswer> questionAnswers;
    private final LayoutInflater inflater;

    public RViewAdapter_Faq(Context context, ArrayList<QuestionAnswer> qList) {
        inflater = LayoutInflater.from(context);
        questionAnswers = qList;
    }

    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.cardview_faq, parent, false);
        return new QuestionViewHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return questionAnswers.size();
    }

    @Override
    public void onBindViewHolder(@NonNull RViewAdapter_Faq.QuestionViewHolder holder, final int position) {
        if (questionAnswers != null) {
            final int p = position;
            final QuestionAnswer current = questionAnswers.get(p);
            holder.bindTo(current);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean expanded = current.getExpanded();
                    current.setExpanded(!expanded);
                    notifyItemChanged(p);
                }
            });
        }
    }
    class QuestionViewHolder extends RecyclerView.ViewHolder {
        private TextView questionTv, answerTv;
        private ImageView expandCollapse;

        QuestionViewHolder(View itemView) {
            super(itemView);

            questionTv = itemView.findViewById(R.id.question);
            answerTv = itemView.findViewById(R.id.answer);
            expandCollapse = itemView.findViewById(R.id.expand_collapse);
        }

        void bindTo(QuestionAnswer questionAnswer) {
            boolean expanded = questionAnswer.getExpanded();

            String question = questionAnswer.getQuestion();
            String answer = questionAnswer.getAnswer();

            questionTv.setText(question);
            answerTv.setText(answer);

            answerTv.setVisibility(expanded ? View.VISIBLE : View.GONE);

            if (expanded) {
                expandCollapse.setBackgroundResource(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO
                        ? R.drawable.ic_collapse_24dp : R.drawable.ic_collapse_dark_24dp);
            } else {
                expandCollapse.setBackgroundResource(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO
                        ? R.drawable.ic_expand_24dp : R.drawable.ic_expand_dark_24dp);
            }
        }
    }
}
