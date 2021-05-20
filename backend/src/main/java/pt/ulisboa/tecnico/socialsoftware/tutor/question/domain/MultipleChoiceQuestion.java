package pt.ulisboa.tecnico.socialsoftware.tutor.question.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.Updator;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.MultipleChoiceQuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDetailsDto;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.AT_LEAST_TWO_ORDERED_OPTIONS_NEEDED;
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.NO_CORRECT_OPTION;
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.ONE_CORRECT_OPTION_NEEDED;
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.OPTION_NOT_FOUND;
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.SORTING_CONFLICT;

@Entity
@DiscriminatorValue(Question.QuestionTypes.MULTIPLE_CHOICE_QUESTION)
public class MultipleChoiceQuestion extends QuestionDetails {
    private ChoiceType choiceType;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "questionDetails", fetch = FetchType.EAGER, orphanRemoval = true)
    private final List<Option> options = new ArrayList<>();


    public MultipleChoiceQuestion() {
        super();
        setChoiceType(ChoiceType.SINGLE_SELECTION);
    }

    public MultipleChoiceQuestion(Question question, MultipleChoiceQuestionDto questionDto) {
        super(question);
        setChoiceType(questionDto.getChoiceType());
        setOptions(questionDto.getOptions());
    }


    public ChoiceType getChoiceType() {
        return choiceType;
    }

    public void setChoiceType(ChoiceType type) {
        this.choiceType = type;
    }

    public List<Option> getOptions() {
        return options;
    }


    public void setOptions(List<OptionDto> optionDtos) {
        long correctOptions = optionDtos.stream().filter(OptionDto::isCorrect).count();
        if (choiceType == ChoiceType.SINGLE_SELECTION && correctOptions > 1) {
            throw new TutorException(ONE_CORRECT_OPTION_NEEDED);
        } else if (correctOptions < 1) {
            throw new TutorException(NO_CORRECT_OPTION);
        } else if (choiceType == ChoiceType.SORTING && optionDtos.stream().filter(x -> x.getOrder() != null).count() < 2 ){
            throw new TutorException(AT_LEAST_TWO_ORDERED_OPTIONS_NEEDED);
        } 
        if (this.choiceType == ChoiceType.SORTING){
            List<Integer> orders = new ArrayList<>();
            for (OptionDto option: optionDtos){
                if (option.isCorrect() && orders.contains(option.getOrder())){
                    throw new TutorException(SORTING_CONFLICT);
                } else if (option.isCorrect()){
                    orders.add(option.getOrder());
                }
            }
        }

        for (Option option: this.options) {
            option.remove();
        }
        this.options.clear();

        int index = 0;
        for (OptionDto optionDto : optionDtos) {
            optionDto.setSequence(index++);
            new Option(optionDto).setQuestionDetails(this);
        }
    }

    public void addOption(Option option) {
        options.add(option);
    }

    public Integer getCorrectOptionId() {
        return this.getOptions().stream()
                .filter(Option::isCorrect)
                .findAny()
                .map(Option::getId)
                .orElse(null);
    }

    public void update(MultipleChoiceQuestionDto questionDetails) {
        setChoiceType(questionDetails.getChoiceType());
        setOptions(questionDetails.getOptions());
    }

    @Override
    public void update(Updator updator) {
        updator.update(this);
    }

    @Override
    public String getCorrectAnswerRepresentation() {
        return this.getCorrectAnswer();
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitQuestionDetails(this);
    }

    public void visitOptions(Visitor visitor) {
        for (Option option : this.getOptions()) {
            option.accept(visitor);
        }
    }

    @Override
    public CorrectAnswerDetailsDto getCorrectAnswerDetailsDto() {
        return new MultipleChoiceCorrectAnswerDto(this);
    }

    @Override
    public StatementQuestionDetailsDto getStatementQuestionDetailsDto() {
        return new MultipleChoiceStatementQuestionDetailsDto(this);
    }

    @Override
    public StatementAnswerDetailsDto getEmptyStatementAnswerDetailsDto() {
        return new MultipleChoiceStatementAnswerDetailsDto();
    }

    @Override
    public AnswerDetailsDto getEmptyAnswerDetailsDto() {
        return new MultipleChoiceAnswerDto();
    }

    @Override
    public QuestionDetailsDto getQuestionDetailsDto() {
        return new MultipleChoiceQuestionDto(this);
    }

    public String getCorrectAnswer() {
        if (this.getChoiceType() == ChoiceType.SINGLE_SELECTION) {
            return convertSequenceToLetter(this.getOptions()
                    .stream()
                    .filter(Option::isCorrect)
                    .findAny().orElseThrow(() -> new TutorException(NO_CORRECT_OPTION))
                    .getSequence());
        } else if (this.getChoiceType() == ChoiceType.MULTIPLE_SELECTION) {
            return this.options.stream()
                    .filter(Option::isCorrect)
                    .map(x -> convertSequenceToLetter(x.getSequence()))
                    .collect(Collectors.joining(" | "));
        } else {
            return this.options.stream()
                    .filter(x-> x.isCorrect() && x.getOrder() != null)
                    .sorted(Comparator.comparing(Option::getOrder))
                    .map(x -> convertSequenceToLetter(x.getSequence()))
                    .collect(Collectors.joining(" -> "));
        }
    }

    @Override
    public void delete() {
        super.delete();
        for (Option option : this.options) {
            option.remove();
        }
        this.options.clear();
    }

    @Override
    public String toString() {
        return "MultipleChoiceQuestion{" +
                "options=" + options +
                '}';
    }

    public static String convertSequenceToLetter(Integer correctAnswer) {
        return correctAnswer != null ? Character.toString('A' + correctAnswer) : "-";
    }

    @Override
    public String getAnswerRepresentation(List<Integer> selectedIds) {
        var result = this.options
                .stream()
                .filter(x -> selectedIds.contains(x.getId()))
                .map(x -> convertSequenceToLetter(x.getSequence()))
                .collect(Collectors.joining("|"));
        return !result.isEmpty() ? result : "-";
    }
}