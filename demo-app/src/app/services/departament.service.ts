import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  CreateDepartamentDto,
  Departament,
  PatchDepartamentDto,
  UpdateDepartamentDto,
} from '../models/departament.model';
import { Person } from '../models/person.model';
import { AuthService } from './auth.service';

const DEPARTAMENT_API_URL = 'http://localhost:8080/departament';
const PERSON_API_URL = 'http://localhost:8080/person';

@Injectable({ providedIn: 'root' })
export class DepartamentService {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AuthService);

  private getAuthHeaders(): HttpHeaders {
    const token = this.authService.getToken();

    return new HttpHeaders({
      Authorization: `Bearer ${token ?? ''}`,
    });
  }

  getAll(): Observable<Departament[]> {
    return this.http.get<Departament[]>(DEPARTAMENT_API_URL, {
      headers: this.getAuthHeaders(),
    });
  }

  getById(id: string): Observable<Departament> {
    return this.http.get<Departament>(`${DEPARTAMENT_API_URL}/${id}`, {
      headers: this.getAuthHeaders(),
    });
  }

  create(dto: CreateDepartamentDto): Observable<Departament> {
    return this.http.post<Departament>(DEPARTAMENT_API_URL, dto, {
      headers: this.getAuthHeaders(),
    });
  }

  update(id: string, dto: UpdateDepartamentDto): Observable<Departament> {
    return this.http.put<Departament>(`${DEPARTAMENT_API_URL}/${id}`, dto, {
      headers: this.getAuthHeaders(),
    });
  }

  patch(id: string, dto: PatchDepartamentDto): Observable<Departament> {
    return this.http.patch<Departament>(`${DEPARTAMENT_API_URL}/${id}`, dto, {
      headers: this.getAuthHeaders(),
    });
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${DEPARTAMENT_API_URL}/${id}`, {
      headers: this.getAuthHeaders(),
    });
  }

  getPersonsByDepartamentId(id: string): Observable<Person[]> {
    return this.http.get<Person[]>(`${PERSON_API_URL}/departament/${id}`, {
      headers: this.getAuthHeaders(),
    });
  }
}