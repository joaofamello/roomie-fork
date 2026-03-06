package br.edu.ufape.roomie.service;

import br.edu.ufape.roomie.dto.UserDTO;
import br.edu.ufape.roomie.dto.UserResponseDTO;
import br.edu.ufape.roomie.enums.UserRole;
import br.edu.ufape.roomie.model.Telefone;
import br.edu.ufape.roomie.model.User;
import br.edu.ufape.roomie.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username);
        if (user == null) {
            throw new UsernameNotFoundException("Usuário não encontrado");
        }

        return user;
    }

    public UserResponseDTO register(UserDTO userDTO) {
        if (this.userRepository.findByEmail(userDTO.getEmail()) != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email já cadastrado!");
        }
        if (this.userRepository.findByCpf(userDTO.getCpf()) != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CPF já cadastrado!");
        }

        String encryptedPassword = passwordEncoder.encode(userDTO.getPassword());

        UserRole role = userDTO.getRole();
        if (role == null) {
            role = UserRole.USER;
        }

        User newUser = new User(userDTO.getName(), userDTO.getEmail(), userDTO.getCpf(), encryptedPassword, userDTO.getGender(), role);

        if (userDTO.getPhones() != null) {
            for (String numero : userDTO.getPhones()) {
                newUser.addTelefone(numero);
            }
        }
        User savedUser = this.userRepository.save(newUser);

        UserResponseDTO response = new UserResponseDTO();
        response.setId(savedUser.getId());
        response.setName(savedUser.getName());
        response.setEmail(savedUser.getEmail());
        response.setGender(savedUser.getGender());
        response.setRole(savedUser.getRole());

        if (savedUser.getTelefones() != null) {
            response.setPhones(savedUser.getTelefones().stream()
                    .map(Telefone::getNumero)
                    .toList());
        }

        return response;
    }
}