// TypeScript models mirroring the backend API DTOs.

export interface EmployeeResponse {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  country: string;
  department: string;
  jobRole: string;
  hireDate: string;
  status: 'ACTIVE' | 'INACTIVE';
  baseSalary: number;
  currencyCode: string;
  salaryInBaseCurrency: number;
}

export interface EmployeeRequest {
  firstName: string;
  lastName: string;
  email: string;
  countryId: number;
  departmentId: number;
  jobRoleId: number;
  hireDate: string;
  baseSalary: number;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface EmployeeQuery {
  page: number;
  size: number;
  sort?: string;
  direction?: 'asc' | 'desc';
  search?: string;
  country?: string;
  department?: string;
  role?: string;
  status?: 'ACTIVE' | 'INACTIVE';
}

export interface SalarySummary {
  headcount: number;
  baseCurrency: string;
  totalSalary: number;
  averageSalary: number;
  medianSalary: number;
  minSalary: number;
  maxSalary: number;
}

export interface GroupSalaryStat {
  group: string;
  headcount: number;
  averageSalary: number;
  medianSalary: number;
  minSalary: number;
  maxSalary: number;
}

export interface SalaryBand {
  label: string;
  minInclusive: number;
  maxExclusive: number | null;
  headcount: number;
}

export interface CountryDto {
  id: number;
  name: string;
  isoCode: string;
  currencyCode: string;
}

export interface NamedDto {
  id: number;
  name: string;
}

export interface AnalyticsFilter {
  country?: string;
  department?: string;
  role?: string;
}
