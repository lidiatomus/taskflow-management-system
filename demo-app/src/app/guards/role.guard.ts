import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const roleGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const user = authService.currentUser();
  const allowedRoles = route.data?.['roles'] as string[] | undefined;

  if (!user) {
    router.navigate(['/login']);
    return false;
  }

  if (!allowedRoles || allowedRoles.includes(user.role)) {
    return true;
  }

  if (user.role === 'TEAM_MANAGER') {
    router.navigate(['/manager-dashboard']);
    return false;
  }

  if (user.role === 'DEPARTMENT_HEAD') {
    router.navigate(['/head-dashboard']);
    return false;
  }

  router.navigate(['/member-dashboard']);
  return false;
};