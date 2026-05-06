import {Pipe, PipeTransform} from '@angular/core';
import {DomSanitizer, SafeHtml} from '@angular/platform-browser';
import {parse} from 'marked';
import DOMPurify from 'dompurify';

@Pipe({
  name: 'markdown',
  standalone: true
})
export class MarkdownPipe implements PipeTransform {

  constructor(private sanitizer: DomSanitizer) {
  }

  transform(value: string | null | undefined): SafeHtml {
    if (!value) {
      return '';
    }

    // Parse markdown to raw HTML
    const rawHtml = parse(value) as string;
    
    // Sanitize the HTML to prevent XSS
    const sanitizedHtml = DOMPurify.sanitize(rawHtml);

    // Bypass Angular's built-in sanitizer since we already purified it
    return this.sanitizer.bypassSecurityTrustHtml(sanitizedHtml);
  }
}
