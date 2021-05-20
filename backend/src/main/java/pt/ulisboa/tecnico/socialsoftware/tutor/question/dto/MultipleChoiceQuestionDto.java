package pt.ulisboa.tecnico.socialsoftware.tutor.question.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.MultipleChoiceQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.QuestionDetails;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ChoiceType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class MultipleChoiceQuestionDto extends QuestionDetailsDto {
    private ChoiceType choiceType = ChoiceType.SINGLE_SELECTION;
    private List<OptionDto> options = new ArrayList<>();

    public MultipleChoiceQuestionDto() {
    }

    public MultipleChoiceQuestionDto(ChoiceType type) {
        setChoiceType(type);
    }

    public MultipleChoiceQuestionDto(MultipleChoiceQuestion question) {
        setOptions(question.getOptions().stream().map(OptionDto::new).collect(Collectors.toList()));
        setChoiceType(question.getChoiceType());
    }
    
    public ChoiceType getChoiceType(){
        return choiceType;
    }

    public void setChoiceType(ChoiceType type){
        this.choiceType = type;
    }


    public List<OptionDto> getOptions() {
        return options;
    }

    public void setOptions(List<OptionDto> options) {
        this.options = options;
    }

    @Override
    public QuestionDetails getQuestionDetails(Question question) {
        return new MultipleChoiceQuestion(question, this);
    }

    @Override
    public void update(MultipleChoiceQuestion question) {
        question.update(this);
    }

    @Override
    public String toString() {
        return "MultipleChoiceQuestionDto{" +
                "options=" + options +
                '}';
    }

}
