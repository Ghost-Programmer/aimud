import {Injectable, Inject, PLATFORM_ID} from '@angular/core';
import {isPlatformBrowser} from '@angular/common';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  public isLightTheme = false;

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {
    if (isPlatformBrowser(this.platformId)) {
      const savedTheme = localStorage.getItem('theme');
      if (savedTheme === 'light') {
        this.isLightTheme = true;
        document.documentElement.setAttribute('data-theme', 'light');
      }
    }
  }

  toggleTheme() {
    this.isLightTheme = !this.isLightTheme;
    if (isPlatformBrowser(this.platformId)) {
      if (this.isLightTheme) {
        document.documentElement.setAttribute('data-theme', 'light');
        localStorage.setItem('theme', 'light');
      } else {
        document.documentElement.removeAttribute('data-theme');
        localStorage.setItem('theme', 'dark');
      }
    }
  }
}
