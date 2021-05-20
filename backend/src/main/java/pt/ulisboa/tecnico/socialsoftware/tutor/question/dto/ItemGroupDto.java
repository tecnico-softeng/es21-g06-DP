package pt.ulisboa.tecnico.socialsoftware.tutor.question.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemGroup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemGroupDto implements Serializable {
    private Integer id;
    private Integer sequence;
    private List<ItemDto> items = new ArrayList<>();

    public ItemGroupDto() {
    }

    public ItemGroupDto(ItemGroup itemGroup) {
        this.id = itemGroup.getId();
        this.sequence = itemGroup.getSequence();
        this.items = itemGroup.getItems().stream().map(ItemDto::new).collect(Collectors.toList());
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

    public List<ItemDto> getItems() {
        return items;
    }

    public void setItems(List<ItemDto> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "ItemGroupDto{" +
                "id=" + id +
                ", sequence=" + sequence +
                ", items=" + items +
                '}';
    }
}
