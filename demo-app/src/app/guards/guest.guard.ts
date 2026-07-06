import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const guestGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isLoggedIn()) {
    return true;
  }

  const user = authService.getCurrentUser();

  if (user?.role === 'TEAM_MANAGER') {
    return router.createUrlTree(['/manager-dashboard']);
  }

  if (user?.role === 'DEPARTMENT_HEAD') {
    return router.createUrlTree(['/head-dashboard']);
  }

  return router.createUrlTree(['/member-dashboard']);
};