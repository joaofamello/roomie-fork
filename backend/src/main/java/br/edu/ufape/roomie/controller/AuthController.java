package br.edu.ufape.roomie.controller;

import br.edu.ufape.roomie.dto.LoginDTO;
import br.edu.ufape.roomie.dto.LoginResponseDTO;
import br.edu.ufape.roomie.dto.UserDTO;
import br.edu.ufape.roomie.dto.UserResponseDTO;
import br.edu.ufape.roomie.model.User;
import br.edu.ufape.roomie.service.AuthService;
import br.edu.ufape.roomie.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AuthService authService;

    @Autowired
    private TokenService tokenService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@RequestBody UserDTO userDTO) {
        UserResponseDTO registeredUser = this.authService.register(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginDTO data) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(data.getEmail(), data.getPassword());

        Authentication authentication = authenticationManager.authenticate(authToken);
        User user = (User) authentication.getPrincipal();
        String token = tokenService.generateToken(user);

        return ResponseEntity.ok(new LoginResponseDTO(token));

    }
}