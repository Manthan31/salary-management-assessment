import { Component, Inject, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { EmployeeService } from '../../core/employee.service';
import { CountryDto, EmployeeRequest, EmployeeResponse, NamedDto } from '../../core/models';

interface DialogData {
  mode: 'create' | 'edit';
  employee?: EmployeeResponse;
}

/**
 * Create/edit employee form dialog. Validation mirrors the backend request
 * DTO (required fields, valid email, positive salary).
 */
@Component({
  selector: 'app-employee-edit-dialog',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
  ],
  templateUrl: './employee-edit-dialog.html',
  styleUrl: './employee-edit-dialog.scss',
})
export class EmployeeEditDialog implements OnInit {
  private fb = inject(FormBuilder);
  private employeeService = inject(EmployeeService);
  private dialogRef = inject(MatDialogRef<EmployeeEditDialog>);
  private snackBar = inject(MatSnackBar);

  readonly countries = signal<CountryDto[]>([]);
  readonly departments = signal<NamedDto[]>([]);
  readonly roles = signal<NamedDto[]>([]);
  readonly saving = signal(false);

  readonly isEdit: boolean;

  readonly form = this.fb.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    countryId: [null as number | null, Validators.required],
    departmentId: [null as number | null, Validators.required],
    jobRoleId: [null as number | null, Validators.required],
    hireDate: ['', Validators.required],
    baseSalary: [null as number | null, [Validators.required, Validators.min(0.01)]],
  });

  constructor(@Inject(MAT_DIALOG_DATA) public data: DialogData) {
    this.isEdit = data.mode === 'edit';
  }

  ngOnInit(): void {
    this.employeeService.countries().subscribe((c) => {
      this.countries.set(c);
      this.prefillIfEditing(c);
    });
    this.employeeService.departments().subscribe((d) => this.departments.set(d));
    this.employeeService.roles().subscribe((r) => this.roles.set(r));
  }

  /**
   * When editing, prefill the form. Reference selects use ids, so we resolve
   * the employee's country/department/role names back to their ids.
   */
  private prefillIfEditing(countries: CountryDto[]): void {
    if (!this.isEdit || !this.data.employee) {
      return;
    }
    const e = this.data.employee;
    this.form.patchValue({
      firstName: e.firstName,
      lastName: e.lastName,
      email: e.email,
      hireDate: e.hireDate,
      baseSalary: e.baseSalary,
    });
    // Resolve country id from name; department/role ids resolved once loaded.
    const country = countries.find((c) => c.name === e.country);
    if (country) this.form.patchValue({ countryId: country.id });
    this.employeeService.departments().subscribe((deps) => {
      const dep = deps.find((d) => d.name === e.department);
      if (dep) this.form.patchValue({ departmentId: dep.id });
    });
    this.employeeService.roles().subscribe((roles) => {
      const role = roles.find((r) => r.name === e.jobRole);
      if (role) this.form.patchValue({ jobRoleId: role.id });
    });
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    const raw = this.form.getRawValue();
    const body: EmployeeRequest = {
      firstName: raw.firstName!,
      lastName: raw.lastName!,
      email: raw.email!,
      countryId: raw.countryId!,
      departmentId: raw.departmentId!,
      jobRoleId: raw.jobRoleId!,
      hireDate: raw.hireDate!,
      baseSalary: raw.baseSalary!,
    };

    const request$ =
      this.isEdit && this.data.employee
        ? this.employeeService.update(this.data.employee.id, body)
        : this.employeeService.create(body);

    request$.subscribe({
      next: () => {
        this.saving.set(false);
        this.dialogRef.close(true);
      },
      error: (err) => {
        this.saving.set(false);
        const message = err?.error?.message ?? 'Failed to save employee';
        this.snackBar.open(message, 'Dismiss', { duration: 5000 });
      },
    });
  }

  cancel(): void {
    this.dialogRef.close(false);
  }
}
