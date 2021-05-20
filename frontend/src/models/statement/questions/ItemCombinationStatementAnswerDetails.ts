import StatementAnswerDetails from '@/models/statement/questions/StatementAnswerDetails';
import ItemCombinationStatementCorrectAnswerDetails from '@/models/statement/questions/ItemCombinationStatementCorrectAnswerDetails';

export default class ItemCombinationStatementAnswerDetails extends StatementAnswerDetails {
  isQuestionAnswered(): boolean {
    return true;
  }

  isAnswerCorrect(
    correctAnswerDetails: ItemCombinationStatementCorrectAnswerDetails
  ): boolean {
    return true;
  }
}
