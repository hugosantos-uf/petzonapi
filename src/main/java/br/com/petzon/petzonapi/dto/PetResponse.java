package br.com.petzon.petzonapi.dto;

import lombok.Data;

@Data
public class PetResponse extends PetRequest {
    private int id;
    private ResponsavelDto responsavel;
    private String urlFoto;
}
