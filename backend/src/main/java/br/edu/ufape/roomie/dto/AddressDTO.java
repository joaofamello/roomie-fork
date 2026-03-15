package br.edu.ufape.roomie.dto;

import lombok.Data;

@Data
public class AddressDTO {
  private String street;
  private String district;
  private String number;
  private String city;
  private String state;
  private String cep;
}
