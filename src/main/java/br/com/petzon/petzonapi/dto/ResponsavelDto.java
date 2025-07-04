package br.com.petzon.petzonapi.dto;

import lombok.Data;

@Data
public class ResponsavelDto {
    private Integer idUsuario;
    private String nome;
    private String email;
}