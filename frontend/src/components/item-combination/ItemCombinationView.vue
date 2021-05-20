<template>
  <ul>
    <v-card-title>
      <span>Item Group #1</span>
    </v-card-title>
    <li v-for="item in questionDetails.itemGroups[0].items" :key="item.id">
      <span
        v-html="convertMarkDown('Item ' + item.sequence + ': ' + item.content)"
      />
      <span>Combinations</span>
      <ul>
        <li v-for="i in questionDetails.itemGroups[1].items.length" :key="i">
          <span
            v-if="item.combinations.includes(i)"
            v-html="convertMarkDown('✔ Item ' + i)"
          />
          <span v-else v-html="convertMarkDown('✖ Item ' + i)" />
        </li>
      </ul>
    </li>

    <v-card-title>
      <span>Item Group #2</span>
    </v-card-title>
    <li v-for="item in questionDetails.itemGroups[1].items" :key="item.id">
      <span
        v-html="convertMarkDown('Item ' + item.sequence + ': ' + item.content)"
      />
    </li>
  </ul>
</template>

<script lang="ts">
import { Component, Vue, Prop } from 'vue-property-decorator';
import { convertMarkDown } from '@/services/ConvertMarkdownService';
import Image from '@/models/management/Image';
import ItemCombinationAnswerDetails from '@/models/management/questions/ItemCombinationAnswerType';
import ItemCombinationQuestionDetails from '@/models/management/questions/ItemCombinationQuestionDetails';
@Component
export default class ItemCombinationView extends Vue {
  @Prop() readonly questionDetails!: ItemCombinationQuestionDetails;
  @Prop() readonly answerDetails?: ItemCombinationAnswerDetails;

  convertMarkDown(text: string, image: Image | null = null): string {
    return convertMarkDown(text, image);
  }
}
</script>
