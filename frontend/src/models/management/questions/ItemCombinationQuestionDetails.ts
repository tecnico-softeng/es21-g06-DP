import QuestionDetails from '@/models/management/questions/QuestionDetails';
import ItemGroup from '@/models/management/questions/ItemGroup';
import { QuestionTypes } from '@/services/QuestionHelpers';

export default class ItemCombinationQuestionDetails extends QuestionDetails {
  itemGroups: ItemGroup[] = [new ItemGroup(), new ItemGroup()];

  constructor(jsonObj?: ItemCombinationQuestionDetails) {
    super(QuestionTypes.ItemCombination);

    if (jsonObj) {
      this.itemGroups = jsonObj.itemGroups.map(
        (item: ItemGroup) => new ItemGroup(item)
      );
    }
  }

  setAsNew(): void {
    this.itemGroups.forEach(group => {
      group.setAsNew();
    });
  }
}
