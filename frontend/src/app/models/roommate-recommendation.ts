export interface RoommateRecommendation {
  studentId: number;
  name: string;
  major: string;
  compatibilityPercentage: number;
  commonInterests: string[];
}
