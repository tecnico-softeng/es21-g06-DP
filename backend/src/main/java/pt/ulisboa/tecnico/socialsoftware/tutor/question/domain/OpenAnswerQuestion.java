package pt.ulisboa.tecnico.socialsoftware.tutor.question.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.AnswerDetailsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.CorrectAnswerDetailsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.Updator;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OpenAnswerQuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDetailsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementAnswerDetailsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementQuestionDetailsDto;
import javax.persistence.*;
import java.util.List;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.NO_CORRECT_ANSWER;

@Entity
@DiscriminatorValue(Question.QuestionTypes.OPEN_ANSWER_QUESTION)
public class OpenAnswerQuestion extends QuestionDetails {

    @Column(columnDefinition = "TEXT")
    private String correctAnswer;

    public OpenAnswerQuestion() {
        super();
    }

    public OpenAnswerQuestion(Question question, OpenAnswerQuestionDto questionDto) {
        super(question);
        setCorrectAnswer(questionDto.getCorrectAnswer());
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String answer) {
        if (answer == null || answer.length() == 0){
            throw new TutorException(NO_CORRECT_ANSWER);
        }

        this.correctAnswer = answer;
    }

    public void update(OpenAnswerQuestionDto questionDetails) {
        setCorrectAnswer(questionDetails.getCorrectAnswer());
    }

    @Override
    public void update(Updator updator) {
        updator.update(this);
    }

    @Override
    public String getCorrectAnswerRepresentation() {
        return null;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitQuestionDetails(this);
    }


    @Override
    //To do
    public CorrectAnswerDetailsDto getCorrectAnswerDetailsDto() {
        return null;
    }

    @Override
    public StatementQuestionDetailsDto getStatementQuestionDetailsDto() {
        return null;
    }

    @Override
    public StatementAnswerDetailsDto getEmptyStatementAnswerDetailsDto() {
        return null;
    }

    @Override
    public AnswerDetailsDto getEmptyAnswerDetailsDto() {
        return null;
    }

    @Override
    public QuestionDetailsDto getQuestionDetailsDto() {
        return new OpenAnswerQuestionDto(this);
    }

    @Override
    public String toString() {
        return "OpenAnswerQuestion{" +
                "correctAnswer=" + correctAnswer +
                '}';
    }

    @Override
    public String getAnswerRepresentation(List<Integer> selectedIds) {
        return correctAnswer;
    }
}
