<template>
  <div class="open-answer-correct-answer">
    <v-row>
      <v-col cols="12" offset="0">
        <v-textarea
          v-model="sQuestionDetails.correctAnswer"
          :label="`Correct Answer`"
          :data-cy="`Correct-answer`"
          rows="4"
          auto-grow
        ></v-textarea>
      </v-col>
    </v-row>
  </div>
</template>

<script lang="ts">
import { Component, PropSync, Vue } from 'vue-property-decorator';
import OpenAnswerQuestionDetails from '@/models/management/questions/OpenAnswerQuestionDetails';

@Component
export default class OpenAnswerCreate extends Vue {
  @PropSync('questionDetails', { type: OpenAnswerQuestionDetails })
  sQuestionDetails!: OpenAnswerQuestionDetails;
}
</script>
