import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AdministrationPage } from './administration';

describe('AdministrationPage', () => {
  let component: AdministrationPage;
  let fixture: ComponentFixture<AdministrationPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdministrationPage],
    }).compileComponents();

    fixture = TestBed.createComponent(AdministrationPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('renders the four administration sections and the documented sources', () => {
    const content: HTMLElement = fixture.nativeElement;

    expect(content.textContent).toContain('Sources de données');
    expect(content.textContent).toContain('La Bonne Alternance');
    expect(content.textContent).toContain('Statistiques de contexte sur les volumes');
    expect(content.textContent).toContain('Comptes et rôles');
    expect(content.textContent).toContain('Collectes');
    expect(content.textContent).toContain('Journal des collectes');
  });

  it('keeps administration actions disabled while the API and session role are unavailable', () => {
    const buttons: NodeListOf<HTMLButtonElement> = fixture.nativeElement.querySelectorAll('.admin-action');

    expect(buttons.length).toBe(3);
    expect([...buttons].every((button) => button.disabled)).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('rôle de la session ne sont pas connectés');
  });

  it('shows the documented schedule and alert rules without inventing run history', () => {
    const content: HTMLElement = fixture.nativeElement;

    expect(content.textContent).toContain('chaque nuit après 03 h (Europe/Paris)');
    expect(content.textContent).toContain('Échec d’une étape');
    expect(content.textContent).toContain('Aucune collecte réussie depuis 26 h');
    expect(content.textContent).toContain('Aucun historique disponible.');
    expect(content.textContent).toContain('Aucun compte disponible dans cette vue.');
  });
});
