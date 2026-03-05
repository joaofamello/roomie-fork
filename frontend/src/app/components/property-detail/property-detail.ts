import {Component, EventEmitter, Input, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {Property} from '../../models/property';
import {PropertyDetailView} from '../../models/property-detail-view';
import {environment} from '../../../enviroments/enviroment';

@Component({
  selector: 'app-property-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './property-detail.html',
  styleUrl: './property-detail.css',
})
export class PropertyDetail {
  @Input() property!: Property;
  @Input() detail?: PropertyDetailView | null;
  @Output() closed = new EventEmitter<void>();

  readonly apiBase = environment.apiUrl;

  selectedPhotoIndex = 0;

  get photos() {
    return this.property.photos ?? [];
  }

  get selectedPhotoUrl(): string | null {
    if (this.photos.length > 0) {
      const path = this.photos[this.selectedPhotoIndex].path;
      return path.startsWith('http') ? path : this.apiBase + path;
    }
    return null;
  }

  selectPhoto(index: number): void {
    this.selectedPhotoIndex = index;
  }

  photoUrl(path: string): string {
    return path.startsWith('http') ? path : this.apiBase + path;
  }

  typeLabel(type?: string): string {
    const map: Record<string, string> = {
      HOUSE: 'Casa',
      APARTMENT: 'Apartamento',
      STUDIO: 'Studio',
      ROOM: 'Quarto Individual',
      DORMITORY: 'Dormitório',
    };
    return type ? (map[type] ?? type) : '—';
  }

  genderLabel(gender?: string): string {
    const map: Record<string, string> = {
      MALE: 'Masculino',
      FEMALE: 'Feminino',
      MIXED: 'Misto',
      OTHER: 'Outro',
    };
    return gender ? (map[gender] ?? gender) : '—';
  }

  closeOnOverlay(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('modal-overlay')) {
      this.closed.emit();
    }
  }
}
