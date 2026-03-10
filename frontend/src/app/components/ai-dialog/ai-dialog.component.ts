import { Component, EventEmitter, Output, Input, ElementRef, ViewChild, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AiService } from '../../services/ai.service';

@Component({
  selector: 'app-ai-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ai-dialog.component.html',
  styleUrl: './ai-dialog.component.css'
})
export class AiDialogComponent implements AfterViewChecked {
  @Input() isOpen: boolean = false;
  @Output() close = new EventEmitter<void>();
  @ViewChild('scrollContainer') private scrollContainer!: ElementRef;

  prompt: string = '';
  messages: { role: 'user' | 'ai', text: string }[] = [];
  isLoading: boolean = false;

  constructor(private aiService: AiService) {}

  ngAfterViewChecked() {
    this.scrollToBottom();
  }

  scrollToBottom(): void {
    try {
      this.scrollContainer.nativeElement.scrollTop = this.scrollContainer.nativeElement.scrollHeight;
    } catch(err) { }
  }

  sendMessage() {
    if (!this.prompt.trim()) return;

    const currentPrompt = this.prompt;
    this.messages.push({ role: 'user', text: currentPrompt });
    this.prompt = '';
    this.isLoading = true;

    this.aiService.processPrompt(currentPrompt).subscribe({
      next: (response) => {
        this.messages.push({ role: 'ai', text: response.response });
        this.isLoading = false;
      },
      error: (error) => {
        console.error('AI Service Error:', error);
        this.messages.push({ role: 'ai', text: 'Sorry, I encountered an error processing your request.' });
        this.isLoading = false;
      }
    });
  }

  closeDialog() {
    this.close.emit();
  }
}
