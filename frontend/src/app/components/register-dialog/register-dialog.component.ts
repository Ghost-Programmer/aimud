import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { UserService } from '../../services/user.service';

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
  errorMessage: string = '';

  constructor(private fb: FormBuilder, private userService: UserService) {
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
      const { username, password } = this.registerForm.value;
      this.userService.register({ username, password }).subscribe({
        next: (response) => {
          console.log('User registered successfully', response);
          this.closeDialog.emit();
        },
        error: (error) => {
          console.error('Registration failed', error);
          if (error.status === 409) {
             this.errorMessage = 'Username already exists';
          } else {
             this.errorMessage = error.error?.message || 'Registration failed';
          }
        }
      });
    }
  }

  onCancel() {
    this.closeDialog.emit();
  }
}
