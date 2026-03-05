import {PropertyType} from './property-type.enum';

export interface Address {
  street: string;
  district: string;
  cep: string;
  number?: string;
  city?: string;
  state?: string;
}

export interface PropertyRequest {
  title: string;
  description: string;
  type: PropertyType;
  price: number;
  availableVacancies: number; // mapeado para numero de quartos
  address: Address;
  gender?: string;
  acceptAnimals?: boolean;
  hasGarage?: boolean;
}
