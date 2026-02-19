package br.edu.ufape.roomie.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import br.edu.ufape.roomie.enums.UserGender;
import br.edu.ufape.roomie.enums.UserRole;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @Column(name = "nome")
    private String name;

    @Column(name = "email", unique = true)
    private String email;

    @JsonIgnore
    @Column(name = "cpf", unique = true)
    private String cpf;

    @JsonIgnore
    @Column(name = "senha")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "genero")
    private UserGender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "cargo")
    private UserRole role;

    @JsonIgnore
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Telefone> telefones = new ArrayList<>();

    public User(String name, String email, String cpf, String password, UserGender gender, UserRole role) {
        this.name = name;
        this.email = email;
        this.cpf = cpf;
        this.password = password;
        this.gender = gender;
        this.role = role;
    }

    public void addTelefone(String numero){
        Telefone novoTelefone = new Telefone(numero, this);
        this.telefones.add(novoTelefone);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.role == UserRole.ADMIN) {
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER"));
        }
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}