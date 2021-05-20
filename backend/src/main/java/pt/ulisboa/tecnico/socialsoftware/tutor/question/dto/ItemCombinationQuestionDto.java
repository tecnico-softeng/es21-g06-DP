package pt.ulisboa.tecnico.socialsoftware.tutor.question.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemCombinationQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.QuestionDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemCombinationQuestionDto extends QuestionDetailsDto {

    private List<ItemGroupDto> itemGroups = new ArrayList<>();

    public ItemCombinationQuestionDto() {
    }

    public ItemCombinationQuestionDto(ItemCombinationQuestion question) {
        this.itemGroups = question.getItemGroups()
                .stream().map(ItemGroupDto::new)
                .collect(Collectors.toList());
    }

    public List<ItemGroupDto> getItemGroups() {
        return itemGroups;
    }

    public void setItemGroups(List<ItemGroupDto> itemGroups) {
        this.itemGroups = itemGroups;
    }

    @Override
    public String toString() {
        return "ItemCombinationQuestionDto{" +
                ", itemGroups=" + itemGroups +
                '}';
    }

    @Override
    public QuestionDetails getQuestionDetails(Question question) {
        return new ItemCombinationQuestion(question, this);
    }

    @Override
    public void update(ItemCombinationQuestion question) {
        question.update(this);
    }
}
