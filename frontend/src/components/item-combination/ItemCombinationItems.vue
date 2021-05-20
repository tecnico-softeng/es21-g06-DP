<template>
  <v-card border-variant="light" outlined>
    <v-list>
      <v-list-item>
        <v-list-item-content>
          <v-list-item-title>
            <v-text-field
              v-model="currentText"
              label="Add item"
              v-on:keyup.enter="addNewElement"
              data-cy="itemGroupAddItemNameArea"
            />
          </v-list-item-title>
        </v-list-item-content>
        <v-list-item-action>
          <v-btn @click="addNewElement" class="ma-2" icon>
            <v-icon :data-cy="`AddItemButton`" color="grey lighten-1"
              >mdi-plus
            </v-icon>
          </v-btn>
        </v-list-item-action>
      </v-list-item>
      <v-list-item v-for="(item, index) in group.items" :key="item.content">
        <v-list-item-content>
          <v-list-item-title
            >{{ 'Item ' + (index + 1) + ': ' + item.content }}
          </v-list-item-title>
        </v-list-item-content>
        <v-list-item-action>
          <v-btn @click="deleteItem(index)" icon>
            <v-icon color="red lighten-1">mdi-delete-forever </v-icon>
          </v-btn>
        </v-list-item-action>
      </v-list-item>
    </v-list>
  </v-card>
</template>

<script lang="ts">
import { Component, Vue, PropSync } from 'vue-property-decorator';
import ItemGroup from '@/models/management/questions/ItemGroup';
import Item from '@/models/management/Item';

@Component
export default class ItemCombinationItems extends Vue {
  @PropSync('value', { type: ItemGroup }) group!: ItemGroup;
  currentText: string = '';
  counter: number = 1;

  created() {
    this.counter = this.getMaxSequence();
  }

  getMaxSequence() {
    return this.group.items.length + 1;
  }

  addNewElement() {
    if (this.currentText) {
      const item = new Item();
      item.content = this.currentText;
      item.sequence = this.counter;
      this.group.items.push(item);
      this.currentText = '';
      this.counter++;
    }
  }

  deleteItem(index: number) {
    this.group.items.splice(index, 1);
    for (let i = index; i < this.group.items.length; i++) {
      this.group.items[i].sequence = this.group.items[i].sequence - 1;
    }
    this.counter--;
    this.$root.$emit('deleteComb', this.group, index);
  }
}
</script>
