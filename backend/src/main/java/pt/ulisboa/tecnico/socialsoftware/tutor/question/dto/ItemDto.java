package pt.ulisboa.tecnico.socialsoftware.tutor.question.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Item;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class ItemDto implements Serializable{
    private Integer id;
    private Integer sequence;
    private String content;
    private Set<Integer> combinations = new HashSet<>();
    
    public ItemDto() {
    }

    public ItemDto(Item item) {
        this.id = item.getId();
        this.sequence = item.getSequence();
        this.content = item.getContent();
        this.combinations = item.getCombinations();
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
        return "";
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCombinations(Set<Integer> combinations) {
        this.combinations = combinations;
    }

    public Set<Integer> getCombinations() {
        return combinations;
    }


    @Override
    public String toString() {
        return "ItemDto{" +
                "id=" + id +
                ", content='" + content + '\'' +
                '}';
    }



}
