import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { finalize } from 'rxjs';
import { PasswordResetService } from '../../services/password-reset.service';

@Component({
  selector: 'app-forgot-password-page',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  templateUrl: './forgot-password-page.component.html',
  styleUrl: './forgot-password-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ForgotPasswordPageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly passwordResetService = inject(PasswordResetService);
  private readonly router = inject(Router);

  protected readonly isLoading = signal(false);
  protected readonly hasError = signal(false);
  protected readonly errorMessage = signal('');
  protected readonly successMessage = signal('');
  protected readonly codeSent = signal(false);

  protected readonly requestForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    newPassword: ['', [Validators.required]],
    confirmPassword: ['', [Validators.required]],
  });

  protected readonly confirmForm = this.fb.group({
    code: ['', [Validators.required]],
  });

  protected requestReset(): void {
    if (this.requestForm.invalid) {
      this.requestForm.markAllAsTouched();
      return;
    }

    const raw = this.requestForm.getRawValue();

    if (raw.newPassword !== raw.confirmPassword) {
      this.hasError.set(true);
      this.errorMessage.set('Passwords do not match.');
      return;
    }

    this.hasError.set(false);
    this.successMessage.set('');
    this.isLoading.set(true);

    this.passwordResetService
      .requestReset({
        email: raw.email ?? '',
        newPassword: raw.newPassword ?? '',
        confirmPassword: raw.confirmPassword ?? '',
      })
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: () => {
          this.codeSent.set(true);
          this.successMessage.set('Reset code sent. Check your email.');
        },
        error: (error: HttpErrorResponse) => this.showError(error),
      });
  }

  protected confirmReset(): void {
    if (this.confirmForm.invalid) {
      this.confirmForm.markAllAsTouched();
      return;
    }

    const email = this.requestForm.controls.email.value ?? '';
    const code = this.confirmForm.controls.code.value ?? '';

    this.hasError.set(false);
    this.successMessage.set('');
    this.isLoading.set(true);

    this.passwordResetService
      .confirmReset({ email, code })
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: () => {
          this.successMessage.set('Password changed successfully. You can now login.');
          setTimeout(() => this.router.navigate(['/login']), 1200);
        },
        error: (error: HttpErrorResponse) => this.showError(error),
      });
  }

  private showError(error: HttpErrorResponse): void {
    this.hasError.set(true);

    const backendError = error.error;

    if (typeof backendError === 'string') {
      this.errorMessage.set(backendError);
      return;
    }

    if (backendError?.message) {
      this.errorMessage.set(backendError.message);
      return;
    }

    if (backendError && typeof backendError === 'object') {
      this.errorMessage.set(Object.values(backendError).join(', '));
      return;
    }

    this.errorMessage.set('Something went wrong. Please try again.');
  }
}