package br.com.petzon.petzonapi.dto;

import lombok.Data;

import java.util.Set;

@Data
public class UsuarioResponse {
    private int idUsuario;
    private String nome;
    private String email;
    private Set<String> cargos;
    private boolean ativo;
}