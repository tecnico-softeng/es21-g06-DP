<template>
  <div class="multiple-choice-options">
    <v-row>
      <v-select
        v-model="sQuestionDetails.choiceType"
        label="Choice Type"
        :items="choiceTypesOptions"
        data-cy="choiceTypeInput"
      />
    </v-row>
    <v-row>
      <v-col cols="1" offset="9">
        Correct
      </v-col>
    </v-row>
    <v-row
      v-for="(option, index) in sQuestionDetails.options"
      :key="index"
      data-cy="questionOptionsInput"
    >
      <v-col cols="9">
        <v-textarea
          v-model="option.content"
          :label="`Option ${index + 1}`"
          :data-cy="`Option${index + 1}`"
          rows="1"
          auto-grow
        ></v-textarea>
      </v-col>
      <v-col cols="1">
        <v-switch
          v-model="option.correct"
          inset
          :data-cy="`Switch${index + 1}`"
        />
      </v-col>
      <v-col v-if="sQuestionDetails.choiceType === 'SORTING' && option.correct">
        <v-select
          v-model="option.order"
          label="Choose Order"
          :items="availableOrders"
          :data-cy="`Order${index + 1}`"
        />
      </v-col>

      <v-col v-if="sQuestionDetails.options.length > 2">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon
              :data-cy="`Delete${index + 1}`"
              small
              class="ma-1 action-button"
              v-on="on"
              @click="removeOption(index)"
              color="red"
              >close</v-icon
            >
          </template>
          <span>Remove Option</span>
        </v-tooltip>
      </v-col>
    </v-row>

    <v-row>
      <v-btn
        class="ma-auto"
        color="blue darken-1"
        @click="addOption"
        data-cy="addOptionMultipleChoice"
        >Add Option</v-btn
      >
    </v-row>
  </div>
</template>

<script lang="ts">
import { Component, PropSync, Vue} from 'vue-property-decorator';
import MultipleChoiceQuestionDetails from '@/models/management/questions/MultipleChoiceQuestionDetails';
import Option from '@/models/management/Option';
import {
  ChoiceTypes
} from '@/services/QuestionHelpers';

@Component
export default class MultipleChoiceCreate extends Vue {
  @PropSync('questionDetails', { type: MultipleChoiceQuestionDetails })
  sQuestionDetails!: MultipleChoiceQuestionDetails;
  addOption() {
    this.sQuestionDetails.options.push(new Option());
  }

  removeOption(index: number) {
    this.sQuestionDetails.options.splice(index, 1);
  }

  get choiceTypesOptions() {
    return Object.values(ChoiceTypes).map(qt => ({
      text: qt.replace(/_/g, ' '),
      value: qt
    }));
  }

  get availableOrders() {
    let orders = [];
    for(let i = 1; i < this.sQuestionDetails.options.length + 1; i++){
      orders.push(i);
    }
    return Object.values(orders);

  }
}
</script>
