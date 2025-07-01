package br.com.petzon.petzonapi.dto;

import lombok.Data;

import java.util.Set;

@Data
public class UsuarioDto {
    private Integer idUsuario;
    private String nome;
    private String email;
    private Set<String> cargos;
}