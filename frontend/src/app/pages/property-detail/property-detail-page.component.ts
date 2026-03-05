import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { PropertyService } from '../../services/propertyService';
import { PropertyDetailView } from '../../models/property-detail-view';
import { PropertyPhoto } from '../../models/property';
import { HeaderComponent } from '../../components/shared/header/header.component';
import { environment } from '../../../enviroments/enviroment';

@Component({
  selector: 'app-property-detail-page',
  standalone: true,
  imports: [CommonModule, HeaderComponent],
  templateUrl: './property-detail-page.component.html',
  styleUrl: './property-detail-page.component.css',
})
export class PropertyDetailPageComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly propertyService = inject(PropertyService);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly apiBase = environment.apiUrl;

  detail: PropertyDetailView | null = null;
  photos: PropertyPhoto[] = [];
  selectedPhotoIndex = 0;
  isLoading = true;
  errorMessage: string | null = null;

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    const id = Number(idParam);

    if (!idParam || isNaN(id)) {
      this.errorMessage = 'ID de imóvel inválido.';
      this.isLoading = false;
      this.cdr.detectChanges();
      return;
    }

    this.propertyService.getDetailById(id).subscribe({
      next: (data) => {
        this.detail = data;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erro ao carregar detalhes do imóvel:', err);
        this.errorMessage = 'Não foi possível carregar os detalhes deste imóvel.';
        this.isLoading = false;
        this.cdr.detectChanges();
      },
    });

    this.propertyService.getById(id).subscribe({
      next: (property) => {
        if (property?.photos?.length) {
          this.photos = property.photos;
          this.cdr.detectChanges();
        }
      },
      error: () => { /* sem fotos não bloqueia a página */ }
    });
  }

  photoUrl(path: string): string {
    return path.startsWith('http') ? path : this.apiBase + path;
  }

  get selectedPhotoSrc(): string | null {
    return this.photos.length ? this.photoUrl(this.photos[this.selectedPhotoIndex].path) : null;
  }

  prevPhoto(): void {
    this.selectedPhotoIndex =
      (this.selectedPhotoIndex - 1 + this.photos.length) % this.photos.length;
  }

  nextPhoto(): void {
    this.selectedPhotoIndex = (this.selectedPhotoIndex + 1) % this.photos.length;
  }

  goBack(): void {
    this.router.navigate(['/home']);
  }

  typeLabel(type?: string): string {
    const map: Record<string, string> = {
      HOUSE: 'Casa', APARTMENT: 'Apartamento', STUDIO: 'Studio',
      ROOM: 'Quarto Individual', DORMITORY: 'Dormitório',
    };
    return type ? (map[type] ?? type) : '—';
  }

  genderLabel(gender?: string): string {
    const map: Record<string, string> = {
      MALE: 'Masculino', FEMALE: 'Feminino', MIXED: 'Misto', OTHER: 'Outro',
    };
    return gender ? (map[gender] ?? gender) : '—';
  }
}

