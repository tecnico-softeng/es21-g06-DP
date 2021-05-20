package pt.ulisboa.tecnico.socialsoftware.tutor.question.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.DomainEntity;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemGroupDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemDto;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.ITEM_GROUPS_CANT_BE_EMPTY;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;



@Entity
@Table(name = "item_group",
        uniqueConstraints = @UniqueConstraint(columnNames = {"question_details_id", "sequence"}))
public class ItemGroup implements DomainEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer sequence;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "question_details_id")
    private ItemCombinationQuestion questionDetails;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "itemGroup", fetch = FetchType.EAGER, orphanRemoval = true)
    private final List<Item> items = new ArrayList<>();

    public ItemGroup() {
    }

    public ItemGroup(ItemGroupDto itemGroupDto) {
        setItems(itemGroupDto.getItems());
        setSequence(itemGroupDto.getSequence());
    }

    public Integer getId() {
        return id;
    }

    public ItemCombinationQuestion getQuestionDetails() {
        return questionDetails;
    }

    public void setQuestionDetails(ItemCombinationQuestion question) {
        this.questionDetails = question;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<ItemDto> items) {
        if(items.isEmpty()) {
            throw new TutorException(ITEM_GROUPS_CANT_BE_EMPTY);
        }

        this.items.clear();

        List<Integer> sequences = new ArrayList<>();
        for (ItemDto item : items) {
            sequences.add(item.getSequence());
        }
        int index = 0;
        for (ItemDto itemDto : items) {
            int itemSequence;
            if(itemDto.getSequence() != null) itemSequence = itemDto.getSequence();
            else {
                for (; sequences.contains(index); index++); /* Lowest free sequence value */ 
                itemSequence = index;
                sequences.add(itemSequence);
            }
            itemDto.setSequence(itemSequence);
            Item item = new Item(itemDto);
            item.setItemGroup(this);
            this.items.add(item);
        }
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitItemGroup(this);
    }

    public void visitItems(Visitor visitor) {
        for (Item item : this.getItems()) {
            item.accept(visitor);
        }
    }
}