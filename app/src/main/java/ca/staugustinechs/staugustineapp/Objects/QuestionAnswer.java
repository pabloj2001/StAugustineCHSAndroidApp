package ca.staugustinechs.staugustineapp.Objects;

public class QuestionAnswer {
    private String question;
    private String answer;
    private boolean expanded = false;

    public QuestionAnswer(String q, String a) {
        this.question = q;
        this.answer = a;
    }

    public String getQuestion() {
        return this.question;
    }

    public String getAnswer() {
        return this.answer;
    }

    public boolean getExpanded() {
        return this.expanded;
    }

    public void setExpanded(boolean state) {
        this.expanded = state;
    }
}
