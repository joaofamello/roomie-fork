import {TestBed} from '@angular/core/testing';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {provideHttpClient} from '@angular/common/http';
import {RecommendationService} from './recommendation.service';
import {RoommateRecommendation} from '../models/roommate-recommendation';
import {environment} from '../../enviroments/enviroment';

describe('RecommendationService', () => {
  let service: RecommendationService;
  let httpMock: HttpTestingController;

  const mockRecommendations: RoommateRecommendation[] = [
    {studentId: 1, name: 'Alice', major: 'CC', compatibilityPercentage: 85, commonInterests: ['Leitura']},
    {studentId: 2, name: 'Bob', major: 'EC', compatibilityPercentage: 60, commonInterests: ['Esportes']},
    {studentId: 3, name: 'Carol', major: 'CC', compatibilityPercentage: 40, commonInterests: ['Música']},
  ];

  beforeEach(() => {
    sessionStorage.clear();

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        RecommendationService,
      ],
    });

    service = TestBed.inject(RecommendationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    sessionStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch roommate recommendations from the API', () => {
    service.getRoommateRecommendations().subscribe(result => {
      expect(result).toEqual(mockRecommendations);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/recommendations/roommates`);
    expect(req.request.method).toBe('GET');
    req.flush(mockRecommendations);
  });

  it('should filter out ignored recommendations', () => {
    service.ignoreRecommendation(2);

    service.getFilteredRecommendations().subscribe(result => {
      expect(result.length).toBe(2);
      expect(result.find(r => r.studentId === 2)).toBeUndefined();
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/recommendations/roommates`);
    req.flush(mockRecommendations);
  });

  it('should add a student to the ignored list', () => {
    service.ignoreRecommendation(1);
    expect(service.isIgnored(1)).toBe(true);
    expect(service.ignoredCount).toBe(1);
  });

  it('should undo an ignore action', () => {
    service.ignoreRecommendation(1);
    expect(service.isIgnored(1)).toBe(true);

    service.undoIgnore(1);
    expect(service.isIgnored(1)).toBe(false);
    expect(service.ignoredCount).toBe(0);
  });

  it('should clear all ignored recommendations', () => {
    service.ignoreRecommendation(1);
    service.ignoreRecommendation(2);
    expect(service.ignoredCount).toBe(2);

    service.clearIgnored();
    expect(service.ignoredCount).toBe(0);
    expect(service.isIgnored(1)).toBe(false);
  });

  it('should persist ignored ids in sessionStorage', () => {
    service.ignoreRecommendation(5);
    service.ignoreRecommendation(10);

    const stored = sessionStorage.getItem('roomie_ignored_recommendations');
    expect(stored).toBeTruthy();
    const parsed: number[] = JSON.parse(stored!);
    expect(parsed).toContain(5);
    expect(parsed).toContain(10);
  });

  it('should load ignored ids from sessionStorage on init', () => {
    sessionStorage.setItem('roomie_ignored_recommendations', JSON.stringify([7, 8]));

    // Recria o service para ler do storage
    const freshService = new RecommendationService(TestBed.inject(HttpTestingController) as any);
    expect(freshService.isIgnored(7)).toBe(true);
    expect(freshService.isIgnored(8)).toBe(true);
  });

  it('should emit updated ignored ids via observable', (done) => {
    const emissions: Set<number>[] = [];

    service.ignoredIds$.subscribe(ids => {
      emissions.push(new Set(ids));
      if (emissions.length === 2) {
        expect(emissions[1].has(42)).toBe(true);
        done();
      }
    });

    service.ignoreRecommendation(42);
  });
});
