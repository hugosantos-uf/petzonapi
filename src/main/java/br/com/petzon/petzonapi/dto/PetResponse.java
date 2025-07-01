package br.com.petzon.petzonapi.dto;

import br.com.petzon.petzonapi.entity.Usuario;

public class PetResponse extends PetRequest {
    private int idPet;
    private Usuario responsavel;
}
