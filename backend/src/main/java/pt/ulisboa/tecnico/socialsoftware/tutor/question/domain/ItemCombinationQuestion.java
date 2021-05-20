package pt.ulisboa.tecnico.socialsoftware.tutor.question.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.Updator;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemCombinationQuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemGroupDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDetailsDto;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Entity
@DiscriminatorValue(Question.QuestionTypes.ITEM_COMBINATION_QUESTION)
public class ItemCombinationQuestion extends QuestionDetails {

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "questionDetails", fetch = FetchType.LAZY, orphanRemoval = true)
    private final List<ItemGroup> itemGroups = new ArrayList<>();


    public ItemCombinationQuestion() {
        super();
    }

    public ItemCombinationQuestion(Question question, ItemCombinationQuestionDto itemCombinationDto) {
        super(question);
        update(itemCombinationDto);
    }

    public List<ItemGroup> getItemGroups() {
        return itemGroups;
    }

    public void setItemGroups(List<ItemGroupDto> itemGroups) {
        checkExceptions(itemGroups);
        int sequence = 0;
        for (ItemGroupDto itemGroupDto : itemGroups) {
            if (itemGroupDto.getId() == null) {
                int newSequence = itemGroupDto.getSequence() != null ? itemGroupDto.getSequence() : ++sequence;
                ItemGroup itemGroup = new ItemGroup(itemGroupDto);
                itemGroup.setSequence(newSequence);
                itemGroup.setQuestionDetails(this);
                this.itemGroups.add(itemGroup);
                
            } else {
                ItemGroup itemGroup = getItemGroups()
                        .stream()
                        .filter(op -> op.getId().equals(itemGroupDto.getId()))
                        .findFirst()
                        .orElseThrow(() -> new TutorException(ITEM_GROUP_NOT_FOUND, itemGroupDto.getId()));

                itemGroup.setItems(itemGroupDto.getItems());
            }
        }
    }

    public void checkExceptions(List<ItemGroupDto> itemGroups) {
        if (itemGroups.size() != 2) {
            throw new TutorException(TWO_AND_ONLY_TWO_ITEM_GROUPS_NEEDED);
        }

        List<Integer> sequences = new ArrayList<>();
        for (ItemDto item : itemGroups.get(1).getItems()) {
            sequences.add(item.getSequence());
        }

        for(ItemDto itemDto : itemGroups.get(0).getItems()) {
            for(Integer combination : itemDto.getCombinations()) {
                if(!sequences.contains(combination) || combination > sequences.size()) {
                    throw new TutorException(COMB_INDEX_OUT_OF_BOUNDS);
                }
            }
        }
    }

    @Override
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
        return new ItemCombinationQuestionDto(this);
    }

    @Override
    public String getCorrectAnswerRepresentation() {
        return null;
    }

    public void update(ItemCombinationQuestionDto questionDetails) {
        setItemGroups(questionDetails.getItemGroups());
    }

    @Override
    public void update(Updator updator) {
        updator.update(this);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitQuestionDetails(this);
    }

    public void visitItemGroups(Visitor visitor) {
        for (var group : this.getItemGroups()) {
            group.accept(visitor);
        }
    }

    @Override
    public String getAnswerRepresentation(List<Integer> selectedIds) {
        return null;
    }
}
