import { Injectable, computed, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

import {
  AuthUser,
  LoginRequest,
  LoginResponse,
  UserRole
} from '../models/auth.model';

const LOGIN_API_URL = 'http://localhost:8080/login';
const VERIFY_2FA_API_URL = 'http://localhost:8080/auth/verify-2fa';

const AUTH_STORAGE_KEY = 'auth_user';
const REFRESH_API_URL = 'http://localhost:8080/auth/refresh';
const LOGOUT_API_URL = 'http://localhost:8080/auth/logout';
@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly currentUserSignal =
    signal<AuthUser | null>(
      this.loadUserFromStorage()
    );

  readonly currentUser =
    this.currentUserSignal.asReadonly();

  readonly isLoggedIn =
    computed(() =>
      this.currentUserSignal() !== null
    );

  readonly role =
    computed<UserRole | null>(
      () =>
        this.currentUserSignal()?.role ?? null
    );

  constructor(
    private readonly http: HttpClient
  ) { }

  login(payload: LoginRequest) {
    return this.http.post<any>(
      LOGIN_API_URL,
      payload
    );
  }

  verify2FA(email: string, code: string): Observable<LoginResponse> {

    return this.http.post<LoginResponse>(
      'http://localhost:8080/auth/verify-2fa',
      {
        email,
        code
      }
    ).pipe(

      tap(response => {

        const user: AuthUser = {
          id: response.id,
          name: response.name,
          email: response.email,
          role: response.role,
          departamentId: response.departamentId,
          token: response.token,
          refreshToken: response.refreshToken
        };

        this.currentUserSignal.set(user);

        sessionStorage.setItem(
          AUTH_STORAGE_KEY,
          JSON.stringify(user)
        );

      })

    );
  }

  private clearSession(): void {
    this.currentUserSignal.set(null);
    sessionStorage.removeItem(AUTH_STORAGE_KEY);
  }

  logout(): void {
    const refreshToken = this.getRefreshToken();

    if (refreshToken) {
      this.http.post(LOGOUT_API_URL, { refreshToken }).subscribe({
        next: () => this.clearSession(),
        error: () => this.clearSession(),
      });

      return;
    }

    this.clearSession();
  }

  getCurrentUser(): AuthUser | null {

    return this.currentUserSignal();

  }

  getToken(): string | null {

    return this.currentUserSignal()
      ?.token ?? null;

  }

  isTeamManager(): boolean {

    return this.currentUserSignal()
      ?.role === 'TEAM_MANAGER';

  }

  isDepartmentHead(): boolean {

    return this.currentUserSignal()
      ?.role === 'DEPARTMENT_HEAD';

  }

  isMember(): boolean {

    return this.currentUserSignal()
      ?.role === 'MEMBER';

  }

  private loadUserFromStorage():
    AuthUser | null {

    const raw =
      sessionStorage.getItem(
        AUTH_STORAGE_KEY
      );

    if (!raw) {
      return null;
    }

    try {

      return JSON.parse(
        raw
      ) as AuthUser;

    }
    catch {

      sessionStorage.removeItem(
        AUTH_STORAGE_KEY
      );

      return null;

    }

  }
  getRefreshToken(): string | null {
    return this.currentUserSignal()?.refreshToken ?? null;
  }
  refreshToken(): Observable<LoginResponse> {
    const refreshToken = this.getRefreshToken();

    return this.http.post<LoginResponse>(REFRESH_API_URL, {
      refreshToken
    }).pipe(
      tap((response) => {
        const user: AuthUser = {
          id: response.id,
          name: response.name,
          email: response.email,
          role: response.role,
          departamentId: response.departamentId,
          token: response.token,
          refreshToken: response.refreshToken,
        };

        this.currentUserSignal.set(user);
        sessionStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(user));
      })
    );
  }

}