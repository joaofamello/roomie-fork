export interface UserResponseDto {
  id: number;
  name: string;
  email: string;
  gender?: string;
  phones?: string[];
  role: string;
}
