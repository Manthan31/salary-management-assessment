import { HttpInterceptorFn } from '@angular/common/http';
import { environment } from '../../environments/environment';

/**
 * Attaches the static API token to every request to the backend so the HR user
 * is authenticated. Matches the backend's ApiTokenFilter (X-API-Token header).
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  if (req.url.startsWith(environment.apiBaseUrl)) {
    const authReq = req.clone({
      setHeaders: { 'X-API-Token': environment.apiToken },
    });
    return next(authReq);
  }
  return next(req);
};
