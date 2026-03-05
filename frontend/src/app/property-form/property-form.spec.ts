import {ComponentFixture, TestBed} from '@angular/core/testing';
import {PropertyFormComponent} from './property-form';
import {RouterTestingModule} from '@angular/router/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {ActivatedRoute} from '@angular/router';

describe('PropertyFormComponent', () => {
  let component: PropertyFormComponent;
  let fixture: ComponentFixture<PropertyFormComponent>;
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PropertyFormComponent, RouterTestingModule, HttpClientTestingModule],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {snapshot: {paramMap: {get: () => null}}}
        }
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(PropertyFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
