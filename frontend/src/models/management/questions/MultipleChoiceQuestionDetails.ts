import Option from '@/models/management/Option';
import QuestionDetails from '@/models/management/questions/QuestionDetails';
import { QuestionTypes } from '@/services/QuestionHelpers';

export default class MultipleChoiceQuestionDetails extends QuestionDetails {
  choiceType: string = 'SINGLE_SELECTION';
  options: Option[] = [new Option(), new Option(), new Option(), new Option()];

  constructor(jsonObj?: MultipleChoiceQuestionDetails) {
    super(QuestionTypes.MultipleChoice);
    if (jsonObj) {
      this.choiceType = jsonObj.choiceType || this.choiceType;
      this.options = jsonObj.options.map(
        (option: Option) => new Option(option)
      );
    }
  }

  setAsNew(): void {
    this.options.forEach(option => {
      option.id = null;
    });
  }
}
