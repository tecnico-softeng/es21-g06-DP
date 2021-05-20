<template>
  <v-list>
    <v-list-item v-for="(comb, index) in item.combinations" :key="comb">
      <v-list-item-content>
        <v-list-item-title
          >{{ 'Item' + item.sequence + ' - ' + 'Item' + comb }}
        </v-list-item-title>
      </v-list-item-content>
      <v-list-item-action>
        <v-btn @click="item.combinations.splice(index, 1)" icon>
          <v-icon color="red lighten-1">mdi-delete-forever </v-icon>
        </v-btn>
      </v-list-item-action>
    </v-list-item>
  </v-list>
</template>

<script lang="ts">
import { Component, Vue, PropSync } from 'vue-property-decorator';
import Item from '@/models/management/Item';

@Component
export default class ItemCombinationCombinations extends Vue {
  @PropSync('value', { type: Item }) item!: Item;
}
</script>
