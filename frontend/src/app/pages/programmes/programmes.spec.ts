import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProgrammesPage } from './programmes';

describe('ProgrammesPage', () => {
  let component: ProgrammesPage;
  let fixture: ComponentFixture<ProgrammesPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProgrammesPage],
    }).compileComponents();

    fixture = TestBed.createComponent(ProgrammesPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('presents the official structure counts without inventing UE or module names', () => {
    const content: HTMLElement = fixture.nativeElement;

    expect(content.textContent).toContain('7 UE · 23 matières prévues à l’import initial');
    expect(content.textContent).toContain('Aucune structure de programme disponible');
    expect(content.querySelectorAll('.ue-item').length).toBe(0);
    expect(content.textContent).not.toContain('UE 01');
  });

  it('keeps role-scoped actions unavailable until roles and programme data are connected', () => {
    const actions: NodeListOf<HTMLButtonElement> = fixture.nativeElement.querySelectorAll('.role-actions button');

    expect(actions.length).toBe(2);
    expect([...actions].every((action) => action.disabled)).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('Administrateur');
    expect(fixture.nativeElement.textContent).toContain('Responsable de formation');
  });

  it('shows explicit empty states for module competencies and correction history', () => {
    const content: HTMLElement = fixture.nativeElement;

    expect(content.textContent).toContain('Aucune compétence à afficher.');
    expect(content.textContent).toContain('Aucune correction à afficher.');
    expect(content.textContent).toContain('Non abordée');
    expect(content.textContent).toContain('Mise en pratique');
  });
});
