import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { App } from './app';

describe('App shell', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter([])],
    }).compileComponents();
  });

  it('creates the app', () => {
    const fixture = TestBed.createComponent(App);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('renders the toolbar title and navigation links', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;

    expect(compiled.querySelector('.app-title')?.textContent).toContain('Salary Management');

    const links = Array.from(compiled.querySelectorAll('.app-nav a')).map(
      (a) => a.textContent?.trim(),
    );
    expect(links.some((t) => t?.includes('Employees'))).toBeTrue();
    expect(links.some((t) => t?.includes('Analytics'))).toBeTrue();
  });
});
