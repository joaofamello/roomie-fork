package br.edu.ufape.roomie.seed;

import br.edu.ufape.roomie.enums.*;
import br.edu.ufape.roomie.model.*;
import br.edu.ufape.roomie.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class RoomieDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PropertyRepository propertyRepository;
    private final HabitRepository habitRepository;
    private final HobbyRepository hobbyRepository;
    private final LifeStyleRepository lifeStyleRepository;
    private final CleaningPrefsRepository cleaningPrefsRepository;
    private final PropertyPhotoRepository propertyPhotoRepository;
    private final PropertyEvaluationRepository propertyEvaluationRepository;
    private final ContractRepository contractRepository;
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    @SuppressWarnings("NullableProblems")
    public void run(String... args) {
        if (userRepository.count() == 0) {
            seedDatabase();
        }
    }

    private void seedDatabase() {
        Faker faker = new Faker(Locale.of("pt", "BR"));
        SecureRandom random = new SecureRandom();

        String defaultPassword = passwordEncoder.encode("123456");

        // ── 20 proprietários ────────────────────────────────────────────────
        List<User> savedOwners = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            User owner = new User();
            owner.setName(faker.name().fullName());
            owner.setEmail(faker.internet().emailAddress());
            owner.setCpf(faker.cpf().valid());
            owner.setPassword(defaultPassword);
            owner.setGender(UserGender.values()[random.nextInt(UserGender.values().length)]);
            owner.setRole(UserRole.USER);
            owner.addTelefone(faker.phoneNumber().cellPhone());
            savedOwners.add(userRepository.save(owner));
        }

        // ── 50 estudantes + habit + hobbies + estilos + prefs de limpeza ────
        List<Student> savedStudents = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Student student = new Student();
            student.setName(faker.name().fullName());
            student.setEmail(faker.internet().emailAddress());
            student.setCpf(faker.cpf().valid());
            student.setPassword(defaultPassword);
            student.setGender(UserGender.values()[random.nextInt(UserGender.values().length)]);
            student.setRole(UserRole.USER);
            student.setMajor(faker.educator().course());
            student.setInstitution("UFAPE");
            student.addTelefone(faker.phoneNumber().cellPhone());
            Student saved = studentRepository.save(student);
            savedStudents.add(saved);

            // Habit
            Habit habit = new Habit();
            habit.setStudent(saved);
            habit.setStudySchedule(StudySchedule.values()[random.nextInt(StudySchedule.values().length)]);
            Habit savedHabit = habitRepository.save(habit);

            // 2 hobbies por estudante
            for (int h = 0; h < 2; h++) {
                Hobby hobby = new Hobby();
                hobby.setHabit(savedHabit);
                hobby.setHobby(faker.hobby().activity());
                hobbyRepository.save(hobby);
            }

            // 2 estilos de vida por estudante
            for (int l = 0; l < 2; l++) {
                LifeStyle lifeStyle = new LifeStyle();
                lifeStyle.setHabit(savedHabit);
                lifeStyle.setStyle(faker.options().option(
                        "Organizado", "Descontraído", "Noturno", "Matutino",
                        "Sociável", "Reservado", "Vegetariano", "Fitness"));
                lifeStyleRepository.save(lifeStyle);
            }

            // 2 preferências de limpeza por estudante
            for (int c = 0; c < 2; c++) {
                CleaningPrefs pref = new CleaningPrefs();
                pref.setHabit(savedHabit);
                pref.setPref(faker.options().option(
                        "Limpeza diária", "Limpeza semanal", "Limpeza quinzenal",
                        "Divisão de tarefas", "Cada um limpa o seu", "Rodízio mensal"));
                cleaningPrefsRepository.save(pref);
            }
        }

        // ── 100 imóveis com proprietários aleatórios ──────────────────────────
        List<Property> savedProperties = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            User randomOwner = savedOwners.get(random.nextInt(savedOwners.size()));

            Property property = new Property();
            property.setOwner(randomOwner);
            property.setTitle("Quarto " + faker.color().name() + " perto da universidade");
            property.setDescription(faker.lorem().paragraph(2));
            property.setType(PropertyType.values()[random.nextInt(PropertyType.values().length)]);
            property.setPrice(BigDecimal.valueOf(faker.number().randomDouble(2, 350, 1500)));
            property.setGender(UserGender.values()[random.nextInt(UserGender.values().length)]);
            property.setAcceptAnimals(faker.bool().bool());
            property.setHasGarage(faker.bool().bool());
            property.setAvailableVacancies(faker.number().numberBetween(1, 5));
            property.setStatus(PropertyStatus.ACTIVE);

            Address address = new Address();
            address.setProperty(property);
            address.setStreet(faker.address().streetName());
            address.setDistrict(faker.address().cityName());
            address.setNumber(faker.address().buildingNumber());
            address.setCity("Garanhuns");
            address.setState("PE");
            address.setCep(faker.address().zipCode());
            property.setAddress(address);

            Property savedProperty = propertyRepository.save(property);
            savedProperties.add(savedProperty);

            // 2 fotos por imóvel (100 fotos no total)
            for (int p = 0; p < 2; p++) {
                PropertyPhoto photo = new PropertyPhoto();
                photo.setProperty(savedProperty);
                photo.setPath("https://picsum.photos/seed/" + savedProperty.getId() + p + "/800/600");
                propertyPhotoRepository.save(photo);
            }
        }

        // ── 50 avaliações de imóveis ─────────────────────────────────────────
        for (int i = 0; i < 50; i++) {
            PropertyEvaluation evaluation = new PropertyEvaluation();
            evaluation.setProperty(savedProperties.get(random.nextInt(savedProperties.size())));
            evaluation.setStudent(savedStudents.get(random.nextInt(savedStudents.size())));
            evaluation.setRating(faker.number().numberBetween(1, 5));
            evaluation.setComment(faker.lorem().sentence());
            evaluation.setTimestamp(LocalDateTime.now().minusDays(random.nextInt(365)));
            propertyEvaluationRepository.save(evaluation);
        }

        // ── 50 contratos ─────────────────────────────────────────────────────
        for (int i = 0; i < 50; i++) {
            Property property = savedProperties.get(random.nextInt(savedProperties.size()));
            Student student = savedStudents.get(random.nextInt(savedStudents.size()));

            LocalDate start = LocalDate.now().minusMonths(random.nextInt(12) + 1);
            LocalDate end = start.plusMonths(random.nextInt(12) + 1);

            Contract contract = new Contract();
            contract.setProperty(property);
            contract.setStudent(student);
            contract.setOwner(property.getOwner());
            contract.setStartDate(start);
            contract.setEndDate(end);
            contract.setPrice(property.getPrice());
            contract.setStatus(ContractStatus.values()[random.nextInt(ContractStatus.values().length)]);
            contractRepository.save(contract);
        }

        // ── 50 chats + 3 mensagens cada (150 mensagens no total) ─────────────
        for (int i = 0; i < 50; i++) {
            Property property = savedProperties.get(random.nextInt(savedProperties.size()));
            Student student = savedStudents.get(random.nextInt(savedStudents.size()));

            Chat chat = new Chat();
            chat.setStudent(student);
            chat.setUser(property.getOwner());
            chat.setProperty(property);
            chat.setTimestamp(LocalDateTime.now().minusDays(random.nextInt(60)));
            Chat savedChat = chatRepository.save(chat);

            // 3 mensagens por chat
            for (int m = 0; m < 3; m++) {
                Message message = new Message();
                message.setChat(savedChat);
                // alterna remetente entre estudante e proprietário
                message.setSender(m % 2 == 0 ? student : property.getOwner());
                message.setContent(faker.lorem().sentence());
                message.setTimestamp(savedChat.getTimestamp().plusMinutes(m * 10L));
                message.setRead(faker.bool().bool());
                messageRepository.save(message);
            }
        }
    }
}
