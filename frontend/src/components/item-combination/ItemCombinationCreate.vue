<template>
  <div class="item-combination-groups">
    <v-row>
      <v-col cols="0" offset="0">
        Item Groups
      </v-col>
    </v-row>
    <v-card-title>
      <span>Item Group #1</span>
    </v-card-title>
    <Items v-model="sQuestionDetails.itemGroups[0]" />
    <v-card-title>
      <span>Item Group #2</span>
    </v-card-title>
    <Items v-model="sQuestionDetails.itemGroups[1]" />
    <v-row>
      <v-col cols="0" offset="0">
        Correct combinations
      </v-col>
    </v-row>
    <v-card border-variant="light" outlined>
      <v-list-item>
        <v-list-item-content>
          <v-list-item-title>
            <v-row>
              <v-col>
                <v-select
                  :items="
                    sQuestionDetails.itemGroups[0].items.map(x => [
                      sQuestionDetails.itemGroups[0].items.indexOf(x) + 1,
                      x.content
                    ])
                  "
                  v-model="item1"
                  label="Select group 1 item"
                  :data-cy="`SelectCombinationOne`"
                ></v-select>
              </v-col>
              <v-col>
                <v-select
                  :items="
                    sQuestionDetails.itemGroups[1].items.map(x => [
                      sQuestionDetails.itemGroups[1].items.indexOf(x) + 1,
                      x.content
                    ])
                  "
                  v-model="item2"
                  label="Select group 2 item"
                  :data-cy="`SelectCombinationTwo`"
                ></v-select>
              </v-col>
            </v-row>
          </v-list-item-title>
        </v-list-item-content>
        <v-list-item-action>
          <v-btn @click="addNewCombination()" class="ma-2" icon>
            <v-icon :data-cy="`AddCombination`" color="grey lighten-1"
              >mdi-plus</v-icon
            >
          </v-btn>
        </v-list-item-action>
      </v-list-item>
      <v-card-title>
        <span>Combinations</span>
      </v-card-title>
      <Combinations
        v-for="(item, index) in sQuestionDetails.itemGroups[0].items"
        :key="index"
        :option="item"
        v-model="sQuestionDetails.itemGroups[0].items[index]"
      />
    </v-card>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop, PropSync } from 'vue-property-decorator';
import ItemCombinationQuestionDetails from '@/models/management/questions/ItemCombinationQuestionDetails';
import Items from '@/components/item-combination/ItemCombinationItems.vue';
import Combinations from '@/components/item-combination/ItemCombinationCombinations.vue';
import ItemGroup from '@/models/management/questions/ItemGroup';

@Component({
  components: {
    Items,
    Combinations
  }
})
export default class ItemCombinationCreate extends Vue {
  @PropSync('questionDetails', { type: ItemCombinationQuestionDetails })
  sQuestionDetails!: ItemCombinationQuestionDetails;
  @Prop({ default: true }) readonly readonlyEdit!: boolean;
  item1: string[] | null = null;
  item2: string[] | null = null;

  addNewCombination() {
    if (
      this.item1 != null &&
      this.item2 != null &&
      this.sQuestionDetails.itemGroups[0].items[
        Number(this.item1[0]) - 1
      ].combinations.indexOf(Number(this.item2[0])) == -1
    ) {
      this.sQuestionDetails.itemGroups[0].items[
        Number(this.item1[0]) - 1
      ].combinations.push(Number(this.item2[0]));
      this.sQuestionDetails.itemGroups[0].items[
        Number(this.item1[0]) - 1
      ].combinations.sort(function(a, b) {
        return a - b;
      });
    }
    this.item1 = null;
    this.item2 = null;
  }
  mounted() {
    this.$root.$on('deleteComb', (group: ItemGroup, index: number) => {
      this.deleteCombination(group, index);
    });
  }
  deleteCombination(group: ItemGroup, index: number) {
    if (this.sQuestionDetails.itemGroups[1] == group) {
      for (let item of this.sQuestionDetails.itemGroups[0].items) {
        if (item.combinations.includes(index + 1)) {
          item.combinations.splice(item.combinations.indexOf(index + 1), 1);
          for (let j = index; j < item.combinations.length; j++) {
            item.combinations[j]--;
          }
        }
      }
    }
  }
}
</script>
