import {
  ChangeDetectionStrategy,
  Component,
  OnDestroy,
  inject,
  signal
} from '@angular/core';

import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import { HttpErrorResponse } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';

import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

import {
  finalize,
  interval,
  Subscription
} from 'rxjs';

import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatCardModule,
    RouterLink
  ],
  templateUrl: './login-page.component.html',
  styleUrl: './login-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginPageComponent implements OnDestroy {

  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly isLoading = signal(false);
  protected readonly hasError = signal(false);
  protected readonly errorMessage = signal('');

  protected readonly show2FA = signal(false);
  protected readonly lockRemaining = signal(0);

  private timerSubscription?: Subscription;

  protected readonly form = this.fb.group({
    email: [
      '',
      [Validators.required, Validators.email]
    ],
    password: [
      '',
      [Validators.required]
    ]
  });

  protected readonly codeForm = this.fb.group({
    code: [
      '',
      [Validators.required]
    ]
  });

  submit(): void {
    if (this.form.invalid || this.lockRemaining() > 0) {
      this.form.markAllAsTouched();
      return;
    }

    this.hasError.set(false);
    this.errorMessage.set('');
    this.isLoading.set(true);

    const raw = this.form.getRawValue();

    this.authService
      .login({
        email: raw.email ?? '',
        password: raw.password ?? ''
      })
      .pipe(
        finalize(() => this.isLoading.set(false))
      )
      .subscribe({
        next: () => {
          this.show2FA.set(true);
        },
        error: (error: HttpErrorResponse) => {
          this.handleError(error);
        }
      });
  }

  verify2FA(): void {
    if (this.codeForm.invalid) {
      this.codeForm.markAllAsTouched();
      return;
    }

    this.hasError.set(false);
    this.errorMessage.set('');
    this.isLoading.set(true);

    const email = this.form.getRawValue().email ?? '';
    const code = this.codeForm.getRawValue().code ?? '';

    this.authService
      .verify2FA(email, code)
      .pipe(
        finalize(() => this.isLoading.set(false))
      )
      .subscribe({
        next: (response) => {
          if (response.role === 'TEAM_MANAGER') {
            this.router.navigate(['/manager-dashboard']);
            return;
          }

          if (response.role === 'DEPARTMENT_HEAD') {
            this.router.navigate(['/head-dashboard']);
            return;
          }

          this.router.navigate(['/member-dashboard']);
        },
        error: (error: HttpErrorResponse) => {
          this.handleError(error);
        }
      });
  }

  protected get formattedTime(): string {
    const seconds = this.lockRemaining();

    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;

    return `${minutes.toString().padStart(2, '0')}:${remainingSeconds
      .toString()
      .padStart(2, '0')}`;
  }

  private handleError(error: HttpErrorResponse): void {
    this.hasError.set(true);

    const message = this.extractErrorMessage(error);

    this.errorMessage.set(message);

    const seconds = this.extractSecondsFromMessage(message);

    if (seconds !== null) {
      this.startCountdown(seconds);
    }
  }

  private extractErrorMessage(error: HttpErrorResponse): string {
    const backendError = error.error;

    if (typeof backendError === 'string') {
      return backendError;
    }

    if (backendError?.message) {
      return backendError.message;
    }

    if (backendError && typeof backendError === 'object') {
      return Object.values(backendError).join(', ');
    }

    return 'Something went wrong';
  }

  private extractSecondsFromMessage(message: string): number | null {
  const match = message.match(/(\d+)/);

  if (!match) {
    return null;
  }

  if (!message.toLowerCase().includes('too many failed login attempts')) {
    return null;
  }

  return Number(match[1]);
}

  private startCountdown(seconds: number): void {
    this.lockRemaining.set(seconds);
    this.timerSubscription?.unsubscribe();

    this.timerSubscription = interval(1000).subscribe(() => {
      const remaining = this.lockRemaining() - 1;

      if (remaining <= 0) {
        this.lockRemaining.set(0);
        this.timerSubscription?.unsubscribe();
        return;
      }

      this.lockRemaining.set(remaining);
    });
  }

  ngOnDestroy(): void {
    this.timerSubscription?.unsubscribe();
  }
}