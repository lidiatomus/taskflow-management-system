import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatToolbar } from '@angular/material/toolbar';
import { ConfirmDeleteDialogComponent } from '../../components/confirm-delete-dialog/confirm-delete-dialog.component';
import {
  PersonFormDialogComponent,
  PersonFormDialogData,
  PersonFormDialogResult,
} from '../../components/person-form-dialog/person-form-dialog.component';
import { Departament } from '../../models/departament.model';
import { CreatePersonDto, Person, UpdatePersonDto } from '../../models/person.model';
import { DepartamentService } from '../../services/departament.service';
import { PersonListStore } from './person-list.store';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-person-list-page',
  imports: [MatTableModule, MatButtonModule, MatIconModule, MatDialogModule, MatToolbar],
  templateUrl: './person-list-page.component.html',
  styleUrl: './person-list-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PersonListPageComponent {
  private readonly dialog = inject(MatDialog);
  private readonly store = inject(PersonListStore);
  private readonly destroyRef = inject(DestroyRef);
  private readonly departamentService = inject(DepartamentService);
  private readonly authService = inject(AuthService);

  protected readonly persons = this.store.persons;
  protected readonly hasError = this.store.hasError;
  protected readonly errorMessage = this.store.errorMessage;
  protected readonly isLoading = this.store.isLoading;
  protected readonly availableDepartaments = signal<Departament[]>([]);
  protected readonly displayedColumns = ['name', 'age', 'email', 'role', 'departament', 'actions'];
  protected readonly currentUser = this.authService.currentUser;

  constructor() {
    this.store.load();
    this.loadDepartaments();
  }

  protected openCreateDialog(): void {
    if (this.isLoading() || !this.canCreate()) return;

    this.dialog
      .open<PersonFormDialogComponent, PersonFormDialogData, PersonFormDialogResult>(
        PersonFormDialogComponent,
        {
          data: {
            title: 'Create Person',
            submitLabel: 'Create',
            availableDepartaments: this.availableDepartaments(),
            canEditRole: true,
            canEditDepartament: true,
          },
        },
      )
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((result) => {
        if (!result) return;
        this.store.create(result as CreatePersonDto);
      });
  }

  protected openEditDialog(person: Person): void {
    if (this.isLoading() || !this.canEdit(person)) return;

    this.dialog
      .open<PersonFormDialogComponent, PersonFormDialogData, PersonFormDialogResult>(
        PersonFormDialogComponent,
        {
          data: {
            title: 'Edit Person',
            submitLabel: 'Save',
            availableDepartaments: this.availableDepartaments(),
            initialValue: {
              name: person.name,
              age: person.age,
              email: person.email,
              role: person.role,
              departamentId: person.departament?.id ?? '',
              
            },
            canEditRole: this.currentUser()?.role === 'TEAM_MANAGER',
            canEditDepartament: this.currentUser()?.role === 'TEAM_MANAGER',
          },
        },
      )
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((result) => {
        if (!result) return;
        this.store.update(person.id, result as UpdatePersonDto);
      });
  }

  protected openPatchDialog(person: Person): void {
    if (this.isLoading() || !this.canEdit(person)) return;

    this.dialog
      .open<PersonFormDialogComponent, PersonFormDialogData, PersonFormDialogResult>(
        PersonFormDialogComponent,
        {
          data: {
            title: 'Patch Person',
            submitLabel: 'Patch',
            availableDepartaments: this.availableDepartaments(),
            initialValue: {
              name: person.name,
              age: person.age,
              email: person.email,
              role: person.role,
              departamentId: person.departament?.id ?? '',
            },
            canEditRole: this.currentUser()?.role === 'TEAM_MANAGER',
            canEditDepartament: this.currentUser()?.role === 'TEAM_MANAGER',
            patchMode: true,
          },
        },
      )
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((result) => {
        if (!result) return;
        this.store.patch(person.id, result as Partial<UpdatePersonDto>);
      });
  }

  protected openDeleteDialog(person: Person): void {
    if (this.isLoading() || !this.canDelete(person)) return;

    this.dialog
      .open<ConfirmDeleteDialogComponent, { person: Person }, boolean>(
        ConfirmDeleteDialogComponent,
        { data: { person } },
      )
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((confirmed) => {
        if (!confirmed) return;
        this.store.remove(person.id);
      });
  }

  private loadDepartaments(): void {
    this.departamentService
      .getAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (departaments) => this.availableDepartaments.set(departaments),
        error: () => {},
      });
  }

  protected canEdit(person: Person): boolean {
    const user = this.currentUser();

    if (!user) return false;

    if (person.id === user.id) {
      return true;
    }

    if (user.role === 'TEAM_MANAGER') {
      return true;
    }

    if (user.role === 'DEPARTMENT_HEAD') {
      return !!user.departamentId && person.departament?.id === user.departamentId;
    }

    return false;
  }
  
  protected canDelete(person: Person): boolean {
    const user = this.currentUser();

    if (!user) return false;

    return user.role === 'TEAM_MANAGER';
  }

  protected canCreate(): boolean {
    const user = this.currentUser();
    return user?.role === 'TEAM_MANAGER';
  }

  protected canEditPassword(person: Person): boolean {
    const user = this.currentUser();

    if (!user) return false;

    return user.id === person.id;
  }
}