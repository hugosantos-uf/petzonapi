package br.com.petzon.petzonapi.service;

import br.com.petzon.petzonapi.entity.Pet;
import br.com.petzon.petzonapi.entity.PetType;
import br.com.petzon.petzonapi.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;

    public List<Pet> listarTodosOuPorTipo(String tipo) {
        if (tipo != null) {
            try {
                PetType petType = PetType.valueOf(tipo.toUpperCase());
                return petRepository.findByTipo(petType);
            } catch (IllegalArgumentException e) {
                return Collections.emptyList();
            }
        }
        return petRepository.findAll();
    }
}