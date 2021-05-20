import StatementAnswerDetails from '@/models/statement/questions/StatementAnswerDetails';
import { QuestionTypes } from '@/services/QuestionHelpers';
import OpenAnswerStatementCorrectAnswerDetails from '@/models/statement/questions/OpenAnswerStatementCorrectAnswerDetails';

export default class OpenAnswerStatementAnswerDetails extends StatementAnswerDetails {
  constructor(jsonObj?: OpenAnswerStatementAnswerDetails) {
    super(QuestionTypes.OpenAnswer);
  }

  isQuestionAnswered(): boolean {
    return true;
  }

  isAnswerCorrect(
    correctAnswerDetails: OpenAnswerStatementCorrectAnswerDetails
  ): boolean {
    return true;
  }
}
