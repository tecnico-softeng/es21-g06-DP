import StatementCorrectAnswerDetails from '@/models/statement/questions/StatementCorrectAnswerDetails';
import { QuestionTypes } from '@/services/QuestionHelpers';

export default class OpenAnswerStatementCorrectAnswerDetails extends StatementCorrectAnswerDetails {
  constructor(jsonObj?: OpenAnswerStatementCorrectAnswerDetails) {
    super(QuestionTypes.OpenAnswer);
  }
}
