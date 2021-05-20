package pt.ulisboa.tecnico.socialsoftware.tutor.question.dto;


import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.QuestionDetails;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.OpenAnswerQuestion;

public class OpenAnswerQuestionDto extends QuestionDetailsDto {
    private String correctAnswer = "";

    public OpenAnswerQuestionDto() {
    }
    public OpenAnswerQuestionDto(OpenAnswerQuestion question) {
        this.setCorrectAnswer(question.getCorrectAnswer());
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = "correctAnswer";
    }

    @Override
    public QuestionDetails getQuestionDetails(Question question) {
        return new OpenAnswerQuestion(question, this);
    }

    @Override
    public void update(OpenAnswerQuestion question) {
        question.update(this);
    }

    @Override
    public String toString() {
        return "OpenAnswerQuestion{" +
                "correctAnswer=" + correctAnswer +
                '}';
    }
}
