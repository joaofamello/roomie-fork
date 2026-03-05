package br.edu.ufape.roomie.dto;

import br.edu.ufape.roomie.enums.UserGender;
import br.edu.ufape.roomie.enums.UserRole;
import lombok.Data;

import java.util.List;

@Data
public class UserResponseDTO {
    private Long id;
    private String name;
    private String email;
    private UserGender gender;
    private List<String> phones;
    private UserRole role;
}
