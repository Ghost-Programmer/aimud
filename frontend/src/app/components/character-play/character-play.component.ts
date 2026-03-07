import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-character-play',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './character-play.component.html',
  styleUrl: './character-play.component.css'
})
export class CharacterPlayComponent {
  @Input() character: any;
}
