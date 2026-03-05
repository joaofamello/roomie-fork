import {ComponentFixture, TestBed} from '@angular/core/testing';
import {provideRouter} from '@angular/router';

import {PropertyList} from './property-list';

describe('PropertyList', () => {
  let component: PropertyList;
  let fixture: ComponentFixture<PropertyList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PropertyList],
      providers: [provideRouter([])]
    })
      .compileComponents();

    fixture = TestBed.createComponent(PropertyList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
