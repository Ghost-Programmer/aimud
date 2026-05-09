import { Component, Input, OnChanges, SimpleChanges, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Subscription } from 'rxjs';
import { GameWebSocketService } from '../../services/game-websocket.service';
import { CharacterService } from '../../services/character.service';

export interface MapNode {
  roomId: number;
  name: string;
  roomType: string;
  relativeX: number;
  relativeY: number;
  relativeZ: number;
  north: boolean;
  south: boolean;
  east: boolean;
  west: boolean;
  up: boolean;
  down: boolean;
}

export interface MapData {
  nodes: MapNode[];
  currentRoomId: number;
  currentZ: number;
  characterId: number;
}

@Component({
  selector: 'app-map-display',
  standalone: true,
  imports: [CommonModule, MatExpansionModule, MatButtonModule, MatIconModule, MatTooltipModule],
  templateUrl: './map-display.component.html',
  styleUrls: ['./map-display.component.css']
})
export class MapDisplayComponent implements OnChanges, OnDestroy {
  @Input() characterId!: number;
  @Input() isAdmin: boolean = false;

  mapData: MapData | null = null;
  currentZ: number = 0;
  
  gridSize = 7;
  gridRadius = 3;
  gridCells: (MapNode | null)[][] = [];

  private mapSub: Subscription | null = null;

  constructor(
    private wsService: GameWebSocketService,
    private characterService: CharacterService
  ) {}

  ngOnChanges(changes: SimpleChanges) {
    if (changes['characterId']) {
      this.subscribeToMapUpdates();
    }
  }

  ngOnDestroy() {
    if (this.mapSub) {
      this.mapSub.unsubscribe();
    }
  }

  subscribeToMapUpdates() {
    if (this.mapSub) {
      this.mapSub.unsubscribe();
    }
    if (!this.characterId) return;

    this.mapSub = this.wsService.getCharacterUpdates(this.characterId).subscribe((payload: any) => {
      if (payload.type === 'map_update') {
        this.mapData = payload.data;
        this.currentZ = this.mapData?.currentZ || 0;
        this.buildGrid();
      }
    });
  }

  buildGrid() {
    // Initialize empty grid
    this.gridCells = [];
    for (let y = 0; y < this.gridSize; y++) {
      let row = [];
      for (let x = 0; x < this.gridSize; x++) {
        row.push(null);
      }
      this.gridCells.push(row);
    }

    if (!this.mapData || !this.mapData.nodes) return;

    // Place nodes into grid based on relative X/Y at current Z
    for (let node of this.mapData.nodes) {
      if (node.relativeZ === this.currentZ) {
        let gridX = node.relativeX + this.gridRadius;
        let gridY = node.relativeY + this.gridRadius;
        
        if (gridX >= 0 && gridX < this.gridSize && gridY >= 0 && gridY < this.gridSize) {
          this.gridCells[gridY][gridX] = node;
        }
      }
    }
  }

  changeZ(delta: number) {
    this.currentZ += delta;
    this.buildGrid();
  }

  getRoomColorClass(type: string): string {
    switch(type) {
      case 'FOREST': return 'bg-forest';
      case 'CITY': return 'bg-city';
      case 'WATER_SURFACE': 
      case 'UNDERWATER': return 'bg-water';
      case 'MOUNTAIN': return 'bg-mountain';
      case 'DESERT': return 'bg-desert';
      case 'CAVE': return 'bg-cave';
      case 'SWAMP': return 'bg-swamp';
      case 'FIELD': return 'bg-field';
      case 'INDOORS': return 'bg-indoors';
      default: return 'bg-default';
    }
  }

  onRoomClick(node: MapNode) {
    if (this.isAdmin) {
      this.characterService.sendCommand(this.characterId, `goto ${node.roomId}`).subscribe();
    }
  }
}
