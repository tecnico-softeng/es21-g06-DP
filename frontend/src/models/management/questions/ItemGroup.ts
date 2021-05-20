import Item from '@/models/management/Item';

export default class ItemGroup {
  id: number | null = null;
  sequence!: number | null;
  items: Item[] = [];

  constructor(jsonObj?: ItemGroup) {
    if (jsonObj) {
      this.id = jsonObj.id || this.id;
      this.items = jsonObj.items
        ? jsonObj.items.map((item: Item) => new Item(item))
        : this.items;
    }
  }

  setAsNew(): void {
    this.id = null;
    this.items.forEach(item => {
      item.id = null;
    });
  }
}
