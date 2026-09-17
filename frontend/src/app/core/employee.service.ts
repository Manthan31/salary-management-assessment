import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  CountryDto,
  EmployeeQuery,
  EmployeeRequest,
  EmployeeResponse,
  NamedDto,
  PageResponse,
} from './models';

/** Client for the employee and reference (meta) endpoints. */
@Injectable({ providedIn: 'root' })
export class EmployeeService {
  private http = inject(HttpClient);
  private base = environment.apiBaseUrl;

  list(query: EmployeeQuery): Observable<PageResponse<EmployeeResponse>> {
    let params = new HttpParams()
      .set('page', query.page)
      .set('size', query.size);
    if (query.sort) params = params.set('sort', query.sort);
    if (query.direction) params = params.set('direction', query.direction);
    if (query.search) params = params.set('search', query.search);
    if (query.country) params = params.set('country', query.country);
    if (query.department) params = params.set('department', query.department);
    if (query.role) params = params.set('role', query.role);
    if (query.status) params = params.set('status', query.status);
    return this.http.get<PageResponse<EmployeeResponse>>(`${this.base}/employees`, { params });
  }

  get(id: number): Observable<EmployeeResponse> {
    return this.http.get<EmployeeResponse>(`${this.base}/employees/${id}`);
  }

  create(body: EmployeeRequest): Observable<EmployeeResponse> {
    return this.http.post<EmployeeResponse>(`${this.base}/employees`, body);
  }

  update(id: number, body: EmployeeRequest): Observable<EmployeeResponse> {
    return this.http.put<EmployeeResponse>(`${this.base}/employees/${id}`, body);
  }

  updateStatus(id: number, status: 'ACTIVE' | 'INACTIVE'): Observable<EmployeeResponse> {
    return this.http.patch<EmployeeResponse>(`${this.base}/employees/${id}/status`, { status });
  }

  countries(): Observable<CountryDto[]> {
    return this.http.get<CountryDto[]>(`${this.base}/meta/countries`);
  }

  departments(): Observable<NamedDto[]> {
    return this.http.get<NamedDto[]>(`${this.base}/meta/departments`);
  }

  roles(): Observable<NamedDto[]> {
    return this.http.get<NamedDto[]>(`${this.base}/meta/roles`);
  }
}
