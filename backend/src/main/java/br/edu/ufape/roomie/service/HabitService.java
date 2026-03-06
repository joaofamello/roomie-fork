package br.edu.ufape.roomie.service;

import br.edu.ufape.roomie.dto.HabitRequestDTO;
import br.edu.ufape.roomie.dto.HabitResponseDTO;
import br.edu.ufape.roomie.enums.StudySchedule;
import br.edu.ufape.roomie.model.CleaningPrefs;
import br.edu.ufape.roomie.model.Habit;
import br.edu.ufape.roomie.model.Hobby;
import br.edu.ufape.roomie.model.LifeStyle;
import br.edu.ufape.roomie.model.Student;
import br.edu.ufape.roomie.repository.HabitRepository;
import br.edu.ufape.roomie.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HabitService {

    private final HabitRepository habitRepository;
    private final StudentRepository studentRepository;

    @Transactional(readOnly = true)
    public HabitResponseDTO getHabitByStudent(Student student) {
        Student managedStudent = studentRepository.findById(student.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estudante não encontrado."));
        Habit habit = managedStudent.getHabit();
        if (habit == null) {
            return null;
        }
        return toResponseDTO(habit);
    }

    @Transactional
    public HabitResponseDTO createOrUpdateHabit(Student student, HabitRequestDTO dto) {
        Student managedStudent = studentRepository.findById(student.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estudante não encontrado."));

        Habit habit = managedStudent.getHabit();

        if (habit == null) {
            habit = new Habit();
            habit.setStudent(managedStudent);
        }

        // Horário de estudo
        if (dto.getStudySchedule() != null && !dto.getStudySchedule().isBlank()) {
            try {
                habit.setStudySchedule(StudySchedule.valueOf(dto.getStudySchedule().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Horário de estudo inválido. Use: MORNING, AFTERNOON, EVENING ou DAWN.");
            }
        } else {
            habit.setStudySchedule(null);
        }

        // Hobbies — limpa e recria
        habit.getHobbies().clear();
        if (dto.getHobbies() != null) {
            for (String hobbyName : dto.getHobbies()) {
                if (hobbyName != null && !hobbyName.isBlank()) {
                    Hobby hobby = new Hobby();
                    hobby.setHabit(habit);
                    hobby.setHobby(hobbyName.trim());
                    habit.getHobbies().add(hobby);
                }
            }
        }

        // Estilos de vida — limpa e recria
        habit.getLifeStyles().clear();
        if (dto.getLifeStyles() != null) {
            for (String styleName : dto.getLifeStyles()) {
                if (styleName != null && !styleName.isBlank()) {
                    LifeStyle lifeStyle = new LifeStyle();
                    lifeStyle.setHabit(habit);
                    lifeStyle.setStyle(styleName.trim());
                    habit.getLifeStyles().add(lifeStyle);
                }
            }
        }

        // Preferências de limpeza — limpa e recria
        habit.getCleaningPrefs().clear();
        if (dto.getCleaningPrefs() != null) {
            for (String prefName : dto.getCleaningPrefs()) {
                if (prefName != null && !prefName.isBlank()) {
                    CleaningPrefs pref = new CleaningPrefs();
                    pref.setHabit(habit);
                    pref.setPref(prefName.trim());
                    habit.getCleaningPrefs().add(pref);
                }
            }
        }

        habit = habitRepository.save(habit);
        return toResponseDTO(habit);
    }

    private HabitResponseDTO toResponseDTO(Habit habit) {
        return new HabitResponseDTO(
                habit.getIdHabit(),
                habit.getStudySchedule() != null ? habit.getStudySchedule().name() : null,
                habit.getHobbies().stream().map(Hobby::getHobby).toList(),
                habit.getLifeStyles().stream().map(LifeStyle::getStyle).toList(),
                habit.getCleaningPrefs().stream().map(CleaningPrefs::getPref).toList()
        );
    }
}
