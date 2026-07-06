import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const authRefreshInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (
        (error.status === 401 || error.status === 403) &&
        !req.url.includes('/login') &&
        !req.url.includes('/auth/verify-2fa') &&
        !req.url.includes('/auth/refresh')
      ) {
        return authService.refreshToken().pipe(
          switchMap(() => {
            const token = authService.getToken();

            const newRequest = req.clone({
              setHeaders: {
                Authorization: `Bearer ${token ?? ''}`
              }
            });

            return next(newRequest);
          })
        );
      }

      return throwError(() => error);
    })
  );
};