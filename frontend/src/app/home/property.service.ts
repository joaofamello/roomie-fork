import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PropertyService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/properties';

  buscarComFiltros(filtros: any) {
    let params = new HttpParams();
    
    Object.keys(filtros).forEach(key => {
      if (filtros[key]) {
        params = params.append(key, filtros[key]);
      }
    });
    return this.http.get(this.apiUrl, { params });
  }

}