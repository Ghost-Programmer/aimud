import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AiService } from '../../services/ai.service';

@Component({
  selector: 'app-area-builder',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './area-builder.component.html',
  styleUrl: './area-builder.component.css'
})
export class AreaBuilderComponent {
  prompt: string = '';
  aiResponse: string = '';
  isLoading: boolean = false;

  constructor(private aiService: AiService) {}

  sendPrompt() {
    if (!this.prompt.trim()) return;

    this.isLoading = true;
    this.aiResponse = '';

    this.aiService.processPrompt(this.prompt).subscribe({
      next: (res) => {
        this.aiResponse = res.response;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error sending prompt:', err);
        this.aiResponse = 'Error: Failed to get response from AI.';
        this.isLoading = false;
      }
    });
  }
}
