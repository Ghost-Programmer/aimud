import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-user-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './user-dashboard.component.html',
  styleUrls: ['../../app.component.css', './user-dashboard.component.css']
})
export class UserDashboardComponent implements OnInit {

  username = 'Operator';
  roles: string[] = [];
  isAdmin = false;
  gravatarUrl = '';

  constructor(private authService: AuthService, private router: Router) { }

  ngOnInit(): void {
    if (this.authService.hasToken()) {
      this.username = this.authService.getUsername() || 'Unknown Operator';
      this.roles = this.authService.getRoles();
      this.isAdmin = this.authService.isAdmin();

      // Basic MD5 hashing for gravatar - this requires a library or native crypto API. 
      // For now, generating a placeholder gravatar from robohash for visual fidelity
      const userHash = btoa(this.username).substring(0, 10);
      this.gravatarUrl = `https://robohash.org/${userHash}?set=set3&bgset=&size=200x200`;
    }
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/']);
  }

  changePassword() {
    alert('Change Password Stub: This will open a modal or navigate to a specialized component in the future.');
  }

  adminPanel() {
    alert('Admin Panel Stub: Only visible to ADMIN users.');
  }
}
