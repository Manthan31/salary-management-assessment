import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AnalyticsFilter, GroupSalaryStat, SalaryBand, SalarySummary } from './models';

/** Client for the salary analytics endpoints. */
@Injectable({ providedIn: 'root' })
export class AnalyticsService {
  private http = inject(HttpClient);
  private base = environment.apiBaseUrl;

  private toParams(filter: AnalyticsFilter): HttpParams {
    let params = new HttpParams();
    if (filter.country) params = params.set('country', filter.country);
    if (filter.department) params = params.set('department', filter.department);
    if (filter.role) params = params.set('role', filter.role);
    return params;
  }

  summary(filter: AnalyticsFilter = {}): Observable<SalarySummary> {
    return this.http.get<SalarySummary>(`${this.base}/analytics/summary`, {
      params: this.toParams(filter),
    });
  }

  byCountry(filter: AnalyticsFilter = {}): Observable<GroupSalaryStat[]> {
    return this.http.get<GroupSalaryStat[]>(`${this.base}/analytics/by-country`, {
      params: this.toParams(filter),
    });
  }

  byDepartment(filter: AnalyticsFilter = {}): Observable<GroupSalaryStat[]> {
    return this.http.get<GroupSalaryStat[]>(`${this.base}/analytics/by-department`, {
      params: this.toParams(filter),
    });
  }

  byRole(filter: AnalyticsFilter = {}): Observable<GroupSalaryStat[]> {
    return this.http.get<GroupSalaryStat[]>(`${this.base}/analytics/by-role`, {
      params: this.toParams(filter),
    });
  }

  distribution(filter: AnalyticsFilter = {}): Observable<SalaryBand[]> {
    return this.http.get<SalaryBand[]>(`${this.base}/analytics/distribution`, {
      params: this.toParams(filter),
    });
  }
}
