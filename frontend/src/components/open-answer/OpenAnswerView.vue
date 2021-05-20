<template>
  <div>
    <span
      data-cy="correctAnswer"
      v-html="
        convertMarkDown(
          '<b>Correct Answer: </b>' + questionDetails.correctAnswer
        )
      "
    />
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop } from 'vue-property-decorator';
import { convertMarkDown } from '@/services/ConvertMarkdownService';
import Image from '@/models/management/Image';
import OpenAnswerAnswerDetails from '@/models/management/questions/OpenAnswerAnswerDetails';
import OpenAnswerQuestionDetails from '@/models/management/questions/OpenAnswerQuestionDetails';

@Component
export default class OpenAnswerView extends Vue {
  @Prop() readonly questionDetails!: OpenAnswerQuestionDetails;
  @Prop() readonly answerDetails?: OpenAnswerAnswerDetails;

  studentAnswered(answer: string) {
    return null;
  }

  convertMarkDown(text: string, image: Image | null = null): string {
    return convertMarkDown(text, image);
  }
}
</script>
