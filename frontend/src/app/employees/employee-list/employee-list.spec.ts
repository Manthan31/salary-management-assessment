import { TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { of } from 'rxjs';
import { EmployeeService } from '../../core/employee.service';
import { EmployeeResponse, PageResponse } from '../../core/models';
import { EmployeeList } from './employee-list';

function employee(overrides: Partial<EmployeeResponse> = {}): EmployeeResponse {
  return {
    id: 1,
    firstName: 'Ada',
    lastName: 'Lovelace',
    email: 'ada@acme.example',
    country: 'United States',
    department: 'Engineering',
    jobRole: 'Software Engineer',
    hireDate: '2020-01-15',
    status: 'ACTIVE',
    baseSalary: 100000,
    currencyCode: 'USD',
    salaryInBaseCurrency: 100000,
    ...overrides,
  };
}

describe('EmployeeList', () => {
  let employees: jasmine.SpyObj<EmployeeService>;
  let snackBar: jasmine.SpyObj<MatSnackBar>;
  let dialog: jasmine.SpyObj<MatDialog>;

  const page: PageResponse<EmployeeResponse> = {
    content: [employee(), employee({ id: 2, firstName: 'Grace', lastName: 'Hopper' })],
    page: 0,
    size: 20,
    totalElements: 2,
    totalPages: 1,
  };

  beforeEach(async () => {
    employees = jasmine.createSpyObj<EmployeeService>('EmployeeService', [
      'list',
      'countries',
      'departments',
      'roles',
      'updateStatus',
    ]);
    snackBar = jasmine.createSpyObj<MatSnackBar>('MatSnackBar', ['open']);
    dialog = jasmine.createSpyObj<MatDialog>('MatDialog', ['open']);

    employees.list.and.returnValue(of(page));
    employees.countries.and.returnValue(of([]));
    employees.departments.and.returnValue(of([]));
    employees.roles.and.returnValue(of([]));
    employees.updateStatus.and.returnValue(of(employee({ status: 'INACTIVE' })));

    await TestBed.configureTestingModule({
      imports: [EmployeeList],
      providers: [
        provideNoopAnimations(),
        { provide: EmployeeService, useValue: employees },
        { provide: MatSnackBar, useValue: snackBar },
        { provide: MatDialog, useValue: dialog },
      ],
    }).compileComponents();
  });

  it('loads employees and total count on init', () => {
    const fixture = TestBed.createComponent(EmployeeList);
    fixture.detectChanges();
    const component = fixture.componentInstance;

    expect(employees.list).toHaveBeenCalled();
    expect(component.employees().length).toBe(2);
    expect(component.totalElements()).toBe(2);
    expect(component.loading()).toBeFalse();
  });

  it('requests a new page when the paginator changes', () => {
    const fixture = TestBed.createComponent(EmployeeList);
    fixture.detectChanges();
    const component = fixture.componentInstance;

    employees.list.calls.reset();
    component.onPage({ pageIndex: 3, pageSize: 50, length: 100 });

    const query = employees.list.calls.mostRecent().args[0];
    expect(query.page).toBe(3);
    expect(query.size).toBe(50);
  });

  it('toggles status via the service and reloads', () => {
    const fixture = TestBed.createComponent(EmployeeList);
    fixture.detectChanges();
    const component = fixture.componentInstance;

    component.toggleStatus(employee({ id: 5, status: 'ACTIVE' }));
    expect(employees.updateStatus).toHaveBeenCalledWith(5, 'INACTIVE');
  });

  it('resets the query when filters are cleared', () => {
    const fixture = TestBed.createComponent(EmployeeList);
    fixture.detectChanges();
    const component = fixture.componentInstance;

    component.countryControl.setValue('Germany');
    employees.list.calls.reset();
    component.clearFilters();

    const query = employees.list.calls.mostRecent().args[0];
    expect(query.country).toBeUndefined();
    expect(component.countryControl.value).toBe('');
  });
});
