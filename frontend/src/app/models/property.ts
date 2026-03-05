export interface PropertyPhoto {
  id: number;
  path: string;
}

export interface PropertyAddress {
  idAddress?: number;
  street: string;
  district: string;
  number: string;
  city: string;
  state: string;
  cep: string;
}

export interface PropertyOwner {
  id: number;
  name: string;
  email?: string;
}

export interface Property {
  id: number;
  title: string;
  description?: string;
  type?: string;
  price: number;
  gender?: string;
  acceptAnimals?: boolean;
  hasGarage?: boolean;
  availableVacancies?: number;
  status?: string;
  address?: PropertyAddress;
  photos?: PropertyPhoto[];
  owner?: PropertyOwner;
  // legacy fields kept for compat
  neighborhood?: string;
  mainPhotoUrl?: string;
  activeResidents?: Resident[];
}

export interface Resident {
  id: number;
  name: string;
  avatarUrl?: string;
}
