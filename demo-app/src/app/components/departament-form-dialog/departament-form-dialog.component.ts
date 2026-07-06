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
import {
  DepartamentName,
  PatchDepartamentDto,
  UpdateDepartamentDto,
} from '../../models/departament.model';

export interface DepartamentFormDialogData {
  title: string;
  submitLabel?: string;
  patchMode?: boolean;
  initialValue?: DepartamentFormInitialValue | null;
}

export interface DepartamentFormInitialValue {
  name: DepartamentName;
  description: string;
}

export type DepartamentFormDialogResult =
  | UpdateDepartamentDto
  | PatchDepartamentDto
  | undefined;

@Component({
  selector: 'app-departament-form-dialog',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
  ],
  templateUrl: './departament-form-dialog.component.html',
  styleUrl: './departament-form-dialog.component.scss', 
   changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DepartamentFormDialogComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<DepartamentFormDialogComponent>);
  protected readonly data = inject<DepartamentFormDialogData>(MAT_DIALOG_DATA);

  protected readonly names: DepartamentName[] = [
    'ELECTRICAL',
    'SOFTWARE',
    'MECHANICAL',
    'PR',
    'SPONSORSHIP',
    'MANAGEMENT',
  ];

  protected readonly form = this.fb.group({
    name: [null as DepartamentName | null],
    description: ['', [Validators.minLength(10), Validators.maxLength(500)]],
  });

  ngOnInit(): void {
    if (this.data.initialValue) {
      this.form.patchValue({
        name: this.data.initialValue.name,
        description: this.data.initialValue.description,
      });
    }

    if (!this.data.patchMode) {
      this.form.controls.name.addValidators([Validators.required]);
      this.form.controls.description.addValidators([Validators.required]);
    }

    this.form.controls.name.updateValueAndValidity();
    this.form.controls.description.updateValueAndValidity();
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();

    if (this.data.patchMode) {
      const patchPayload: PatchDepartamentDto = {};

      if (raw.name && raw.name !== this.data.initialValue?.name) {
        patchPayload.name = raw.name;
      }

      if (
        raw.description &&
        raw.description !== this.data.initialValue?.description
      ) {
        patchPayload.description = raw.description;
      }

      this.dialogRef.close(patchPayload);
      return;
    }

    this.dialogRef.close({
      name: raw.name!,
      description: raw.description!,
    });
  }

  protected cancel(): void {
    this.dialogRef.close(undefined);
  }
}