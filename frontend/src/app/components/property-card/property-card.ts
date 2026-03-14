import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterLink} from '@angular/router';
import {Property} from '../../models/property';
import {environment} from '../../../enviroments/enviroment';

@Component({
  selector: 'app-property-card',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './property-card.html',
  styleUrl: './property-card.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PropertyCard {
  @Input() property!: Property;

  get propertyId(): number {
    return this.property.id || (this.property as any).idImovel;
  }

  get propertyTitle(): string {
    return this.property.title || (this.property as any).titulo || 'Sem título';
  }

  get propertyPrice(): number {
    return this.property.price || (this.property as any).preco || 0;
  }

  get propertyDistrict(): string {
    if (this.property.address?.district) return this.property.address.district;
    if (this.property.neighborhood) return this.property.neighborhood;
    return (this.property as any).bairro || '';
  }

  get propertyCity(): string {
    if (this.property.address?.city) return this.property.address.city;
    return (this.property as any).cidade || '';
  }

  get propertyVacancies(): number {
    return this.property.availableVacancies ?? (this.property as any).vagasDisponiveis ?? 0;
  }
  
  get propertyAcceptsAnimals(): boolean {
    return this.property.acceptAnimals || (this.property as any).aceitaAnimais || false;
  }
  
  get propertyHasGarage(): boolean {
    return this.property.hasGarage || (this.property as any).temGaragem || false;
  }

  readonly apiBase = environment.apiUrl;

  get firstPhotoUrl(): string | null {
    if (this.property.photos && this.property.photos.length > 0) {
      const path = this.property.photos[0].path;
      return path.startsWith('http') ? path : this.apiBase + path;
    }
    if (this.property.mainPhotoUrl) {
      return this.property.mainPhotoUrl;
    }
    return null;
  }
}
