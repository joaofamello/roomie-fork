import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Property} from '../models/property';
import {PropertyDetailView} from '../models/property-detail-view';
import {environment} from '../../enviroments/enviroment';


@Injectable({
  providedIn: 'root',
})
export class PropertyService {
  private readonly apiUrl = `${environment.apiUrl}/api/properties`;

  constructor(private readonly http: HttpClient) {
  }

  getAll(): Observable<Property[]> {
    return this.http.get<Property[]>(this.apiUrl);
  }

  createProperty(propertyData: any): Observable<any> {
    return this.http.post(this.apiUrl, propertyData, {responseType: 'text' as 'json'});
  }

  getMyProperties(): Observable<PropertyDetailView[]> {
    return this.http.get<PropertyDetailView[]>(`${this.apiUrl}/meus`);
  }

  getById(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`);
  }

  publishProperty(id: number): Observable<any> {
    return this.http.patch(`${this.apiUrl}/${id}/publish`, {});
  }

  setDraft(id: number): Observable<any> {
    return this.http.patch(`${this.apiUrl}/${id}/draft`, {});
  }

  deleteProperty(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }

  updateProperty(id: number, formData: FormData): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, formData);
  }

  getAllDetails(): Observable<PropertyDetailView[]> {
    return this.http.get<PropertyDetailView[]>(`${this.apiUrl}/details`);
  }

  getDetailById(id: number): Observable<PropertyDetailView> {
    return this.http.get<PropertyDetailView>(`${this.apiUrl}/${id}/details`);
  }

}

