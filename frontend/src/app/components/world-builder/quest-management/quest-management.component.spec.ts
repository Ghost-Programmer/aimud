import { ComponentFixture, TestBed } from '@angular/core/testing';

import { QuestManagementComponent } from './quest-management.component';

describe('QuestManagementComponent', () => {
  let component: QuestManagementComponent;
  let fixture: ComponentFixture<QuestManagementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [QuestManagementComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(QuestManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
