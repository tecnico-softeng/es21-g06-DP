import AnswerDetails from '@/models/management/questions/AnswerDetails';
import ItemCombinationQuestionDetails from './ItemCombinationQuestionDetails';

export default class ItemCombinationAnswerType extends AnswerDetails {
  isCorrect(questionDetails: ItemCombinationQuestionDetails): boolean {
    return true;
  }

  answerRepresentation(
    questionDetails: ItemCombinationQuestionDetails
  ): string {
    return '';
  }
}
