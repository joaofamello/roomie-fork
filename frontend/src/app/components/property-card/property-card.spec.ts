import {ComponentFixture, TestBed} from '@angular/core/testing';
import {provideRouter} from '@angular/router';

import {PropertyCard} from './property-card';

describe('PropertyCard', () => {
  let component: PropertyCard;
  let fixture: ComponentFixture<PropertyCard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PropertyCard],
      providers: [provideRouter([])]
    })
      .compileComponents();

    fixture = TestBed.createComponent(PropertyCard);
    component = fixture.componentInstance;
    component.property = {
      id: 1,
      title: 'Test Property',
      price: 500,
      photos: [],
    } as any;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
