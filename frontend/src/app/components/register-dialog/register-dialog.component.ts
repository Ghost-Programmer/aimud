import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

@Component({
  selector: 'app-register-dialog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './register-dialog.component.html',
  styleUrl: './register-dialog.component.css'
})
export class RegisterDialogComponent {
  @Output() closeDialog = new EventEmitter<void>();

  registerForm: FormGroup;

  constructor(private fb: FormBuilder) {
    this.registerForm = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required],
      confirmPassword: ['', Validators.required]
    }, { validators: this.passwordMatchValidator });
  }

  passwordMatchValidator(g: FormGroup) {
    return g.get('password')?.value === g.get('confirmPassword')?.value
      ? null : { mismatch: true };
  }

  onSubmit() {
    if (this.registerForm.valid) {
      console.log('Registration details:', this.registerForm.value);
      this.closeDialog.emit();
    }
  }

  onCancel() {
    this.closeDialog.emit();
  }
}
