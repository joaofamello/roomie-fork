export interface Property {
  id: number;
  title: string;
  price: number;
  neighborhood: string;
  mainPhotoUrl?: string;
  activeResidents?: Resident[];
}

export interface Resident{
  id: number;
  name: string;
  avatarUrl?: string;
}
