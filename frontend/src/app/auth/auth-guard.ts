import {inject} from '@angular/core';
import {CanActivateFn, Router} from '@angular/router';
import {Auth} from './auth';
import {UserRole} from './user.interface';

export const authGuard: CanActivateFn = (route, state) => {

  const auth = inject(Auth);
  const router = inject(Router);

  const requiredRole = route.data?.['role'] as UserRole;

  if (!auth.isAuthenticated()) {
    return router.createUrlTree(['/unauthorized']);
  }

  if (requiredRole && !auth.hasRole(requiredRole)) {
    return router.createUrlTree(['/unauthorized']);
  }

  return true;
};
