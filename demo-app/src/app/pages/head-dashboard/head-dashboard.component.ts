import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-head-dashboard',
  standalone: true,
  imports: [MatButtonModule, RouterLink],
  templateUrl: './head-dashboard.component.html',
  styleUrl: './head-dashboard.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HeadDashboardComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly user = this.authService.currentUser;

  protected logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}