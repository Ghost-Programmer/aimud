import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {StoreService, Store} from '../../services/store.service';
import {ItemService} from '../../services/item.service';
import {SearchableDropdownComponent} from '../searchable-dropdown/searchable-dropdown.component';

@Component({
  selector: 'app-store-management',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, SearchableDropdownComponent],
  templateUrl: './store-management.component.html',
  styleUrls: ['./store-management.component.css']
})
export class StoreManagementComponent implements OnInit {
  stores: Store[] = [];
  totalItems: number = 0;
  page: number = 0;
  size: number = 10;
  selectedStore: Store | null = null;
  storeForm!: FormGroup;

  storeItems: any[] = [];
  availableItems: any[] = [];
  selectedItemIdToAdd: number | null = null;

  constructor(
    private fb: FormBuilder,
    private storeService: StoreService,
    private itemService: ItemService
  ) {
    this.createForm();
  }

  ngOnInit(): void {
    this.loadStores();
    this.loadItems();
  }

  Math = Math;

  createForm() {
    this.storeForm = this.fb.group({
      id: [null],
      name: ['', Validators.required],
      description: ['']
    });
  }

  loadStores() {
    this.storeService.getAllStores(this.page, this.size).subscribe({
      next: (res) => {
        this.stores = res.stores;
        this.totalItems = res.total;
      },
      error: (err) => console.error('Error loading stores', err)
    });
  }

  prevPage() {
    if (this.page > 0) {
      this.page--;
      this.loadStores();
    }
  }

  nextPage() {
    if ((this.page + 1) * this.size < this.totalItems) {
      this.page++;
      this.loadStores();
    }
  }

  loadItems() {
    this.itemService.getItems(0, 1000, {}).subscribe({
      next: (data) => {
        this.availableItems = [...data.items].sort((a, b) => (a.name ?? '').localeCompare(b.name ?? ''));
      },
      error: (err) => console.error('Error loading items', err)
    });
  }

  itemDisplayFn = (item: any) => `${item.name} (${item.id})`;

  selectStore(store: Store) {
    this.selectedStore = store;
    this.storeForm.patchValue(store);
    this.selectedItemIdToAdd = null;
    this.loadStoreItems(store.id!);
  }

  loadStoreItems(storeId: number) {
    this.storeService.getStoreItems(storeId).subscribe({
      next: (data) => this.storeItems = data,
      error: (err) => console.error('Error loading store items', err)
    });
  }

  createNew() {
    this.selectedStore = null;
    this.storeForm.reset();
    this.storeItems = [];
    this.selectedItemIdToAdd = null;
  }

  save() {
    if (this.storeForm.invalid) return;

    const formValue = this.storeForm.value;

    if (formValue.id) {
      this.storeService.updateStore(formValue.id, formValue).subscribe({
        next: () => {
          this.loadStores();
          this.selectedStore = null;
        }
      });
    } else {
      this.storeService.createStore(formValue).subscribe({
        next: (created) => {
          this.loadStores();
          this.selectedStore = null;
        }
      });
    }
  }

  deleteStore(store: Store) {
    if (confirm(`Are you sure you want to delete '${store.name}'?`)) {
      this.storeService.deleteStore(store.id!).subscribe({
        next: () => {
          this.loadStores();
          if (this.selectedStore?.id === store.id) {
            this.createNew();
          }
        }
      });
    }
  }

  addStoreItem() {
    if (this.selectedStore && this.selectedStore.id && this.selectedItemIdToAdd) {
      this.storeService.addStoreItem(this.selectedStore.id, this.selectedItemIdToAdd).subscribe({
        next: () => {
          this.loadStoreItems(this.selectedStore!.id!);
          this.selectedItemIdToAdd = null;
        }
      });
    }
  }

  removeStoreItem(item: any) {
    if (this.selectedStore && this.selectedStore.id && item.id) {
      this.storeService.removeStoreItem(this.selectedStore.id, item.id).subscribe({
        next: () => {
          this.loadStoreItems(this.selectedStore!.id!);
        }
      });
    }
  }
}
