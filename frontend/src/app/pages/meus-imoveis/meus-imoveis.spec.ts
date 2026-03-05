import {ComponentFixture, TestBed} from '@angular/core/testing';
import {provideRouter} from '@angular/router';

import {MeusImoveis} from './meus-imoveis';
import {PropertyService} from '../../services/propertyService';
import {UserService} from '../../services/user.service';
import {Auth} from '../../auth/auth';
import {of} from 'rxjs';

describe('MeusImoveis', () => {
  let component: MeusImoveis;
  let fixture: ComponentFixture<MeusImoveis>;

  const mockPropertyService = {
    getMyProperties: () => of([]),
    publishProperty: () => of(null)
  };

  const mockUserService = {
    getOwnersReport: () => of([])
  };

  const mockAuth = {
    currentUser$: of(null)
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MeusImoveis],
      providers: [
        provideRouter([]),
        {provide: PropertyService, useValue: mockPropertyService},
        {provide: UserService, useValue: mockUserService},
        {provide: Auth, useValue: mockAuth}
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(MeusImoveis);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
