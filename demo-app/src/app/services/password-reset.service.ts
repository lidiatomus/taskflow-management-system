import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  PasswordResetConfirm,
  PasswordResetRequest,
} from '../models/password-reset.model';

const API_URL = 'http://localhost:8080/password-reset';

@Injectable({ providedIn: 'root' })
export class PasswordResetService {
  private readonly http = inject(HttpClient);

  requestReset(dto: PasswordResetRequest): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${API_URL}/request`, dto);
  }

  confirmReset(dto: PasswordResetConfirm): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${API_URL}/confirm`, dto);
  }
}