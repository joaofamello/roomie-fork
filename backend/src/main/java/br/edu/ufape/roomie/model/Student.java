package br.edu.ufape.roomie.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "estudante")
@PrimaryKeyJoinColumn(name = "id_estudante")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Student extends User {

    @Column(name = "curso", nullable = false, length = 100)
    private String major;

    @Column(name = "instituicao", nullable = false, length = 100)
    private String institution;

    @OneToOne(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Habit habit;

}
