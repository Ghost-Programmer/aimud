import { Component, Input, Output, EventEmitter, forwardRef, ElementRef, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NG_VALUE_ACCESSOR, ControlValueAccessor } from '@angular/forms';

@Component({
  selector: 'app-searchable-dropdown',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './searchable-dropdown.component.html',
  styleUrl: './searchable-dropdown.component.css',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => SearchableDropdownComponent),
      multi: true
    }
  ]
})
export class SearchableDropdownComponent implements ControlValueAccessor {
  @Input() options: any[] = [];
  @Input() bindLabel: string | null = null;
  @Input() bindValue: string | null = null;
  @Input() displayFn?: (item: any) => string;
  @Input() placeholder: string = 'Select...';
  @Input() showNoneOption: boolean = false;
  @Input() noneLabel: string = 'None';

  searchText: string = '';
  isOpen: boolean = false;
  
  private _value: any = null;
  
  onChange: any = () => {};
  onTouched: any = () => {};

  constructor(private eRef: ElementRef) {}

  get value() {
    return this._value;
  }

  set value(val: any) {
    if (val !== this._value) {
      this._value = val;
      this.onChange(val);
      this.onTouched();
    }
  }

  writeValue(value: any): void {
    this._value = value;
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  get filteredOptions(): any[] {
    if (!this.searchText) return this.options;
    
    return this.options.filter(option => {
      const label = this.getLabel(option);
      return label ? label.toLowerCase().includes(this.searchText.toLowerCase()) : false;
    });
  }

  getLabel(option: any): string {
    if (option === null || option === undefined) return '';
    if (this.displayFn) return this.displayFn(option);
    return this.bindLabel ? option[this.bindLabel] : String(option);
  }

  getValue(option: any): any {
    if (option === null || option === undefined) return null;
    return this.bindValue ? option[this.bindValue] : option;
  }

  getSelectedLabel(): string {
    if (this._value === null || this._value === undefined) return this.placeholder;

    let selectedOption = null;
    const bindValKey = this.bindValue;
    if (bindValKey) {
      selectedOption = this.options.find(opt => opt[bindValKey] === this._value);
    } else {
      selectedOption = this.options.find(opt => opt === this._value);
    }
    
    return selectedOption ? this.getLabel(selectedOption) : this.placeholder;
  }

  toggleDropdown() {
    this.isOpen = !this.isOpen;
    if (this.isOpen) {
        this.searchText = '';
        setTimeout(() => {
            const inputElement = this.eRef.nativeElement.querySelector('.search-input');
            if(inputElement) inputElement.focus();
        }, 0);
    }
  }

  selectOption(option: any, event?: Event) {
    if (event) {
        event.stopPropagation();
    }
    this.value = this.getValue(option);
    this.isOpen = false;
  }

  clearSelection(event?: Event) {
    if (event) {
        event.stopPropagation();
    }
    this.value = null;
    this.isOpen = false;
  }

  @HostListener('document:click', ['$event'])
  clickout(event: any) {
    if(!this.eRef.nativeElement.contains(event.target)) {
      this.isOpen = false;
    }
  }
}
