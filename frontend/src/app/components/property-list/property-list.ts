import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {CommonModule} from '@angular/common';
import {Property} from '../../models/property';
import {PropertyCard} from '../property-card/property-card';

@Component({
  selector: 'app-property-list',
  standalone: true,
  imports: [CommonModule, PropertyCard],
  templateUrl: './property-list.html',
  styleUrl: './property-list.css',
  changeDetection: ChangeDetectionStrategy.Default,
})
export class PropertyList {
  @Input() properties: Property[] = [];
  @Input() loading = false;
}
