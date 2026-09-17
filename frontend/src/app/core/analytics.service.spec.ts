import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../environments/environment';
import { AnalyticsService } from './analytics.service';
import { SalarySummary } from './models';

describe('AnalyticsService', () => {
  let service: AnalyticsService;
  let httpMock: HttpTestingController;
  const base = environment.apiBaseUrl;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), AnalyticsService],
    });
    service = TestBed.inject(AnalyticsService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('requests the summary without params when no filter is given', () => {
    const stub: SalarySummary = {
      headcount: 100,
      baseCurrency: 'USD',
      totalSalary: 1000,
      averageSalary: 10,
      medianSalary: 10,
      minSalary: 1,
      maxSalary: 20,
    };
    service.summary().subscribe((res) => expect(res).toEqual(stub));

    const req = httpMock.expectOne(`${base}/analytics/summary`);
    expect(req.request.params.keys().length).toBe(0);
    req.flush(stub);
  });

  it('passes only the filters that are set', () => {
    service.byDepartment({ department: 'Engineering' }).subscribe();
    const req = httpMock.expectOne((r) => r.url === `${base}/analytics/by-department`);
    expect(req.request.params.get('department')).toBe('Engineering');
    expect(req.request.params.get('country')).toBeNull();
    req.flush([]);
  });

  it('hits the distribution endpoint', () => {
    service.distribution().subscribe();
    const req = httpMock.expectOne(`${base}/analytics/distribution`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });
});
