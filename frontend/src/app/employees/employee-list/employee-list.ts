import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { EmployeeService } from '../../core/employee.service';
import { CountryDto, EmployeeQuery, EmployeeResponse, NamedDto } from '../../core/models';
import { EmployeeEditDialog } from '../employee-edit-dialog/employee-edit-dialog';

/**
 * Employee directory: a paginated, filterable, sortable Material table. All
 * paging/filtering/sorting is delegated to the backend so it stays responsive
 * across 10,000 employees.
 */
@Component({
  selector: 'app-employee-list',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatProgressBarModule,
    MatDialogModule,
    MatTooltipModule,
  ],
  templateUrl: './employee-list.html',
  styleUrl: './employee-list.scss',
})
export class EmployeeList implements OnInit {
  private employeeService = inject(EmployeeService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  readonly displayedColumns = [
    'name',
    'email',
    'country',
    'department',
    'jobRole',
    'baseSalary',
    'salaryInBaseCurrency',
    'status',
    'actions',
  ];

  readonly employees = signal<EmployeeResponse[]>([]);
  readonly totalElements = signal(0);
  readonly loading = signal(false);

  readonly countries = signal<CountryDto[]>([]);
  readonly departments = signal<NamedDto[]>([]);
  readonly roles = signal<NamedDto[]>([]);

  readonly searchControl = new FormControl('');
  readonly countryControl = new FormControl('');
  readonly departmentControl = new FormControl('');
  readonly roleControl = new FormControl('');
  readonly statusControl = new FormControl('');

  private query: EmployeeQuery = {
    page: 0,
    size: 20,
    sort: 'lastName',
    direction: 'asc',
  };

  ngOnInit(): void {
    this.loadReferenceData();
    this.load();

    this.searchControl.valueChanges
      .pipe(debounceTime(350), distinctUntilChanged())
      .subscribe((value) => {
        this.query = { ...this.query, page: 0, search: value ?? undefined };
        this.load();
      });
  }

  private loadReferenceData(): void {
    this.employeeService.countries().subscribe((c) => this.countries.set(c));
    this.employeeService.departments().subscribe((d) => this.departments.set(d));
    this.employeeService.roles().subscribe((r) => this.roles.set(r));
  }

  load(): void {
    this.loading.set(true);
    this.employeeService.list(this.query).subscribe({
      next: (page) => {
        this.employees.set(page.content);
        this.totalElements.set(page.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.snackBar.open('Failed to load employees', 'Dismiss', { duration: 4000 });
        this.loading.set(false);
      },
    });
  }

  onFilterChange(): void {
    this.query = {
      ...this.query,
      page: 0,
      country: this.countryControl.value || undefined,
      department: this.departmentControl.value || undefined,
      role: this.roleControl.value || undefined,
      status: (this.statusControl.value as 'ACTIVE' | 'INACTIVE') || undefined,
    };
    this.load();
  }

  clearFilters(): void {
    this.searchControl.setValue('');
    this.countryControl.setValue('');
    this.departmentControl.setValue('');
    this.roleControl.setValue('');
    this.statusControl.setValue('');
    this.query = { page: 0, size: this.query.size, sort: 'lastName', direction: 'asc' };
    this.load();
  }

  onPage(event: PageEvent): void {
    this.query = { ...this.query, page: event.pageIndex, size: event.pageSize };
    this.load();
  }

  onSort(sort: Sort): void {
    if (!sort.direction) {
      this.query = { ...this.query, sort: 'lastName', direction: 'asc' };
    } else {
      const field = sort.active === 'name' ? 'lastName' : sort.active;
      this.query = { ...this.query, sort: field, direction: sort.direction as 'asc' | 'desc' };
    }
    this.load();
  }

  openCreate(): void {
    const ref = this.dialog.open(EmployeeEditDialog, {
      width: '520px',
      data: { mode: 'create' },
    });
    ref.afterClosed().subscribe((saved) => {
      if (saved) {
        this.snackBar.open('Employee created', 'OK', { duration: 3000 });
        this.load();
      }
    });
  }

  openEdit(employee: EmployeeResponse): void {
    const ref = this.dialog.open(EmployeeEditDialog, {
      width: '520px',
      data: { mode: 'edit', employee },
    });
    ref.afterClosed().subscribe((saved) => {
      if (saved) {
        this.snackBar.open('Employee updated', 'OK', { duration: 3000 });
        this.load();
      }
    });
  }

  toggleStatus(employee: EmployeeResponse): void {
    const next = employee.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    this.employeeService.updateStatus(employee.id, next).subscribe({
      next: () => {
        this.snackBar.open(`Employee set to ${next}`, 'OK', { duration: 3000 });
        this.load();
      },
      error: () =>
        this.snackBar.open('Failed to update status', 'Dismiss', { duration: 4000 }),
    });
  }
}
