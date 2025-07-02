package br.com.petzon.petzonapi.dto;

import br.com.petzon.petzonapi.entity.PetType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

@Data
public class PetRequest {
    @NotNull
    private PetType tipo;

    @NotBlank
    private String nome;

    @NotBlank
    private String temperamento;

    @NotBlank
    private String descricao;

    @NotNull
    @PositiveOrZero
    private Integer idade;
}

