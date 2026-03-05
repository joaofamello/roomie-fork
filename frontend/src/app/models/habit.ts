export interface HabitRequest {
  studySchedule: string | null;
  hobbies: string[];
  lifeStyles: string[];
  cleaningPrefs: string[];
}

export interface HabitResponse {
  id: number;
  studySchedule: string | null;
  hobbies: string[];
  lifeStyles: string[];
  cleaningPrefs: string[];
}
