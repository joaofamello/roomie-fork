import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {environment} from '../../enviroments/enviroment';

@Injectable({
  providedIn: 'root'
})
export class PropertyService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/api/properties`;

  buscarComFiltros(filtros: any) {
    let params = new HttpParams();

    Object.keys(filtros).forEach(key => {
      if (filtros[key]) {
        params = params.append(key, filtros[key]);
      }
    });
    return this.http.get(this.apiUrl, {params});
  }

}
