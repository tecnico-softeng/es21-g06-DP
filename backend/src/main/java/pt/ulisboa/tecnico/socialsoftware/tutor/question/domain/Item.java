package pt.ulisboa.tecnico.socialsoftware.tutor.question.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.DomainEntity;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemDto;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

@Entity
@Table(name = "items")
public class Item implements DomainEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer sequence;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="combination", joinColumns=@JoinColumn(name="OWNER_ID"))
    private Set<Integer> combinations = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_combination_id")
    private ItemGroup itemGroup;

    //@ManyToMany()
    //private final Set<ItemCombinationAnswer> questionAnswers = new HashSet<>();  to be implemented in a later sprint

    public Item() {
    }

    public Item(ItemDto item) {
        setSequence(item.getSequence());
        setContent(item.getContent());
        setCombinations(item.getCombinations());
    }

    public Integer getId() {
        return id;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }
 
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ItemGroup getItemGroup() {
        return itemGroup;
    }

    public void setItemGroup(ItemGroup itemGroup) {
        this.itemGroup = itemGroup;
    }

    public void setCombinations(Set<Integer> combinations) {
        this.combinations = combinations;
    }

    public Set<Integer> getCombinations() {
        return combinations;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitItem(this);
    }

}