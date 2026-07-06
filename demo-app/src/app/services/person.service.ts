import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreatePersonDto, Person, UpdatePersonDto } from '../models/person.model';
import { AuthService } from './auth.service';

const API_URL = 'http://localhost:8080/person';

@Injectable({ providedIn: 'root' })
export class PersonService {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AuthService);

  private getAuthHeaders(): HttpHeaders {
    const token = this.authService.getToken();

    return new HttpHeaders({
      Authorization: `Bearer ${token ?? ''}`,
    });
  }

  getAll(): Observable<Person[]> {
    return this.http.get<Person[]>(API_URL, {
      headers: this.getAuthHeaders(),
    });
  }

  getById(id: string): Observable<Person> {
    return this.http.get<Person>(`${API_URL}/${id}`, {
      headers: this.getAuthHeaders(),
    });
  }

  getByDepartamentId(departamentId: string): Observable<Person[]> {
    return this.http.get<Person[]>(`${API_URL}/departament/${departamentId}`, {
      headers: this.getAuthHeaders(),
    });
  }

  create(dto: CreatePersonDto): Observable<Person> {
    return this.http.post<Person>(API_URL, dto, {
      headers: this.getAuthHeaders(),
    });
  }

  update(id: string, dto: UpdatePersonDto): Observable<Person> {
    return this.http.put<Person>(`${API_URL}/${id}`, dto, {
      headers: this.getAuthHeaders(),
    });
  }

  patch(id: string, dto: Partial<UpdatePersonDto>): Observable<Person> {
    return this.http.patch<Person>(`${API_URL}/${id}`, dto, {
      headers: this.getAuthHeaders(),
    });
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${API_URL}/${id}`, {
      headers: this.getAuthHeaders(),
    });
  }
}