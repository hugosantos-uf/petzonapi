package br.com.petzon.petzonapi.controller;

import br.com.petzon.petzonapi.dto.CreatePetDto;
import br.com.petzon.petzonapi.entity.Pet;
import br.com.petzon.petzonapi.service.PetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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

    @PostMapping
    public ResponseEntity<Pet> cadastrarPet(@RequestBody @Valid CreatePetDto petDto) {
        Pet petSalvo = petService.cadastrarPet(petDto);
        return new ResponseEntity<>(petSalvo, HttpStatus.CREATED);
    }
}