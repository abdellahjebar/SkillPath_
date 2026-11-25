import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CompetenceDetailPage } from './competence-detail';

describe('CompetenceDetailPage', () => {
  let component: CompetenceDetailPage;
  let fixture: ComponentFixture<CompetenceDetailPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CompetenceDetailPage],
    }).compileComponents();

    fixture = TestBed.createComponent(CompetenceDetailPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
