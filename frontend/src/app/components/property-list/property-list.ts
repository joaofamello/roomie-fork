import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PropertyService } from '../../services/propertyService';
import { Property } from '../../models/property';
import { PropertyCard } from '../property-card/property-card';
import { PropertyDetail } from '../property-detail/property-detail';

@Component({
  selector: 'app-property-list',
  standalone: true,
  imports: [CommonModule, PropertyCard, PropertyDetail],
  templateUrl: './property-list.html',
  styleUrl: './property-list.css',
})
export class PropertyList implements OnInit {
  @Input() properties: Property[] = [];
  @Input() loading = false;

  selectedProperty: Property | null = null;

  constructor(private propertyService: PropertyService) {}

  ngOnInit(): void {}

  loadProperties(){
    this.loading = true;

    this.propertyService.getAll().subscribe({
      next: (data) => {
        this.properties = data;
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.loading = false;
      }
    });
  }

  openDetail(property: Property): void {
    this.selectedProperty = property;
  }

  closeDetail(): void {
    this.selectedProperty = null;
  }
}
