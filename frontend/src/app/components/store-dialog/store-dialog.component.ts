import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MAT_DIALOG_DATA, MatDialogRef, MatDialogModule, MatDialog} from '@angular/material/dialog';
import {StoreService} from '../../services/store.service';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {ItemStatsDialogComponent} from '../item-stats-dialog/item-stats-dialog.component';

@Component({
  selector: 'app-store-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule],
  templateUrl: './store-dialog.component.html',
  styleUrls: ['./store-dialog.component.css']
})
export class StoreDialogComponent implements OnInit {
  storeData: any = null;
  loading: boolean = true;
  error: string | null = null;

  constructor(
    public dialogRef: MatDialogRef<StoreDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { storeId: number, characterId: number },
    private storeService: StoreService,
    private matDialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.refreshDialog();
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
}
