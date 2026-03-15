package br.edu.ufape.roomie.dto;

import java.util.List;
import lombok.Data;

@Data
public class UpdateUserDTO {
  private String name;
  private String email;
  private String currentPassword;
  private String newPassword;
  private List<String> phones;
}
