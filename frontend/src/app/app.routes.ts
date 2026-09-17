import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'employees' },
  {
    path: 'employees',
    loadComponent: () =>
      import('./employees/employee-list/employee-list').then((m) => m.EmployeeList),
  },
  {
    path: 'analytics',
    loadComponent: () =>
      import('./analytics/analytics-dashboard/analytics-dashboard').then(
        (m) => m.AnalyticsDashboard,
      ),
  },
  { path: '**', redirectTo: 'employees' },
];
