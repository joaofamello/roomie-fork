import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Home } from './home';
import { Router } from '@angular/router';
import { Auth } from '../auth/auth';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { By } from '@angular/platform-browser';

describe('Home', () => {
  let component: Home;
  let fixture: ComponentFixture<Home>;

  let mockRouter = { navigate: () => {} };
  let mockAuth = { logout: () => {} };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Home, HttpClientTestingModule, ReactiveFormsModule],
      providers: [
        { provide: Router, useValue: mockRouter },
        { provide: Auth, useValue: mockAuth }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Home);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create component Home', () => {
    expect(component).toBeTruthy();
  });

  it('should render the header with the "Anunciar Imóvel" button', () => {
    const anunciarBtn = fixture.debugElement.query(By.css('.btn-create-property'));
    expect(anunciarBtn).toBeTruthy();
    expect(anunciarBtn.nativeElement.textContent).toContain('Anunciar Imóvel');
  });

  it('should render the logout button', () => {
    const sairBtn = fixture.debugElement.query(By.css('.logout-btn'));
    expect(sairBtn).toBeTruthy();
    expect(sairBtn.nativeElement.textContent).toContain('Sair');
  });
});