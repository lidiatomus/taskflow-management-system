import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateTaskDto, PatchTaskDto, Task, UpdateTaskDto } from '../models/task.model';
import { AuthService } from './auth.service';

const TASK_API_URL = 'http://localhost:8080/task';

@Injectable({ providedIn: 'root' })
export class TaskService {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AuthService);

  private getAuthHeaders(): HttpHeaders {
    const token = this.authService.getToken();

    return new HttpHeaders({
      Authorization: `Bearer ${token ?? ''}`,
    });
  }

  getAll(): Observable<Task[]> {
    return this.http.get<Task[]>(TASK_API_URL, {
      headers: this.getAuthHeaders(),
    });
  }

  getById(id: string): Observable<Task> {
    return this.http.get<Task>(`${TASK_API_URL}/${id}`, {
      headers: this.getAuthHeaders(),
    });
  }

  create(dto: CreateTaskDto): Observable<Task> {
    return this.http.post<Task>(TASK_API_URL, dto, {
      headers: this.getAuthHeaders(),
    });
  }

  update(id: string, dto: UpdateTaskDto): Observable<Task> {
    return this.http.put<Task>(`${TASK_API_URL}/${id}`, dto, {
      headers: this.getAuthHeaders(),
    });
  }

  patch(id: string, dto: PatchTaskDto): Observable<Task> {
    return this.http.patch<Task>(`${TASK_API_URL}/${id}`, dto, {
      headers: this.getAuthHeaders(),
    });
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${TASK_API_URL}/${id}`, {
      headers: this.getAuthHeaders(),
    });
  }

  getOverdue(): Observable<Task[]> {
    return this.http.get<Task[]>(`${TASK_API_URL}/overdue`, {
      headers: this.getAuthHeaders(),
    });
  }

  getInProgress(): Observable<Task[]> {
    return this.http.get<Task[]>(`${TASK_API_URL}/in-progress`, {
      headers: this.getAuthHeaders(),
    });
  }

  getByPersonId(personId: string): Observable<Task[]> {
    return this.http.get<Task[]>(`${TASK_API_URL}/person/${personId}`, {
      headers: this.getAuthHeaders(),
    });
  }
  requestApproval(id: string): Observable<Task> {
    return this.http.patch<Task>(
      `${TASK_API_URL}/${id}/request-approval`,
      {},
      {
        headers: this.getAuthHeaders(),
      }
    );
  }

  approve(id: string): Observable<Task> {
    return this.http.patch<Task>(
      `${TASK_API_URL}/${id}/approve`,
      {},
      {
        headers: this.getAuthHeaders(),
      }
    );
  }

  reject(id: string, comment: string): Observable<Task> {
    return this.http.patch<Task>(
      `${TASK_API_URL}/${id}/reject`,
      { comment },
      {
        headers: this.getAuthHeaders(),
      }
    );
  }
}