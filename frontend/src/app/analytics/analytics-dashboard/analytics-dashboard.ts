import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { forkJoin } from 'rxjs';
import { AnalyticsService } from '../../core/analytics.service';
import { EmployeeService } from '../../core/employee.service';
import {
  AnalyticsFilter,
  CountryDto,
  GroupSalaryStat,
  NamedDto,
  SalaryBand,
  SalarySummary,
} from '../../core/models';

type GroupDimension = 'country' | 'department' | 'role';

/**
 * Compensation analytics dashboard: org-wide summary cards, salary distribution
 * bands, and grouped pay statistics by country / department / role. All values
 * are USD-normalized by the backend. Filters narrow the population.
 */
@Component({
  selector: 'app-analytics-dashboard',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatTableModule,
    MatFormFieldModule,
    MatSelectModule,
    MatButtonModule,
    MatButtonToggleModule,
    MatIconModule,
    MatProgressBarModule,
  ],
  templateUrl: './analytics-dashboard.html',
  styleUrl: './analytics-dashboard.scss',
})
export class AnalyticsDashboard implements OnInit {
  private analyticsService = inject(AnalyticsService);
  private employeeService = inject(EmployeeService);

  readonly summary = signal<SalarySummary | null>(null);
  readonly distribution = signal<SalaryBand[]>([]);
  readonly groupStats = signal<GroupSalaryStat[]>([]);
  readonly loading = signal(false);

  readonly dimension = signal<GroupDimension>('country');

  readonly countries = signal<CountryDto[]>([]);
  readonly departments = signal<NamedDto[]>([]);
  readonly roles = signal<NamedDto[]>([]);

  readonly countryControl = new FormControl('');
  readonly departmentControl = new FormControl('');
  readonly roleControl = new FormControl('');

  readonly groupColumns = ['group', 'headcount', 'averageSalary', 'medianSalary', 'minSalary', 'maxSalary'];
  readonly maxBandCount = signal(1);

  ngOnInit(): void {
    this.employeeService.countries().subscribe((c) => this.countries.set(c));
    this.employeeService.departments().subscribe((d) => this.departments.set(d));
    this.employeeService.roles().subscribe((r) => this.roles.set(r));
    this.reload();
  }

  private currentFilter(): AnalyticsFilter {
    return {
      country: this.countryControl.value || undefined,
      department: this.departmentControl.value || undefined,
      role: this.roleControl.value || undefined,
    };
  }

  reload(): void {
    this.loading.set(true);
    const filter = this.currentFilter();
    forkJoin({
      summary: this.analyticsService.summary(filter),
      distribution: this.analyticsService.distribution(filter),
      groups: this.loadGroup(filter),
    }).subscribe({
      next: ({ summary, distribution, groups }) => {
        this.summary.set(summary);
        this.distribution.set(distribution);
        this.maxBandCount.set(Math.max(1, ...distribution.map((b) => b.headcount)));
        this.groupStats.set(groups);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  private loadGroup(filter: AnalyticsFilter) {
    switch (this.dimension()) {
      case 'department':
        return this.analyticsService.byDepartment(filter);
      case 'role':
        return this.analyticsService.byRole(filter);
      case 'country':
      default:
        return this.analyticsService.byCountry(filter);
    }
  }

  onDimensionChange(dim: GroupDimension): void {
    this.dimension.set(dim);
    this.loadGroup(this.currentFilter()).subscribe((groups) => this.groupStats.set(groups));
  }

  onFilterChange(): void {
    this.reload();
  }

  clearFilters(): void {
    this.countryControl.setValue('');
    this.departmentControl.setValue('');
    this.roleControl.setValue('');
    this.reload();
  }

  bandPercent(band: SalaryBand): number {
    return Math.round((band.headcount / this.maxBandCount()) * 100);
  }
}
