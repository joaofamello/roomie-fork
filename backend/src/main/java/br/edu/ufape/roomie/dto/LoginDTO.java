package br.edu.ufape.roomie.dto;

import lombok.Data;

@Data
public class LoginDTO {
    private String email;
    private String password;
}