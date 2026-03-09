import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ItemService } from '../../services/item.service';
import { Item, ItemType, WearLocation } from '../../models/item.model';
import { ItemDialogComponent } from '../item-dialog/item-dialog.component';

@Component({
  selector: 'app-item-creator',
  standalone: true,
  imports: [CommonModule, FormsModule, ItemDialogComponent],
  templateUrl: './item-creator.component.html',
  styleUrl: './item-creator.component.css'
})
export class ItemCreatorComponent implements OnInit {
  items: Item[] = [];
  totalItems: number = 0;
  page: number = 0;
  size: number = 10;

  showDialog = false;
  selectedItem: Item | undefined = undefined;

  filters = {
    name: '',
    type: '' as ItemType | '',
    location: '' as WearLocation | '',
    minValue: undefined as number | undefined,
    maxValue: undefined as number | undefined
  };

  itemTypes = Object.values(ItemType);
  wearLocations = Object.values(WearLocation);

  constructor(
    private itemService: ItemService
  ) {}

  ngOnInit(): void {
    this.loadItems();
  }

  get Math() {
    return Math;
  }

  loadItems() {
    const activeFilters: any = {};
    if (this.filters.name) activeFilters.name = this.filters.name;
    if (this.filters.type) activeFilters.type = this.filters.type;
    if (this.filters.location) activeFilters.location = this.filters.location;
    if (this.filters.minValue !== undefined) activeFilters.minValue = this.filters.minValue;
    if (this.filters.maxValue !== undefined) activeFilters.maxValue = this.filters.maxValue;

    this.itemService.getItems(this.page, this.size, activeFilters).subscribe(response => {
      this.items = response.items;
      this.totalItems = response.total;
    });
  }

  onSearch() {
    this.page = 0;
    this.loadItems();
  }

  prevPage() {
    if (this.page > 0) {
      this.page--;
      this.loadItems();
    }
  }

  nextPage() {
    if ((this.page + 1) * this.size < this.totalItems) {
      this.page++;
      this.loadItems();
    }
  }

  openCreateDialog() {
    this.selectedItem = undefined;
    this.showDialog = true;
  }

  editItem(item: Item) {
    this.selectedItem = item;
    this.showDialog = true;
  }

  onSaveItem(item: Item) {
    if (item.id) {
      this.itemService.updateItem(item.id, item).subscribe(() => {
        this.showDialog = false;
        this.loadItems();
      });
    } else {
      this.itemService.createItem(item).subscribe(() => {
        this.showDialog = false;
        this.loadItems();
      });
    }
  }

  onCancelDialog() {
    this.showDialog = false;
  }

  deleteItem(item: Item, event: Event) {
    event.stopPropagation();
    if (confirm(`Are you sure you want to delete ${item.name}?`)) {
      this.itemService.deleteItem(item.id!).subscribe(() => {
        this.loadItems();
      });
    }
  }
}
