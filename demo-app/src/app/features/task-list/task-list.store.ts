import { computed, inject, Injectable, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs';
import { CreateTaskDto, PatchTaskDto, Task, UpdateTaskDto } from '../../models/task.model';
import { TaskService } from '../../services/task.service';

@Injectable({ providedIn: 'root' })
export class TaskListStore {
  private readonly taskService = inject(TaskService);
  private readonly pendingRequests = signal(0);

  readonly tasks = signal<Task[]>([]);
  readonly overdueTasks = signal<Task[]>([]);
  readonly inProgressTasks = signal<Task[]>([]);
  readonly hasError = signal(false);
  readonly errorMessage = signal('');
  readonly isLoading = computed(() => this.pendingRequests() > 0);
  readonly tasksBySelectedPerson = signal<Task[]>([]);
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
    this.loadTasks();
    this.loadOverdueTasks();
    this.loadInProgressTasks();
  }

  private loadTasks(): void {
    this.beginRequest();
    this.taskService
      .getAll()
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (data) => {
          console.log('TASKS FROM BACKEND:', data);
          this.tasks.set(data);
        },
        error: (error: HttpErrorResponse) => {
          console.error('TASKS ERROR:', error);
          this.setError(error);
        },
      });
  }

  private loadOverdueTasks(): void {
    this.beginRequest();
    this.taskService
      .getOverdue()
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (data) => this.overdueTasks.set(data),
        error: (error: HttpErrorResponse) => this.setError(error),
      });
  }

  private loadInProgressTasks(): void {
    this.beginRequest();
    this.taskService
      .getInProgress()
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (data) => this.inProgressTasks.set(data),
        error: (error: HttpErrorResponse) => this.setError(error),
      });
  }

  create(dto: CreateTaskDto): void {
    this.clearError();
    this.beginRequest();
    this.taskService
      .create(dto)
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (created) => {
          this.tasks.update((list) => [...list, created]);
          this.loadOverdueTasks();
          this.loadInProgressTasks();
        },
        error: (error: HttpErrorResponse) => this.setError(error),
      });
  }

  update(id: string, dto: UpdateTaskDto): void {
    this.clearError();
    this.beginRequest();
    this.taskService
      .update(id, dto)
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (updated) => {
          this.tasks.update((list) =>
            list.map((task) => (task.id === updated.id ? updated : task)),
          );
          this.loadOverdueTasks();
          this.loadInProgressTasks();
        },
        error: (error: HttpErrorResponse) => this.setError(error),
      });
  }

  patch(id: string, dto: PatchTaskDto): void {
    this.clearError();
    this.beginRequest();
    this.taskService
      .patch(id, dto)
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (updated) => {
          this.tasks.update((list) =>
            list.map((task) => (task.id === updated.id ? updated : task)),
          );
          this.loadOverdueTasks();
          this.loadInProgressTasks();
        },
        error: (error: HttpErrorResponse) => this.setError(error),
      });
  }

  remove(id: string): void {
    this.clearError();
    this.beginRequest();
    this.taskService
      .delete(id)
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: () => {
          this.tasks.update((list) => list.filter((task) => task.id !== id));
          this.loadOverdueTasks();
          this.loadInProgressTasks();
        },
        error: (error: HttpErrorResponse) => this.setError(error),
      });
  }

  loadTasksByPersonId(personId: string): void {
    this.clearError();
    this.beginRequest();
    this.taskService
      .getByPersonId(personId)
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (data) => this.tasksBySelectedPerson.set(data),
        error: (error: HttpErrorResponse) => this.setError(error),
      });
  }
  clearTasksBySelectedPerson(): void {
    this.tasksBySelectedPerson.set([]);
  }

  requestApproval(id: string): void {
    this.clearError();
    this.beginRequest();

    this.taskService
      .requestApproval(id)
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (updated) => {
          this.tasks.update((list) =>
            list.map((task) => (task.id === updated.id ? updated : task)),
          );
          this.loadInProgressTasks();
          this.loadOverdueTasks();
        },
        error: (error: HttpErrorResponse) => this.setError(error),
      });
  }

  approve(id: string): void {
    this.clearError();
    this.beginRequest();

    this.taskService
      .approve(id)
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (updated) => {
          this.tasks.update((list) =>
            list.map((task) => (task.id === updated.id ? updated : task)),
          );
          this.loadInProgressTasks();
          this.loadOverdueTasks();
        },
        error: (error: HttpErrorResponse) => this.setError(error),
      });
  }

  reject(id: string): void {
    const comment = prompt('Reject reason:') ?? '';

    this.clearError();
    this.beginRequest();

    this.taskService
      .reject(id, comment)
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (updated) => {
          this.tasks.update((list) =>
            list.map((task) => (task.id === updated.id ? updated : task)),
          );
          this.loadInProgressTasks();
          this.loadOverdueTasks();
        },
        error: (error: HttpErrorResponse) => this.setError(error),
      });
  }
}