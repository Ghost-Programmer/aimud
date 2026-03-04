import { Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { LoginComponent } from './auth/login/login.component';
import { RegisterComponent } from './auth/register/register.component';
import { UserDashboardComponent } from './user/user-dashboard/user-dashboard.component';
import { authGuard } from './auth/auth.guard';

export const routes: Routes = [
    { path: '', component: HomeComponent },
    { path: 'login', component: LoginComponent },
    { path: 'register', component: RegisterComponent },
    { path: 'user', component: UserDashboardComponent, canActivate: [authGuard] },
    { path: '**', redirectTo: '' }
];
