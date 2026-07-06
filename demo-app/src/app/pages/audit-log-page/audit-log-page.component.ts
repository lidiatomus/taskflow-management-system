import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { MatTableModule } from '@angular/material/table';

import { AuditLogService } from '../../services/audit-log.service';
import { AuditLog } from '../../models/audit-log.model';

@Component({
  selector: 'app-audit-log-page',
  standalone: true,
  imports: [
    MatTableModule,
    DatePipe
  ],
  templateUrl: './audit-log-page.component.html',
  styleUrl: './audit-log-page.component.scss'
})
export class AuditLogPageComponent {

  private readonly auditLogService =
    inject(AuditLogService);

  protected readonly logs =
    signal<AuditLog[]>([]);

  protected readonly displayedColumns = [
    'createdAt',
    'actorEmail',
    'actorRole',
    'action',
    'entityType',
    'details'
  ];

  constructor() {

    this.auditLogService
      .getAll()
      .subscribe({
        next: (logs) =>
          this.logs.set(logs),

        error: (err) =>
          console.log(err)
      });
  }
}