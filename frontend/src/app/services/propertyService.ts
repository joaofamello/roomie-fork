import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Property } from '../models/property';


@Injectable({
  providedIn: 'root',
})
export class PropertyService {
  private apiUrl = 'http://localhost:8080/api/properties';

  constructor(private http:HttpClient){}

  getAll(): Observable<Property[]> {
    return this.http.get<Property[]>(this.apiUrl);
  }

  createProperty(propertyData: any): Observable<any> {
    return this.http.post(this.apiUrl, propertyData);
  }

}

