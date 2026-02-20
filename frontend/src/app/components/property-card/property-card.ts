import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Property } from '../../models/property';

@Component({
  selector: 'app-property-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './property-card.html',
  styleUrl: './property-card.css',
})
export class PropertyCard {
  @Input() property!: Property;
  @Output() propertySelected = new EventEmitter<Property>();

  readonly apiBase = 'http://localhost:8080';

  get firstPhotoUrl(): string | null {
    if (this.property.photos && this.property.photos.length > 0) {
      return this.apiBase + this.property.photos[0].path;
    }
    if (this.property.mainPhotoUrl) {
      return this.property.mainPhotoUrl;
    }
    return null;
  }
}
