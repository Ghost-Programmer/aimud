import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MAT_DIALOG_DATA, MatDialogRef, MatDialogModule, MatDialog} from '@angular/material/dialog';
import {StoreService} from '../../services/store.service';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {ItemStatsDialogComponent} from '../item-stats-dialog/item-stats-dialog.component';
import {FormsModule} from '@angular/forms';

import {ConfigService} from '../../services/config.service';

@Component({
  selector: 'app-store-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule, FormsModule],
  templateUrl: './store-dialog.component.html',
  styleUrls: ['./store-dialog.component.css']
})
export class StoreDialogComponent implements OnInit {
  storeData: any = null;
  loading: boolean = true;
  error: string | null = null;

  // Filter state
  filterName: string = '';
  filterType: string = '';
  filterLocation: string = '';
  filterMinValue: number | null = null;
  filterMaxValue: number | null = null;

  // Sort state
  sortColumnBuy: string = 'name';
  sortDirBuy: 'asc' | 'desc' = 'asc';
  sortColumnSell: string = 'name';
  sortDirSell: 'asc' | 'desc' = 'asc';

  // Common types and locations for the dropdowns
  itemTypes: string[] = [];
  wearLocations: string[] = [];

  constructor(
    public dialogRef: MatDialogRef<StoreDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { storeId: number, characterId: number },
    private storeService: StoreService,
    private configService: ConfigService,
    private matDialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.refreshDialog();
    this.configService.getItemTypes().subscribe({
      next: (types) => this.itemTypes = types,
      error: (err) => console.error('Failed to load item types', err)
    });
    this.configService.getWearLocations().subscribe({
      next: (locs) => this.wearLocations = locs,
      error: (err) => console.error('Failed to load wear locations', err)
    });
  }

  refreshDialog() {
    this.loading = true;
    this.storeService.getStoreDialog(this.data.storeId, this.data.characterId).subscribe({
      next: (data) => {
        this.storeData = data;
        this.loading = false;
        this.error = null;
      },
      error: (err) => {
        console.error('Error fetching store dialog', err);
        this.error = 'Failed to load store data.';
        this.loading = false;
      }
    });
  }

  buyItem(itemId: number) {
    this.loading = true;
    this.storeService.buyItem(this.data.storeId, itemId, this.data.characterId).subscribe({
      next: (update) => {
        this.storeData = update;
        this.loading = false;
      },
      error: (err) => {
         console.error('Error buying item', err);
         this.error = 'Failed to buy item.';
         this.loading = false;
         this.refreshDialog(); // recover state
      }
    });
  }

  sellItem(itemId: number) {
    this.loading = true;
    this.storeService.sellItem(this.data.storeId, itemId, this.data.characterId).subscribe({
      next: (update) => {
        this.storeData = update;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error selling item', err);
        this.error = 'Failed to sell item.';
        this.loading = false;
        this.refreshDialog(); // recover state
      }
    });
  }

  close() {
    this.dialogRef.close();
  }

  showItemStats(item: any) {
    if (!item) return;
    this.matDialog.open(ItemStatsDialogComponent, {
      width: '400px',
      data: { item }
    });
  }

  toggleSortBuy(column: string) {
    if (this.sortColumnBuy === column) {
      this.sortDirBuy = this.sortDirBuy === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortColumnBuy = column;
      this.sortDirBuy = 'asc';
    }
  }

  toggleSortSell(column: string) {
    if (this.sortColumnSell === column) {
      this.sortDirSell = this.sortDirSell === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortColumnSell = column;
      this.sortDirSell = 'asc';
    }
  }

  private filterItem(item: any, price: number): boolean {
    if (this.filterName && !item.name?.toLowerCase().includes(this.filterName.toLowerCase())) {
      return false;
    }
    if (this.filterType && item.itemType !== this.filterType) {
      return false;
    }
    if (this.filterLocation && item.wearLocation !== this.filterLocation) {
      return false;
    }
    if (this.filterMinValue !== null && price < this.filterMinValue) {
      return false;
    }
    if (this.filterMaxValue !== null && price > this.filterMaxValue) {
      return false;
    }
    return true;
  }

  private sortItems(items: any[], column: string, direction: 'asc' | 'desc', priceKey: string): any[] {
    return items.sort((a, b) => {
      let valA: any;
      let valB: any;

      switch (column) {
        case 'name':
          valA = a.item.name || '';
          valB = b.item.name || '';
          break;
        case 'count':
          valA = a.item.stackable ? a.item.count : 1;
          valB = b.item.stackable ? b.item.count : 1;
          break;
        case 'price':
          valA = a[priceKey];
          valB = b[priceKey];
          break;
        default:
          valA = a.item.name || '';
          valB = b.item.name || '';
      }

      let comparison = 0;
      if (valA > valB) {
        comparison = 1;
      } else if (valA < valB) {
        comparison = -1;
      }

      return direction === 'asc' ? comparison : -comparison;
    });
  }

  get filteredAndSortedStoreItems() {
    if (!this.storeData?.storeItems) return [];
    
    let filtered = this.storeData.storeItems.filter((si: any) => this.filterItem(si.item, si.buyPrice));
    return this.sortItems(filtered, this.sortColumnBuy, this.sortDirBuy, 'buyPrice');
  }

  get filteredAndSortedPlayerItems() {
    if (!this.storeData?.playerInventory) return [];

    let filtered = this.storeData.playerInventory.filter((pi: any) => this.filterItem(pi.item, pi.sellPrice));
    return this.sortItems(filtered, this.sortColumnSell, this.sortDirSell, 'sellPrice');
  }
}
