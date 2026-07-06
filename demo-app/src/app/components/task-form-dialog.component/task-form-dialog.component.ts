import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  inject,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Person } from '../../models/person.model';
import { PatchTaskDto, TaskStatus, UpdateTaskDto } from '../../models/task.model';

export interface TaskFormDialogData {
  title: string;
  submitLabel?: string;
  patchMode?: boolean;
  availablePersons: Person[];
  initialValue?: TaskFormInitialValue | null;

  canEditTitle?: boolean;
  canEditDescription?: boolean;
  canEditStatus?: boolean;
  canEditDeadline?: boolean;
  canEditAssignments?: boolean;
}

export interface TaskFormInitialValue {
  title: string;
  description: string;
  status: TaskStatus;
  deadline: string;
  personIds?: string[];
}

export type TaskFormDialogResult = UpdateTaskDto | PatchTaskDto | undefined;

@Component({
  selector: 'app-task-form-dialog',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
  ],
  templateUrl: './task-form-dialog.component.html',
  styleUrls: ['./task-form-dialog.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TaskFormDialogComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<TaskFormDialogComponent>);
  protected readonly data = inject<TaskFormDialogData>(MAT_DIALOG_DATA);

  protected readonly statuses: TaskStatus[] = ['TODO', 'IN_PROGRESS', 'DONE'];

  protected readonly form = this.fb.group({
    title: [''],
    description: [''],
    status: [null as TaskStatus | null],
    deadline: [''],
    personIds: [[] as string[]],
    addPersonIds: [[] as string[]],
    removePersonIds: [[] as string[]],
  });

  ngOnInit(): void {
    if (this.data.initialValue) {
      this.form.patchValue({
        title: this.data.initialValue.title,
        description: this.data.initialValue.description,
        status: this.data.initialValue.status,
        deadline: this.toDateTimeLocal(this.data.initialValue.deadline),
        personIds: this.data.initialValue.personIds ?? [],
      });
    }

    this.configureValidators();
    this.configureDisabledState();
  }

  protected canEditTitle(): boolean {
    return this.data.canEditTitle !== false;
  }

  protected canEditDescription(): boolean {
    return this.data.canEditDescription !== false;
  }

  protected canEditStatus(): boolean {
    return this.data.canEditStatus !== false;
  }

  protected canEditDeadline(): boolean {
    return this.data.canEditDeadline !== false;
  }

  protected canEditAssignments(): boolean {
    return this.data.canEditAssignments !== false;
  }

  private configureValidators(): void {
    if (this.canEditTitle()) {
      this.form.controls.title.addValidators([Validators.minLength(2), Validators.maxLength(100)]);
      if (!this.data.patchMode) {
        this.form.controls.title.addValidators([Validators.required]);
      }
    }

    if (this.canEditDescription()) {
      this.form.controls.description.addValidators([
        Validators.minLength(2),
        Validators.maxLength(500),
      ]);
      if (!this.data.patchMode) {
        this.form.controls.description.addValidators([Validators.required]);
      }
    }

    if (this.canEditStatus()) {
      if (!this.data.patchMode) {
        this.form.controls.status.addValidators([Validators.required]);
      }
    }

    if (this.canEditDeadline()) {
      if (!this.data.patchMode) {
        this.form.controls.deadline.addValidators([Validators.required]);
      }
    }

    if (this.canEditAssignments()) {
      if (!this.data.patchMode) {
        this.form.controls.personIds.addValidators([Validators.required]);
      }
    }

    this.form.controls.title.updateValueAndValidity();
    this.form.controls.description.updateValueAndValidity();
    this.form.controls.status.updateValueAndValidity();
    this.form.controls.deadline.updateValueAndValidity();
    this.form.controls.personIds.updateValueAndValidity();
    this.form.controls.addPersonIds.updateValueAndValidity();
    this.form.controls.removePersonIds.updateValueAndValidity();
  }

  private configureDisabledState(): void {
    if (!this.canEditTitle()) {
      this.form.controls.title.disable();
    }

    if (!this.canEditDescription()) {
      this.form.controls.description.disable();
    }

    if (!this.canEditStatus()) {
      this.form.controls.status.disable();
    }

    if (!this.canEditDeadline()) {
      this.form.controls.deadline.disable();
    }

    if (!this.canEditAssignments()) {
      this.form.controls.personIds.disable();
      this.form.controls.addPersonIds.disable();
      this.form.controls.removePersonIds.disable();
    }
  }

  private toDateTimeLocal(value: string): string {
    return value ? value.slice(0, 16) : '';
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();

    if (this.data.patchMode) {
      const patchPayload: PatchTaskDto = {};

      if (
        this.canEditTitle() &&
        raw.title &&
        raw.title !== this.data.initialValue?.title
      ) {
        patchPayload.title = raw.title;
      }

      if (
        this.canEditDescription() &&
        raw.description &&
        raw.description !== this.data.initialValue?.description
      ) {
        patchPayload.description = raw.description;
      }

      if (
        this.canEditStatus() &&
        raw.status &&
        raw.status !== this.data.initialValue?.status
      ) {
        patchPayload.status = raw.status;
      }

      if (this.canEditDeadline() && raw.deadline) {
        patchPayload.deadline = raw.deadline;
      }

      if (this.canEditAssignments() && raw.addPersonIds && raw.addPersonIds.length > 0) {
        patchPayload.addPersonIds = raw.addPersonIds;
      }

      if (this.canEditAssignments() && raw.removePersonIds && raw.removePersonIds.length > 0) {
        patchPayload.removePersonIds = raw.removePersonIds;
      }

      this.dialogRef.close(patchPayload);
      return;
    }

    const updatePayload: UpdateTaskDto = {
      title: this.canEditTitle() ? raw.title ?? '' : (this.data.initialValue?.title ?? ''),
      description: this.canEditDescription()
        ? raw.description ?? ''
        : (this.data.initialValue?.description ?? ''),
      status: this.canEditStatus()
        ? raw.status!
        : (this.data.initialValue?.status as TaskStatus),
      deadline: this.canEditDeadline()
        ? raw.deadline ?? ''
        : (this.toDateTimeLocal(this.data.initialValue?.deadline ?? '')),
      personIds: this.canEditAssignments()
        ? (raw.personIds ?? [])
        : (this.data.initialValue?.personIds ?? []),
    };

    this.dialogRef.close(updatePayload);
  }

  protected cancel(): void {
    this.dialogRef.close(undefined);
  }
}