package br.com.petzon.petzonapi.controller;

import br.com.petzon.petzonapi.dto.CreatePetDto;
import br.com.petzon.petzonapi.entity.Pet;
import br.com.petzon.petzonapi.exception.PetNaoEncontradoException;
import br.com.petzon.petzonapi.service.PetService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/pets")
public class PetController {

    private final PetService petService;

    @GetMapping
    public Page<Pet> listarPets(
            @RequestParam(required = false) String tipo,
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return petService.listarTodosOuPorTipo(tipo, pageable);
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
    @PutMapping("/{id}")
    public ResponseEntity<Pet> atualizarPet(@PathVariable Integer id, @RequestBody @Valid CreatePetDto petDto) {
        try {
            Pet petAtualizado = petService.atualizarPet(id, petDto);
            return ResponseEntity.ok(petAtualizado);
        } catch (PetNaoEncontradoException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarPet(@PathVariable Integer id) {
        try {
            petService.deletarPet(id);
            return ResponseEntity.noContent().build();
        } catch (PetNaoEncontradoException e) {
            return ResponseEntity.notFound().build();
        }
    }
}