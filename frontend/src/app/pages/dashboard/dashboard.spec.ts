import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DashboardPage } from './dashboard';

describe('DashboardPage', () => {
  let component: DashboardPage;
  let fixture: ComponentFixture<DashboardPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DashboardPage],
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('keeps the requested segment values and lets the user select a month', () => {
    expect(component.selectedDomain()).toBe('M18');
    expect(component.selectedLocation()).toBe('France');

    const monthInput: HTMLInputElement = fixture.nativeElement.querySelector('input[type="month"]');
    monthInput.value = '2026-01';
    monthInput.dispatchEvent(new Event('change'));
    fixture.detectChanges();

    expect(component.selectedPeriod()).toBe('2026-01');
  });

  it('shows the Wilson method tooltip and an honest empty state', () => {
    const tooltip: HTMLElement | null = fixture.nativeElement.querySelector('#wilson-tooltip');
    const emptyState: HTMLElement | null = fixture.nativeElement.querySelector('.chart-empty');
    const tableBody: HTMLElement | null = fixture.nativeElement.querySelector('.table-empty');

    expect(tooltip?.textContent).toContain('méthode de Wilson');
    expect(emptyState?.textContent).toContain('Aucune donnée à afficher');
    expect(tableBody?.textContent).toContain('Aucun résultat disponible');
  });

  it('applies the sample-size thresholds only when the real count is available', () => {
    expect(component.sampleSizeStatus()).toBeNull();
    expect(fixture.nativeElement.querySelector('.sample-warning')).toBeNull();

    component.effectif.set(38);
    fixture.detectChanges();
    expect(component.sampleSizeStatus()).toBe('insufficient');
    expect(fixture.nativeElement.querySelector('.sample-warning')?.textContent).toContain('Effectif insuffisant');
    expect(fixture.nativeElement.querySelector('.indicator-card.demand-card .indicator-value')?.textContent.trim()).toBe('—');

    component.effectif.set(39);
    fixture.detectChanges();
    expect(component.sampleSizeStatus()).toBe('caution');
    expect(fixture.nativeElement.querySelector('.sample-warning')?.textContent).toContain('À interpréter avec prudence');

    component.effectif.set(92);
    fixture.detectChanges();
    expect(component.sampleSizeStatus()).toBe('caution');

    component.effectif.set(93);
    fixture.detectChanges();
    expect(component.sampleSizeStatus()).toBeNull();
    expect(fixture.nativeElement.querySelector('.sample-warning')).toBeNull();
    expect(fixture.nativeElement.querySelector('.indicator-card.count-card .indicator-value')?.textContent.trim()).toBe('93');
  });
});
