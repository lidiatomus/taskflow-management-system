import { computed, inject, Injectable, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs';
import { CreatePersonDto, Person, UpdatePersonDto } from '../../models/person.model';
import { PersonService } from '../../services/person.service';

@Injectable({ providedIn: 'root' })
export class PersonListStore {
  private readonly personService = inject(PersonService);
  private readonly pendingRequests = signal(0);

  readonly persons = signal<Person[]>([]);
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

  if (backendError?.message) {
    this.errorMessage.set(backendError.message);
    return;
  }

  if (backendError && typeof backendError === 'object') {
    const messages = Object.values(backendError).join(', ');
    this.errorMessage.set(messages);
    return;
  }

  this.errorMessage.set('An error occurred while processing your request. Please try again.');
}

  load(): void {
    this.clearError();
    this.beginRequest();
    this.personService
      .getAll()
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (data) => this.persons.set(data),
        error: (error: HttpErrorResponse) => this.setError(error),
      });
  }

  create(dto: CreatePersonDto): void {
    this.clearError();
    this.beginRequest();
    this.personService
      .create(dto)
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (created) => this.persons.update((list) => [...list, created]),
        error: (error: HttpErrorResponse) => this.setError(error),
      });
  }

  update(id: string, dto: UpdatePersonDto): void {
    this.clearError();
    this.beginRequest();
    this.personService
      .update(id, dto)
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (updated) =>
          this.persons.update((list) =>
            list.map((person) => (person.id === updated.id ? updated : person)),
          ),
        error: (error: HttpErrorResponse) => this.setError(error),
      });
  }

  patch(id: string, dto: Partial<UpdatePersonDto>): void {
    this.clearError();
    this.beginRequest();
    this.personService
      .patch(id, dto)
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (updated) =>
          this.persons.update((list) =>
            list.map((person) => (person.id === updated.id ? updated : person)),
          ),
        error: (error: HttpErrorResponse) => this.setError(error),
      });
  }

  remove(id: string): void {
    this.clearError();
    this.beginRequest();
    this.personService
      .delete(id)
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: () =>
          this.persons.update((list) => list.filter((person) => person.id !== id)),
        error: (error: HttpErrorResponse) => this.setError(error),
      });
  }
}