package br.edu.ufape.roomie.dto;

import br.edu.ufape.roomie.enums.PropertyType;
import br.edu.ufape.roomie.enums.UserGender;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PropertyRequestDTO {
    @NotBlank(message = "O título não pode estar vazio")
    @Size(min = 5, max = 100, message = "O título deve ter entre 5 e 100 caracteres")
    private String title;

    private String description;

    @NotNull(message = "O tipo do imóvel é obrigatório")
    private PropertyType type;

    @NotNull(message = "O preço é obrigatório")
    @Positive(message = "O preço deve ser um valor positivo")
    private BigDecimal price;

    @NotNull(message = "Defina o gênero preferencial")
    private UserGender gender;

    @NotNull(message = "Informe se aceita animais")
    private Boolean acceptAnimals;

    @NotNull(message = "Informe se tem garagem")
    private Boolean hasGarage;

    @NotNull(message = "A quantidade de vagas é obrigatória")
    @Min(value = 1, message = "Deve haver pelo menos 1 vaga disponível")
    private Integer availableVacancies;

    @Valid
    @NotNull(message = "Os dados de endereço são obrigatórios")
    private AddressDTO address;

}
