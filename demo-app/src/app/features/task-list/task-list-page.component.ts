import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatToolbar } from '@angular/material/toolbar';
import { ConfirmDeleteDialogComponent } from '../../components/confirm-delete-dialog/confirm-delete-dialog.component';
import {
  TaskFormDialogComponent,
  TaskFormDialogData,
  TaskFormDialogResult,
} from '../../components/task-form-dialog.component/task-form-dialog.component';
import { CreateTaskDto, PatchTaskDto, Task, UpdateTaskDto } from '../../models/task.model';
import { TaskListStore } from './task-list.store';
import { Person } from '../../models/person.model';
import { PersonService } from '../../services/person.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-task-list-page',
  standalone: true,
  imports: [
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatToolbar,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    FormsModule,
  ],
  templateUrl: './task-list-page.component.html',
  styleUrls: ['./task-list-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TaskListPageComponent {
  private readonly dialog = inject(MatDialog);
  private readonly store = inject(TaskListStore);
  private readonly destroyRef = inject(DestroyRef);
  private readonly personService = inject(PersonService);
  private readonly authService = inject(AuthService);

  protected readonly tasks = this.store.tasks;
  protected readonly overdueTasks = this.store.overdueTasks;
  protected readonly inProgressTasks = this.store.inProgressTasks;
  protected readonly hasError = this.store.hasError;
  protected readonly errorMessage = this.store.errorMessage;
  protected readonly isLoading = this.store.isLoading;
  protected readonly currentUser = this.authService.currentUser;

  protected readonly availablePersons = signal<Person[]>([]);
  protected readonly selectedPersonId = signal('');
  protected readonly tasksBySelectedPerson = this.store.tasksBySelectedPerson;

  protected readonly displayedColumns = [
    'title',
    'description',
    'status',
    'deadline',
    'timeRemaining',
    'completionStatus',
    'assignedPersons',
    'actions',
  ];

  protected readonly taskInfoColumns = [
    'title',
    'description',
    'status',
    'deadline',
    'timeRemaining',
    'completionStatus',
    'assignedPersons',
  ];

  protected readonly searchTerm = signal('');
  protected readonly selectedStatus = signal<string | null>(null);
  protected readonly sortDirection = signal<'asc' | 'desc'>('asc');

  private uniqueTasks(tasks: Task[]): Task[] {
    const seen = new Set<string>();
    return tasks.filter((task) => {
      if (seen.has(task.id)) {
        return false;
      }
      seen.add(task.id);
      return true;
    });
  }

  protected readonly visibleTasks = computed(() => {
    let tasks = this.filterTasksForCurrentUser(this.tasks());

    const term = this.searchTerm().toLowerCase();
    if (term) {
      tasks = tasks.filter(t => t.title.toLowerCase().includes(term));
    }

    if (this.selectedStatus()) {
      tasks = tasks.filter(t => t.status === this.selectedStatus());
    }

    tasks = [...tasks].sort((a, b) => {
      const dateA = new Date(a.deadline).getTime();
      const dateB = new Date(b.deadline).getTime();

      return this.sortDirection() === 'asc'
        ? dateA - dateB
        : dateB - dateA;
    });

    return tasks;
  });

  protected readonly visibleOverdueTasks = computed(() =>
    this.uniqueTasks(this.filterTasksForCurrentUser(this.overdueTasks())),
  );

  protected readonly visibleInProgressTasks = computed(() =>
    this.uniqueTasks(this.filterTasksForCurrentUser(this.inProgressTasks())),
  );

  protected readonly visibleTasksBySelectedPerson = computed(() =>
    this.uniqueTasks(this.filterTasksForCurrentUser(this.tasksBySelectedPerson())),
  );

  protected readonly assignablePersons = computed(() => {
    const user = this.currentUser();
    const persons = this.availablePersons();

    if (!user) return [];

    if (user.role === 'TEAM_MANAGER') {
      return persons;
    }

    if (user.role === 'DEPARTMENT_HEAD') {
      return persons.filter((person) => person.departament?.id === user.departamentId);
    }

    return [];
  });

  constructor() {
    this.store.load();
    this.loadPersons();

    setTimeout(() => {
      this.showApprovalNotificationsOnce();
    }, 1000);
  }

  protected openCreateDialog(): void {
    if (this.isLoading() || !this.canCreateTask()) return;

    this.dialog
      .open<TaskFormDialogComponent, TaskFormDialogData, TaskFormDialogResult>(
        TaskFormDialogComponent,
        {
          data: {
            title: 'Create Task',
            submitLabel: 'Create',
            availablePersons: this.assignablePersons(),
            canEditTitle: true,
            canEditDescription: true,
            canEditStatus: true,
            canEditDeadline: true,
            canEditAssignments: true,
          },
        },
      )
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((result) => {
        if (!result) return;
        this.store.create(result as CreateTaskDto);
      });
  }

  protected openEditDialog(task: Task): void {
    if (this.isLoading() || !this.canUseFullEdit(task)) return;

    this.dialog
      .open<TaskFormDialogComponent, TaskFormDialogData, TaskFormDialogResult>(
        TaskFormDialogComponent,
        {
          data: {
            title: 'Edit Task',
            submitLabel: 'Save',
            initialValue: {
              title: task.title,
              description: task.description,
              status: task.status,
              deadline: task.deadline,
              personIds: task.assignedPersons.map((p) => p.id),
            },
            availablePersons: this.assignablePersons(),
            canEditTitle: true,
            canEditDescription: true,
            canEditStatus: true,
            canEditDeadline: true,
            canEditAssignments: true,
          },
        },
      )
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((result) => {
        if (!result) return;
        this.store.update(task.id, result as UpdateTaskDto);
      });
  }

  protected openPatchDialog(task: Task): void {
    if (this.isLoading() || !this.canEditTask(task)) return;

    const memberEditOnlyStatus = this.isMemberEditingOwnTask(task);

    this.dialog
      .open<TaskFormDialogComponent, TaskFormDialogData, TaskFormDialogResult>(
        TaskFormDialogComponent,
        {
          data: {
            title: memberEditOnlyStatus ? 'Update Task Status' : 'Patch Task',
            submitLabel: 'Patch',
            patchMode: true,
            initialValue: {
              title: task.title,
              description: task.description,
              status: task.status,
              deadline: task.deadline,
              personIds: task.assignedPersons.map((p) => p.id),
            },
            availablePersons: memberEditOnlyStatus ? [] : this.assignablePersons(),
            canEditTitle: !memberEditOnlyStatus,
            canEditDescription: !memberEditOnlyStatus,
            canEditStatus: true,
            canEditDeadline: !memberEditOnlyStatus,
            canEditAssignments: !memberEditOnlyStatus,
          },
        },
      )
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((result) => {
        if (!result) return;

        if (memberEditOnlyStatus) {
          const statusOnlyResult = result as PatchTaskDto;
          this.store.patch(task.id, { status: statusOnlyResult.status });
          return;
        }

        this.store.patch(task.id, result as PatchTaskDto);
      });
  }

  protected openDeleteDialog(task: Task): void {
    if (this.isLoading() || !this.canDeleteTask()) return;

    this.dialog
      .open<ConfirmDeleteDialogComponent, { person: { name: string } }, boolean>(
        ConfirmDeleteDialogComponent,
        {
          data: {
            person: { name: task.title },
          },
        },
      )
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((confirmed) => {
        if (!confirmed) return;
        this.store.remove(task.id);
      });
  }

  protected formatAssignedPersons(task: Task): string {
    return task.assignedPersons?.map((p) => p.name).join(', ') || '-';
  }

  private loadPersons(): void {
    this.personService
      .getAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (data) => this.availablePersons.set(data),
        error: () => { },
      });
  }

  protected loadSelectedPersonTasks(): void {
    const personId = this.selectedPersonId();

    if (!personId) {
      this.store.clearTasksBySelectedPerson();
      return;
    }

    this.store.loadTasksByPersonId(personId);
  }

  protected clearSelectedPersonTasks(): void {
    this.selectedPersonId.set('');
    this.store.clearTasksBySelectedPerson();
  }

  protected canCreateTask(): boolean {
    const user = this.currentUser();

    if (!user) return false;

    return user.role === 'TEAM_MANAGER' || user.role === 'DEPARTMENT_HEAD';
  }

  protected canUseFullEdit(task: Task): boolean {
    const user = this.currentUser();

    if (!user) return false;

    if (user.role === 'TEAM_MANAGER') {
      return true;
    }

    if (user.role === 'DEPARTMENT_HEAD') {
      return this.belongsToHeadsDepartment(task, user.departamentId);
    }

    return false;
  }

  protected canEditTask(task: Task): boolean {
    const user = this.currentUser();

    if (!user) return false;

    if (user.role === 'TEAM_MANAGER') {
      return true;
    }

    if (user.role === 'DEPARTMENT_HEAD') {
      return this.belongsToHeadsDepartment(task, user.departamentId);
    }

    if (user.role === 'MEMBER') {
      return task.assignedPersons?.some((person) => person.id === user.id) ?? false;
    }

    return false;
  }

  protected canDeleteTask(): boolean {
    return this.currentUser()?.role === 'TEAM_MANAGER';
  }

  protected hasAnyAction(task: Task): boolean {
    return this.showEditButton(task) ||
      this.showPatchButton(task) ||
      this.canDeleteTask() ||
      this.canRequestApproval(task) ||
      this.canApproveOrReject(task);
  }

  protected showEditButton(task: Task): boolean {
    return this.canUseFullEdit(task);
  }

  protected showPatchButton(task: Task): boolean {
    return this.canEditTask(task);
  }

  private isMemberEditingOwnTask(task: Task): boolean {
    const user = this.currentUser();

    if (!user || user.role !== 'MEMBER') {
      return false;
    }

    return task.assignedPersons?.some((person) => person.id === user.id) ?? false;
  }

  private belongsToHeadsDepartment(task: Task, departamentId: string | null): boolean {
    if (!departamentId) return false;

    return task.assignedPersons?.some(
      (person) => person.departament?.id === departamentId,
    ) ?? false;
  }

  private filterTasksForCurrentUser(tasks: Task[]): Task[] {
    const user = this.currentUser();

    if (!user) return [];

    if (user.role === 'TEAM_MANAGER') {
      return tasks;
    }

    if (user.role === 'DEPARTMENT_HEAD') {
      return tasks.filter((task) => this.belongsToHeadsDepartment(task, user.departamentId));
    }

    return tasks.filter((task) =>
      task.assignedPersons?.some((person) => person.id === user.id),
    );
  }
  protected requestApproval(task: Task): void {
    this.store.requestApproval(task.id);
  }

  protected approveTask(task: Task): void {
    this.store.approve(task.id);
  }

  protected rejectTask(task: Task): void {
    this.store.reject(task.id);
  }

  protected canRequestApproval(task: Task): boolean {
    const user = this.currentUser();

    return !!user &&
      user.role === 'MEMBER' &&
      task.status === 'IN_PROGRESS' &&
      task.assignedPersons?.some((person) => person.id === user.id);
  }

  protected canApproveOrReject(task: Task): boolean {
    const user = this.currentUser();

    return !!user &&
      task.status === 'PENDING_APPROVAL' &&
      (
        user.role === 'TEAM_MANAGER' ||
        (
          user.role === 'DEPARTMENT_HEAD' &&
          this.belongsToHeadsDepartment(task, user.departamentId)
        )
      );
  }

  private showApprovalNotificationsOnce(): void {
    const user = this.currentUser();

    if (!user || user.role !== 'MEMBER') {
      return;
    }

    const tasks = this.visibleTasks();

    const rejectedTasks = tasks.filter((task) =>
      task.status === 'IN_PROGRESS' &&
      !!task.approvalComment &&
      task.assignedPersons?.some((person) => person.id === user.id)
    );

    const approvedTasks = tasks.filter((task) =>
      task.status === 'DONE' &&
      !!task.approvalResolvedAt &&
      task.assignedPersons?.some((person) => person.id === user.id)
    );

    const messages: string[] = [];

    for (const task of rejectedTasks) {
      const key = `rejected-${user.id}-${task.id}-${task.approvalResolvedAt}`;

      if (!localStorage.getItem(key)) {
        messages.push(
          `Task rejected: ${task.title}\nReason: ${task.approvalComment}`
        );

        localStorage.setItem(key, 'shown');
      }
    }

    for (const task of approvedTasks) {
      const key = `approved-${user.id}-${task.id}-${task.approvalResolvedAt}`;

      if (!localStorage.getItem(key)) {
        messages.push(
          `Task approved: ${task.title}`
        );

        localStorage.setItem(key, 'shown');
      }
    }

    if (messages.length > 0) {
      alert(messages.join('\n\n'));
    }
  }
}