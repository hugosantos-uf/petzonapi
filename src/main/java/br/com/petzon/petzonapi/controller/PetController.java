package br.com.petzon.petzonapi.controller;

import br.com.petzon.petzonapi.dto.CreatePetDto;
import br.com.petzon.petzonapi.entity.Pet;
import br.com.petzon.petzonapi.exception.PetNaoEncontradoException;
import br.com.petzon.petzonapi.service.PetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/pets")
public class PetController {

    private final PetService petService;
    private final ObjectMapper objectMapper;

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

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<Pet> cadastrarPet(
            @RequestPart("pet") String petJson, // 1. Recebe a parte "pet" como uma String
            @RequestPart("imagem") MultipartFile imagem) throws IOException {

        // 2. Converte manualmente a string JSON para o objeto DTO
        CreatePetDto petDto = objectMapper.readValue(petJson, CreatePetDto.class);

        Pet petSalvo = petService.cadastrarPet(petDto, imagem);
        return new ResponseEntity<>(petSalvo, HttpStatus.CREATED);
    }

    // *** E A MESMA CORREÇÃO APLICADA NO UPDATE ***
    @PutMapping(value = "/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<Pet> atualizarPet(
            @PathVariable Integer id,
            @RequestPart("pet") String petJson, // Recebe como String
            @RequestPart(value = "imagem", required = false) MultipartFile imagem) {

        try {
            // Converte manualmente
            CreatePetDto petDto = objectMapper.readValue(petJson, CreatePetDto.class);

            Pet petAtualizado = petService.atualizarPet(id, petDto, imagem);
            return ResponseEntity.ok(petAtualizado);
        } catch (PetNaoEncontradoException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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