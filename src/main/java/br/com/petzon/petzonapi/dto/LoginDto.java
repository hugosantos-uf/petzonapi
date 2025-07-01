package br.com.petzon.petzonapi.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class LoginDto {
    @NotBlank
    private String email;
    @NotBlank
    private String senha;
}