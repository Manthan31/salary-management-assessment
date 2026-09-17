import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../environments/environment';
import { authInterceptor } from './auth.interceptor';
import { EmployeeService } from './employee.service';
import { EmployeeQuery, PageResponse, EmployeeResponse } from './models';

describe('EmployeeService', () => {
  let service: EmployeeService;
  let httpMock: HttpTestingController;
  const base = environment.apiBaseUrl;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        EmployeeService,
      ],
    });
    service = TestBed.inject(EmployeeService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('builds the list request with pagination and only the provided filters', () => {
    const query: EmployeeQuery = {
      page: 2,
      size: 50,
      sort: 'lastName',
      direction: 'desc',
      search: 'smith',
      country: 'Germany',
    };
    const stub: PageResponse<EmployeeResponse> = {
      content: [],
      page: 2,
      size: 50,
      totalElements: 0,
      totalPages: 0,
    };

    service.list(query).subscribe((res) => expect(res).toEqual(stub));

    const req = httpMock.expectOne(
      (r) =>
        r.url === `${base}/employees` &&
        r.params.get('page') === '2' &&
        r.params.get('size') === '50' &&
        r.params.get('search') === 'smith' &&
        r.params.get('country') === 'Germany' &&
        r.params.get('department') === null,
    );
    expect(req.request.method).toBe('GET');
    req.flush(stub);
  });

  it('attaches the API token via the interceptor', () => {
    service.get(1).subscribe();
    const req = httpMock.expectOne(`${base}/employees/1`);
    expect(req.request.headers.get('X-API-Token')).toBe(environment.apiToken);
    req.flush({});
  });

  it('sends a PATCH to update status', () => {
    service.updateStatus(7, 'INACTIVE').subscribe();
    const req = httpMock.expectOne(`${base}/employees/7/status`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ status: 'INACTIVE' });
    req.flush({});
  });

  it('posts to create an employee', () => {
    const body = {
      firstName: 'Ada',
      lastName: 'Lovelace',
      email: 'ada@acme.example',
      countryId: 1,
      departmentId: 2,
      jobRoleId: 3,
      hireDate: '2020-01-15',
      baseSalary: 100000,
    };
    service.create(body).subscribe();
    const req = httpMock.expectOne(`${base}/employees`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush({});
  });
});
