import { computed, inject, Injectable, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs';
import {
  CreateDepartamentDto,
  Departament,
  PatchDepartamentDto,
  UpdateDepartamentDto,
} from '../../models/departament.model';
import { Person } from '../../models/person.model';
import { DepartamentService } from '../../services/departament.service';

@Injectable({ providedIn: 'root' })
export class DepartamentListStore {
  private readonly departamentService = inject(DepartamentService);
  private readonly pendingRequests = signal(0);

  readonly departaments = signal<Departament[]>([]);
  readonly personsByDepartament = signal<Record<string, Person[]>>({});
  readonly hasError = signal(false);
  readonly errorMessage = signal('');
  readonly isLoading = computed(() => this.pendingRequests() > 0);

  private beginRequest(): void {
    this.pendingRequests.update((count) => count + 1);
  }

  private endRequest(): void {
    this.pendingRequests.update((count) => Math.max(0, count - 1));
  }

  private clearError(): void {
    this.hasError.set(false);
    this.errorMessage.set('');
  }

  private setError(error: HttpErrorResponse): void {
  this.hasError.set(true);

  const backendError = error.error;

  if (typeof backendError === 'string') {
    this.errorMessage.set(backendError);
    return;
  }

  if (typeof backendError === 'boolean') {
    this.errorMessage.set('An unexpected server response was received.');
    return;
  }

  if (backendError?.message && typeof backendError.message === 'string') {
    this.errorMessage.set(backendError.message);
    return;
  }

  if (backendError && typeof backendError === 'object') {
    const values = Object.values(backendError)
      .filter((value) => typeof value === 'string' && value.trim().length > 0);

    if (values.length > 0) {
      this.errorMessage.set(values.join(', '));
      return;
    }
  }

  this.errorMessage.set('An error occurred while processing your request. Please try again.');
}

  load(): void {
    this.clearError();
    this.beginRequest();

    this.departamentService
      .getAll()
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (data) => {
          this.departaments.set(data);
          data.forEach((d) => this.loadPersonsForDepartament(d.id));
        },
        error: (error: HttpErrorResponse) => this.setError(error),
      });
  }

  loadPersonsForDepartament(departamentId: string): void {
    this.beginRequest();

    this.departamentService
      .getPersonsByDepartamentId(departamentId)
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (persons) => {
          this.personsByDepartament.update((current) => ({
            ...current,
            [departamentId]: persons,
          }));
        },
        error: (error: HttpErrorResponse) => this.setError(error),
      });
  }

  create(dto: CreateDepartamentDto): void {
    this.clearError();
    this.beginRequest();

    this.departamentService
      .create(dto)
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (created) => {
          this.departaments.update((list) => [...list, created]);
          this.loadPersonsForDepartament(created.id);
        },
        error: (error: HttpErrorResponse) => this.setError(error),
      });
  }

  update(id: string, dto: UpdateDepartamentDto): void {
    this.clearError();
    this.beginRequest();

    this.departamentService
      .update(id, dto)
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (updated) =>
          this.departaments.update((list) =>
            list.map((departament) => (departament.id === updated.id ? updated : departament)),
          ),
        error: (error: HttpErrorResponse) => this.setError(error),
      });
  }

  patch(id: string, dto: PatchDepartamentDto): void {
    this.clearError();
    this.beginRequest();

    this.departamentService
      .patch(id, dto)
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (updated) =>
          this.departaments.update((list) =>
            list.map((departament) => (departament.id === updated.id ? updated : departament)),
          ),
        error: (error: HttpErrorResponse) => this.setError(error),
      });
  }

  remove(id: string): void {
    this.clearError();
    this.beginRequest();

    this.departamentService
      .delete(id)
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: () => {
          this.departaments.update((list) => list.filter((departament) => departament.id !== id));
          this.personsByDepartament.update((current) => {
            const copy = { ...current };
            delete copy[id];
            return copy;
          });
        },
        error: (error: HttpErrorResponse) => this.setError(error),
      });
  }
}