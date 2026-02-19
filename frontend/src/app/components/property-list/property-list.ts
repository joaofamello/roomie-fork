import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PropertyService } from '../../services/propertyService';
import { Property } from '../../models/property';
import { PropertyCard } from '../property-card/property-card';

@Component({
  selector: 'app-property-list',
  standalone: true,
  imports: [CommonModule, PropertyCard],
  templateUrl: './property-list.html',
  styleUrl: './property-list.css',
})
export class PropertyList implements OnInit {
  properties: Property[] = [];
  loading = false;

  constructor(private propertyService: PropertyService) {}

  ngOnInit(): void {
      this.loadProperties();
  }

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
}
