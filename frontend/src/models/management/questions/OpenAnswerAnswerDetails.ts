import AnswerDetails from '@/models/management/questions/AnswerDetails';
import { QuestionTypes } from '@/services/QuestionHelpers';
import OpenAnswerQuestionDetails from './OpenAnswerQuestionDetails';

export default class OpenAnswerAnswerDetails extends AnswerDetails {
  constructor(jsonObj?: OpenAnswerAnswerDetails) {
    super(QuestionTypes.OpenAnswer);
  }

  isCorrect(questionDetails: OpenAnswerQuestionDetails): boolean {
    return true;
  }

  answerRepresentation(questionDetails: OpenAnswerQuestionDetails): string {
    return '';
  }
}
