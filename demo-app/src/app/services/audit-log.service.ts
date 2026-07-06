import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuditLog } from '../models/audit-log.model';
import { AuthService } from './auth.service';

const AUDIT_LOG_API_URL = 'http://localhost:8080/audit-log';

@Injectable({
  providedIn: 'root'
})
export class AuditLogService {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AuthService);

  private getAuthHeaders(): HttpHeaders {
    const token = this.authService.getToken();

    return new HttpHeaders({
      Authorization: `Bearer ${token ?? ''}`,
    });
  }

  getAll(): Observable<AuditLog[]> {
    return this.http.get<AuditLog[]>(AUDIT_LOG_API_URL, {
      headers: this.getAuthHeaders(),
    });
  }
}