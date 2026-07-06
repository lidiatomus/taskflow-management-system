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
import { Departament } from '../../models/departament.model';
import { PatchPersonDto, PersonRole, UpdatePersonDto } from '../../models/person.model';

export interface PersonFormDialogData {
  title: string;
  submitLabel?: string;
  patchMode?: boolean;
  availableDepartaments: Departament[];
  initialValue?: PersonFormInitialValue | null;
  canEditRole?: boolean;
  canEditDepartament?: boolean;
}

export interface PersonFormInitialValue {
  name: string;
  age: number;
  email: string;
  role?: PersonRole;
  departamentId?: string;
}

export type PersonFormDialogResult = UpdatePersonDto | PatchPersonDto | undefined;

@Component({
  selector: 'app-person-form-dialog',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
  ],
  templateUrl: './person-form-dialog.component.html',
  styleUrl: './person-form-dialog.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PersonFormDialogComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<PersonFormDialogComponent>);
  protected readonly data = inject<PersonFormDialogData>(MAT_DIALOG_DATA);

  protected readonly roles: PersonRole[] = ['MEMBER', 'DEPARTMENT_HEAD', 'TEAM_MANAGER'];

  protected readonly form = this.fb.group({
    name: ['', [Validators.minLength(2), Validators.maxLength(100)]],
    age: [null as number | null, [Validators.min(0), Validators.max(100)]],
    email: ['', [Validators.email]],
    role: [null as PersonRole | null],
    departamentId: [''],
  });

  ngOnInit(): void {
    if (this.data.initialValue) {
      this.form.patchValue({
        name: this.data.initialValue.name,
        age: this.data.initialValue.age,
        email: this.data.initialValue.email,
        role: this.data.initialValue.role ?? null,
        departamentId: this.data.initialValue.departamentId ?? '',
      });
    }

    if (!this.data.patchMode) {
      this.form.controls.name.addValidators([Validators.required]);
      this.form.controls.age.addValidators([Validators.required]);
      this.form.controls.email.addValidators([Validators.required]);

      if (this.canEditRole()) {
        this.form.controls.role.addValidators([Validators.required]);
      }

      if (this.canEditDepartament()) {
        this.form.controls.departamentId.addValidators([Validators.required]);
      }
    }

    this.form.controls.name.updateValueAndValidity();
    this.form.controls.age.updateValueAndValidity();
    this.form.controls.email.updateValueAndValidity();
    this.form.controls.role.updateValueAndValidity();
    this.form.controls.departamentId.updateValueAndValidity();
  }

  protected canEditRole(): boolean {
    return this.data.canEditRole === true;
  }

  protected canEditDepartament(): boolean {
    return this.data.canEditDepartament === true;
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();

    if (this.data.patchMode) {
      const patchPayload: PatchPersonDto = {};

      if (raw.name && raw.name !== this.data.initialValue?.name) {
        patchPayload.name = raw.name;
      }

      if (raw.age !== null && raw.age !== this.data.initialValue?.age) {
        patchPayload.age = raw.age;
      }

      if (raw.email && raw.email !== this.data.initialValue?.email) {
        patchPayload.email = raw.email;
      }

      if (this.canEditRole() && raw.role && raw.role !== this.data.initialValue?.role) {
        patchPayload.role = raw.role;
      }

      if (
        this.canEditDepartament() &&
        raw.departamentId &&
        raw.departamentId !== this.data.initialValue?.departamentId
      ) {
        patchPayload.departamentId = raw.departamentId;
      }

      this.dialogRef.close(patchPayload);
      return;
    }

    const result: UpdatePersonDto = {
      name: raw.name!,
      age: raw.age!,
      email: raw.email!,
      role: raw.role!,
      departamentId: raw.departamentId!,
    };

    this.dialogRef.close(result);
  }

  protected cancel(): void {
    this.dialogRef.close(undefined);
  }
}