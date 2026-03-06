import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {Property} from '../models/property';
import {PropertyDetailView} from '../models/property-detail-view';
import {PropertyRankingView} from '../models/property-ranking-view';
import {environment} from '../../enviroments/enviroment';


@Injectable({
  providedIn: 'root',
})
export class PropertyService {
  private readonly apiUrl = `${environment.apiUrl}/api/properties`;
  private readonly announcementsUrl = `${environment.apiUrl}/announcements`;

  constructor(private readonly http: HttpClient) {
  }

  getAll(): Observable<Property[]> {
    return this.http.get<Property[]>(this.apiUrl);
  }

  createProperty(propertyData: any): Observable<{ id: number }> {
    return this.http.post<{ id: number }>(this.apiUrl, propertyData);
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

  expressInterest(propertyId: number): Observable<string> {
    return this.http.post(`${this.announcementsUrl}/${propertyId}/interest`, {}, {responseType: 'text'});
  }

  checkInterest(propertyId: number): Observable<boolean> {
    return this.http.get<{hasInterest: boolean}>(`${this.announcementsUrl}/${propertyId}/interest/check`)
      .pipe(map(res => res.hasInterest));
  }

  getRanking(): Observable<PropertyRankingView[]> {
    return this.http.get<PropertyRankingView[]>(`${this.apiUrl}/ranking`);
  }

}

