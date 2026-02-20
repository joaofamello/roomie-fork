import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Auth } from '../auth/auth';
import { Router, ActivatedRoute } from '@angular/router'; 
import { FormBuilder, FormGroup, ReactiveFormsModule, FormControl } from '@angular/forms';
import { PropertyService } from './property.service';


import { PropertyList } from '../components/property-list/property-list';

@Component({
  selector: 'app-home',
  standalone: true,

  imports: [CommonModule, ReactiveFormsModule, PropertyList],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home implements OnInit, OnDestroy {
  private auth = inject(Auth);
  private router = inject(Router);
  private route = inject(ActivatedRoute); 
  private fb = inject(FormBuilder);
  
  private propertyService = inject(PropertyService);
  private pollInterval: any;

  hasSearched: boolean = false; 
  appliedLocation: string = ''; 
  
  properties: any[] = [];
  isLoading: boolean = false;
  
  initialSearch = new FormControl(''); 

  filterForm: FormGroup = this.fb.group({
    location: [''],
    district: [''],
    minPrice: [''],
    maxPrice: [''],
    propertyType: ['']
  });

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      if (Object.keys(params).length > 0 && !this.hasSearched) {
        this.hasSearched = true;
        this.filterForm.patchValue(params, { emitEvent: false });
        this.appliedLocation = params['location'] || '';
        this.onFilter();
      }
    });

    this.pollInterval = setInterval(() => {
      if (this.hasSearched) {
        this.onFilter(true);
      }
    }, 3000);
  }

  ngOnDestroy() {
    clearInterval(this.pollInterval);
  }

  onInitialSearch() {
    if (this.initialSearch.value) {
      this.filterForm.patchValue({ location: this.initialSearch.value });
    }
    this.hasSearched = true;
    this.onFilter();
  }

  onFilter(silent = false) {
    const formValues = this.filterForm.value;

    this.appliedLocation = formValues.location;

    const cleanParams: any = {};
    Object.keys(formValues).forEach(key => {
      if (formValues[key] !== null && formValues[key] !== '') {
        cleanParams[key] = formValues[key];
      }
    });

    if (!silent) this.isLoading = true;

    this.propertyService.buscarComFiltros(cleanParams).subscribe({
      next: (resultados: any) => {
        this.properties = resultados;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Erro ao buscar imóveis:', err);
        this.isLoading = false;
      }
    });
  }


  goBackHome() {
    this.hasSearched = false;
    this.appliedLocation = ''; 
    this.initialSearch.reset();
    this.filterForm.reset();
    this.properties = []; 
    
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {}
    });
  }

  goToProfile() {
    alert('Aqui abrirá o Perfil!');
  }

  onLogout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }

  goToCreateProperty() {
    this.router.navigate(['/properties/new']);
  
  }
}