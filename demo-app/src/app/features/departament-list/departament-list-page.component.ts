import { ChangeDetectionStrategy, Component, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatToolbar } from '@angular/material/toolbar';
import { ConfirmDeleteDialogComponent } from '../../components/confirm-delete-dialog/confirm-delete-dialog.component';
import {
  DepartamentFormDialogComponent,
  DepartamentFormDialogData,
  DepartamentFormDialogResult,
} from '../../components/departament-form-dialog/departament-form-dialog.component';
import {
  CreateDepartamentDto,
  Departament,
  UpdateDepartamentDto,
} from '../../models/departament.model';
import { DepartamentListStore } from './departament-list.store';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-departament-list-page',
  imports: [MatTableModule, MatButtonModule, MatIconModule, MatDialogModule, MatToolbar],
  templateUrl: './departament-list-page.component.html',
  styleUrl: './departament-list-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DepartamentListPageComponent {
  private readonly dialog = inject(MatDialog);
  private readonly store = inject(DepartamentListStore);
  private readonly destroyRef = inject(DestroyRef);
  private readonly authService = inject(AuthService);

  protected readonly departaments = this.store.departaments;
  protected readonly personsByDepartament = this.store.personsByDepartament;
  protected readonly hasError = this.store.hasError;
  protected readonly errorMessage = this.store.errorMessage;
  protected readonly isLoading = this.store.isLoading;
  protected readonly displayedColumns = ['name', 'description', 'actions'];
  protected readonly memberColumns = ['name', 'age', 'email', 'role'];
  protected readonly currentUser = this.authService.currentUser;

  constructor() {
    this.store.load();
  }

  protected openCreateDialog(): void {
    if (this.isLoading() || !this.canCreateDepartament()) return;

    this.dialog
      .open<DepartamentFormDialogComponent, DepartamentFormDialogData, DepartamentFormDialogResult>(
        DepartamentFormDialogComponent,
        {
          data: {
            title: 'Create Departament',
            submitLabel: 'Create',
          },
        },
      )
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((result) => {
        if (!result) return;
        this.store.create(result as CreateDepartamentDto);
      });
  }

  protected openEditDialog(departament: Departament): void {
    if (this.isLoading() || !this.canEditDepartament(departament)) return;

    this.dialog
      .open<DepartamentFormDialogComponent, DepartamentFormDialogData, DepartamentFormDialogResult>(
        DepartamentFormDialogComponent,
        {
          data: {
            title: 'Edit Departament',
            submitLabel: 'Save',
            initialValue: {
              name: departament.name,
              description: departament.description,
            },
          },
        },
      )
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((result) => {
        if (!result) return;
        this.store.update(departament.id, result as UpdateDepartamentDto);
      });
  }

  protected openPatchDialog(departament: Departament): void {
    if (this.isLoading() || !this.canEditDepartament(departament)) return;

    this.dialog
      .open<DepartamentFormDialogComponent, DepartamentFormDialogData, DepartamentFormDialogResult>(
        DepartamentFormDialogComponent,
        {
          data: {
            title: 'Patch Departament',
            submitLabel: 'Patch',
            patchMode: true,
            initialValue: {
              name: departament.name,
              description: departament.description,
            },
          },
        },
      )
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((result) => {
        if (!result) return;
        this.store.patch(departament.id, result as Partial<UpdateDepartamentDto>);
      });
  }

  protected openDeleteDialog(departament: Departament): void {
    if (this.isLoading() || !this.canDeleteDepartament(departament)) return;

    this.dialog
      .open<ConfirmDeleteDialogComponent, { person: { name: string } }, boolean>(
        ConfirmDeleteDialogComponent,
        {
          data: {
            person: { name: departament.name },
          },
        },
      )
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((confirmed) => {
        if (!confirmed) return;
        this.store.remove(departament.id);
      });
  }

  protected canCreateDepartament(): boolean {
    const user = this.currentUser();
    return user?.role === 'TEAM_MANAGER';
  }

  protected canEditDepartament(departament: Departament): boolean {
    const user = this.currentUser();

    if (!user) return false;

    if (user.role === 'TEAM_MANAGER') {
      return true;
    }

    if (user.role === 'DEPARTMENT_HEAD') {
      return user.departamentId === departament.id;
    }

    return false;
  }

  protected canDeleteDepartament(departament: Departament): boolean {
    const user = this.currentUser();

    if (!user) return false;

    return user.role === 'TEAM_MANAGER';
  }

  protected hasAnyAction(departament: Departament): boolean {
    return this.canEditDepartament(departament) || this.canDeleteDepartament(departament);
  }
}