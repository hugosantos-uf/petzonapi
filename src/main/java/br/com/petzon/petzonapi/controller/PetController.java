package br.com.petzon.petzonapi.controller;

import br.com.petzon.petzonapi.entity.Pet;
import br.com.petzon.petzonapi.service.PetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/pets")
public class PetController {

    private final PetService petService;

    @GetMapping
    public List<Pet> listarPets(@RequestParam(required = false) String tipo) {

        return petService.listarTodosOuPorTipo(tipo);
    }

    @GetMapping("/{id}")
    public Pet buscarPetPorId(@PathVariable Integer id) {
        return petService.buscarPorId(id);
    }
}