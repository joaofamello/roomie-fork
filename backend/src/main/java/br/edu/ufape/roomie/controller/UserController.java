package br.edu.ufape.roomie.controller;

import br.edu.ufape.roomie.dto.UpdateUserDTO;
import br.edu.ufape.roomie.dto.UserResponseDTO;
import br.edu.ufape.roomie.model.Telefone;
import br.edu.ufape.roomie.model.User;
import br.edu.ufape.roomie.projection.OwnerReportView;
import br.edu.ufape.roomie.repository.UserRepository;
import br.edu.ufape.roomie.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @PatchMapping("/profile")
    public ResponseEntity<UserResponseDTO> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @RequestBody UpdateUserDTO dto) {
        User userUpdated = userService.updateProfile(currentUser.getId(), dto);

        UserResponseDTO response = new UserResponseDTO();
        response.setId(userUpdated.getId());
        response.setName(userUpdated.getName());
        response.setEmail(userUpdated.getEmail());
        response.setGender(userUpdated.getGender());
        if (userUpdated.getTelefones() != null) {
            response.setPhones(userUpdated.getTelefones().stream()
                    .map(Telefone::getNumero)
                    .toList());
        }
        response.setRole(userUpdated.getRole());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/owners/report")
    public ResponseEntity<List<OwnerReportView>> getOwnersReport() {
        return ResponseEntity.ok(userRepository.findOwnerReports());
    }
}
