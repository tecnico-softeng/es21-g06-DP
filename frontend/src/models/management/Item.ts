export default class Item {
  id: number | null = null;
  sequence!: number;
  content: string = '';
  combinations: number[] = [];

  constructor(jsonObj?: Item) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.sequence = jsonObj.sequence;
      this.content = jsonObj.content;
      this.combinations = jsonObj.combinations
        ? jsonObj.combinations.map((comb: number) => comb)
        : this.combinations;
    }
  }
}
