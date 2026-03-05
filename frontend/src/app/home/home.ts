import {ChangeDetectorRef, Component, inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {FormBuilder, FormControl, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {PropertyService} from './property.service';
import {PropertyList} from '../components/property-list/property-list';
import {HeaderComponent} from '../components/shared/header/header.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, PropertyList, HeaderComponent],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home implements OnInit {
  hasSearched: boolean = false;
  appliedLocation: string = '';
  properties: any[] = [];
  isLoading: boolean = false;
  initialSearch = new FormControl('');
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly fb = inject(FormBuilder);
  filterForm: FormGroup = this.fb.group({
    location: [''],
    district: [''],
    minPrice: [''],
    maxPrice: [''],
    propertyType: ['']
  });
  private readonly propertyService = inject(PropertyService);
  private readonly cdr = inject(ChangeDetectorRef);

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      if (Object.keys(params).length > 0) {
        this.hasSearched = true;
        this.filterForm.patchValue(params, {emitEvent: false});
        this.appliedLocation = params['location'] || '';
        this.onFilter();
      }
    });
  }

  onInitialSearch() {
    if (this.initialSearch.value) {
      this.filterForm.patchValue({location: this.initialSearch.value});
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
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erro ao buscar imóveis:', err);
        this.isLoading = false;
        this.cdr.detectChanges();
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

}
