import { Routes } from '@angular/router';
import { MemberDashboardComponent } from './pages/member-dashboard/member-dashboard.component';
import { HeadDashboardComponent } from './pages/head-dashboard/head-dashboard.component';
import { ManagerDashboardComponent } from './pages/manager-dashboard/manager-dashboard.component';
import { LoginPageComponent } from './pages/login-page/login-page.component';
import { guestGuard } from './guards/guest.guard';
import { roleGuard } from './guards/role.guard';
import { ForgotPasswordPageComponent } from './pages/forgot-password-page/forgot-password-page.component';
import { AuditLogPageComponent } from './pages/audit-log-page/audit-log-page.component';
import { authGuard } from './guards/auth.guard';
import { AiAssistantPageComponent } from './pages/ai-assistant-page/ai-assistant-page.component';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },

  { path: 'login', component: LoginPageComponent, canActivate: [guestGuard] },

  {
    path: 'manager-dashboard',
    component: ManagerDashboardComponent,
    canActivate: [roleGuard],
    data: { roles: ['TEAM_MANAGER'] },
  },
  {
    path: 'head-dashboard',
    component: HeadDashboardComponent,
    canActivate: [roleGuard],
    data: { roles: ['DEPARTMENT_HEAD'] },
  },
  {
    path: 'member-dashboard',
    component: MemberDashboardComponent,
    canActivate: [roleGuard],
    data: { roles: ['MEMBER'] },
  },

  {
    path: 'people',
    canActivate: [roleGuard],
    data: { roles: ['TEAM_MANAGER', 'DEPARTMENT_HEAD'] },
    loadComponent: () =>
      import('./features/person-list/person-list-page.component').then(
        (m) => m.PersonListPageComponent,
      ),
  },
  {
    path: 'departaments',
    canActivate: [roleGuard],
    data: { roles: ['TEAM_MANAGER', 'DEPARTMENT_HEAD', 'MEMBER'] },
    loadComponent: () =>
      import('./features/departament-list/departament-list-page.component').then(
        (m) => m.DepartamentListPageComponent,
      ),
  },
  {
    path: 'tasks',
    canActivate: [roleGuard],
    data: { roles: ['TEAM_MANAGER', 'DEPARTMENT_HEAD', 'MEMBER'] },
    loadComponent: () =>
      import('./features/task-list/task-list-page.component').then(
        (m) => m.TaskListPageComponent,
      ),
  },
  { path: 'forgot-password', component: ForgotPasswordPageComponent },
  {
    path: 'error',
    loadComponent: () =>
      import('./features/not-found/not-found-page.component').then(
        (m) => m.NotFoundPageComponent,
      ),
  },
  {
    path: 'audit-log',
    component: AuditLogPageComponent,
    canActivate: [authGuard]
  },
  {
    path: 'ai-assistant',
    component: AiAssistantPageComponent,
    canActivate: [authGuard]
  },

  { path: '**', redirectTo: 'error' },
];