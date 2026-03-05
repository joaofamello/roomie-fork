import {ComponentFixture, TestBed} from '@angular/core/testing';
import {Home} from './home';
import {provideRouter} from '@angular/router';
import {Auth} from '../auth/auth';
import {ReactiveFormsModule} from '@angular/forms';
import {By} from '@angular/platform-browser';
import {PropertyService} from './property.service';
import {of} from 'rxjs';

describe('Home', () => {
  let component: Home;
  let fixture: ComponentFixture<Home>;

  const mockUser = {id: 1, name: 'Test User', email: 'test@test.com', role: 'USER' as const};
  const mockAuth = {
    logout: () => {
    },
    currentUser$: of(mockUser)
  };
  const mockPropertyService = {buscarComFiltros: () => of([])};

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Home, ReactiveFormsModule],
      providers: [
        provideRouter([]),
        {provide: Auth, useValue: mockAuth},
        {provide: PropertyService, useValue: mockPropertyService}
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(Home);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create component Home', () => {
    expect(component).toBeTruthy();
  });

  it('should render the header component', () => {
    const header = fixture.debugElement.query(By.css('app-header'));
    expect(header).toBeTruthy();
  });

  it('should render the "Cadastrar Imovel" button in the header', async () => {
    fixture.detectChanges();
    await fixture.whenStable();
    const btn = fixture.debugElement.query(By.css('.outline-btn'));
    expect(btn).toBeTruthy();
    expect(btn.nativeElement.textContent).toContain('Cadastrar');
  });
});
