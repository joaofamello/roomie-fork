export interface UpdateUserDto {
  name?: string;
  email?: string;
  currentPassword?: string;
  newPassword?: string;
  phones?: string[];
}
