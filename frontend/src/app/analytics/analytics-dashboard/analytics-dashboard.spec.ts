import { TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { of } from 'rxjs';
import { AnalyticsService } from '../../core/analytics.service';
import { EmployeeService } from '../../core/employee.service';
import { GroupSalaryStat, SalaryBand, SalarySummary } from '../../core/models';
import { AnalyticsDashboard } from './analytics-dashboard';

describe('AnalyticsDashboard', () => {
  const summary: SalarySummary = {
    headcount: 9178,
    baseCurrency: 'USD',
    totalSalary: 1_100_000_000,
    averageSalary: 119924,
    medianSalary: 120098,
    minSalary: 40008,
    maxSalary: 199999,
  };
  const distribution: SalaryBand[] = [
    { label: '$0k - $50k', minInclusive: 0, maxExclusive: 50000, headcount: 500 },
    { label: '$200k+', minInclusive: 200000, maxExclusive: null, headcount: 100 },
  ];
  const byCountry: GroupSalaryStat[] = [
    { group: 'Germany', headcount: 1009, averageSalary: 120000, medianSalary: 118000, minSalary: 45000, maxSalary: 210000 },
  ];

  let analytics: jasmine.SpyObj<AnalyticsService>;
  let employees: jasmine.SpyObj<EmployeeService>;

  beforeEach(async () => {
    analytics = jasmine.createSpyObj<AnalyticsService>('AnalyticsService', [
      'summary',
      'distribution',
      'byCountry',
      'byDepartment',
      'byRole',
    ]);
    employees = jasmine.createSpyObj<EmployeeService>('EmployeeService', [
      'countries',
      'departments',
      'roles',
    ]);

    analytics.summary.and.returnValue(of(summary));
    analytics.distribution.and.returnValue(of(distribution));
    analytics.byCountry.and.returnValue(of(byCountry));
    analytics.byDepartment.and.returnValue(of([]));
    analytics.byRole.and.returnValue(of([]));
    employees.countries.and.returnValue(of([]));
    employees.departments.and.returnValue(of([]));
    employees.roles.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [AnalyticsDashboard],
      providers: [
        provideNoopAnimations(),
        { provide: AnalyticsService, useValue: analytics },
        { provide: EmployeeService, useValue: employees },
      ],
    }).compileComponents();
  });

  it('loads summary, distribution and grouped stats on init', () => {
    const fixture = TestBed.createComponent(AnalyticsDashboard);
    fixture.detectChanges();
    const component = fixture.componentInstance;

    expect(analytics.summary).toHaveBeenCalled();
    expect(analytics.distribution).toHaveBeenCalled();
    expect(analytics.byCountry).toHaveBeenCalled();
    expect(component.summary()?.headcount).toBe(9178);
    expect(component.distribution().length).toBe(2);
    expect(component.groupStats().length).toBe(1);
  });

  it('renders the summary headcount in the template', () => {
    const fixture = TestBed.createComponent(AnalyticsDashboard);
    fixture.detectChanges();
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('9,178');
  });

  it('computes band width as a percentage of the largest band', () => {
    const fixture = TestBed.createComponent(AnalyticsDashboard);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    // Largest band is 500, so the 100-count band is 20%.
    expect(component.bandPercent(distribution[1])).toBe(20);
    expect(component.bandPercent(distribution[0])).toBe(100);
  });

  it('reloads grouped stats when the dimension changes', () => {
    const fixture = TestBed.createComponent(AnalyticsDashboard);
    fixture.detectChanges();
    const component = fixture.componentInstance;

    component.onDimensionChange('department');
    expect(analytics.byDepartment).toHaveBeenCalled();
    expect(component.dimension()).toBe('department');
  });
});
