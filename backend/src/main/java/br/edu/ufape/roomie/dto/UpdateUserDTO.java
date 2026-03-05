package br.edu.ufape.roomie.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdateUserDTO {
    private String name;
    private String email;
    private String currentPassword;
    private String newPassword;
    private List<String> phones;
}
