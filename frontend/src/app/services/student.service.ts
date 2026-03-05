import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {StudentContactView} from '../models/student-contact-view';
import {environment} from '../../enviroments/enviroment';

export interface StudentProfileDto {
  userId: number;
  major: string;
  institution: string;
}

@Injectable({
  providedIn: 'root'
})
export class StudentService {
  private readonly apiUrl = `${environment.apiUrl}/api/students`;

  constructor(private readonly http: HttpClient) {
  }

  createProfile(dto: StudentProfileDto): Observable<string> {
    return this.http.post(`${this.apiUrl}/profile`, dto, {responseType: 'text'});
  }

  updateProfile(dto: StudentProfileDto): Observable<string> {
    return this.http.put(`${this.apiUrl}/profile`, dto, {responseType: 'text'});
  }

  getAll(): Observable<StudentContactView[]> {
    return this.http.get<StudentContactView[]>(this.apiUrl);
  }

  getById(id: number): Observable<StudentContactView> {
    return this.http.get<StudentContactView>(`${this.apiUrl}/${id}`);
  }
}

