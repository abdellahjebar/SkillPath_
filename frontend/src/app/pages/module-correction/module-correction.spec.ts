import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ModuleCorrectionPage } from './module-correction';

describe('ModuleCorrectionPage', () => {
  let component: ModuleCorrectionPage;
  let fixture: ComponentFixture<ModuleCorrectionPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModuleCorrectionPage],
    }).compileComponents();

    fixture = TestBed.createComponent(ModuleCorrectionPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
