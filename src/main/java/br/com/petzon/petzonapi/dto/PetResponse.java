package br.com.petzon.petzonapi.dto;

import br.com.petzon.petzonapi.entity.Usuario;
import lombok.Data;

@Data
public class PetResponse extends PetRequest {
    private int id;
    private Usuario responsavel;
    private String urlFoto;
}
