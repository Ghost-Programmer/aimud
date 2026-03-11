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
  private shouldScrollToBottom: boolean = false;
  private isAtBottom: boolean = true;

  constructor(private aiService: AiService) {}

  ngAfterViewChecked() {
    if (this.shouldScrollToBottom || this.isAtBottom) {
      this.scrollToBottom();
      this.shouldScrollToBottom = false;
    }
  }

  onScroll() {
    const container = this.scrollContainer.nativeElement;
    // Check if user is near the bottom (within 20px)
    this.isAtBottom = container.scrollHeight - container.scrollTop <= container.clientHeight + 20;
  }

  private scrollToBottom(): void {
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
    this.shouldScrollToBottom = true;

    this.aiService.processPrompt(currentPrompt).subscribe({
      next: (response) => {
        this.messages.push({ role: 'ai', text: response.response });
        this.isLoading = false;
        this.shouldScrollToBottom = true;
      },
      error: (error) => {
        console.error('AI Service Error:', error);
        this.messages.push({ role: 'ai', text: 'Sorry, I encountered an error processing your request.' });
        this.isLoading = false;
        this.shouldScrollToBottom = true;
      }
    });
  }

  closeDialog() {
    this.close.emit();
  }

  clearHistory() {
    this.messages = [];
  }
}
