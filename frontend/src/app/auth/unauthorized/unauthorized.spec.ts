import {ComponentFixture, TestBed} from '@angular/core/testing';
import {provideRouter, Router} from '@angular/router';
import {Unauthorized} from './unauthorized';

describe('Unauthorized', () => {
  let component: Unauthorized;
  let fixture: ComponentFixture<Unauthorized>;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Unauthorized],
      providers: [provideRouter([{path: 'login', component: Unauthorized}])],
    })
      .compileComponents();

    fixture = TestBed.createComponent(Unauthorized);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should navigate to login when goBack is called', () => {
    const navigateSpy = jest.spyOn(router, 'navigate').mockResolvedValue(true);
    component.goBack();
    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  });

  it('should display unauthorized message', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const heading = compiled.querySelector('h1');
    expect(heading).toBeTruthy();
  });
});

