package br.edu.ufape.roomie.service;

import br.edu.ufape.roomie.dto.UpdateUserDTO;
import br.edu.ufape.roomie.model.User;
import br.edu.ufape.roomie.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public User updateProfile(Long userId, UpdateUserDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuário não encontrado!"));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            user.setName(dto.getName());
        }

        if (dto.getEmail() != null && !dto.getEmail().isBlank() && !dto.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmail(dto.getEmail()) != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail já está em uso.");
            }
            user.setEmail(dto.getEmail());
        }

        if (dto.getNewPassword() != null && !dto.getNewPassword().isBlank()) {
            if (dto.getCurrentPassword() == null || dto.getCurrentPassword().isBlank() ||
                    !passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha atual incorreta ou não informada.");
            }
            user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        }

        if (dto.getPhones() != null) {
            user.getTelefones().clear();
            for (String numero : dto.getPhones()) {
                if (numero != null && !numero.isBlank()) {
                    user.addTelefone(numero);
                }
            }
        }

        return userRepository.save(user);
    }
}
