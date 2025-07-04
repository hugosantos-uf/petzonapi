package br.com.petzon.petzonapi.controller;

import br.com.petzon.petzonapi.dto.PetRequest;
import br.com.petzon.petzonapi.dto.PetResponse;
import br.com.petzon.petzonapi.entity.Pet;
import br.com.petzon.petzonapi.entity.PetType;
import br.com.petzon.petzonapi.exception.NotFoundException;
import br.com.petzon.petzonapi.exception.RegraDeNegocioException;
import br.com.petzon.petzonapi.service.PetService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/pets")
public class PetController {

    private final PetService petService;

    @GetMapping("/tipo")
    public Page<PetResponse> listarPetsPorTipo(
            @RequestParam String tipo,
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return petService.listarPorTipo(tipo, pageable);
    }

    @GetMapping
    public Page<PetResponse> listarPets(
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return petService.listarTodos(pageable);
    }

    @GetMapping("/{id}")
    public PetResponse buscarPetPorId(@PathVariable Integer id) {
        return petService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<PetResponse> cadastrarPet(
            @RequestParam("nome") String nome,
            @RequestParam("tipo") PetType tipo,
            @RequestParam("temperamento") String temperamento,
            @RequestParam("idade") int idade,
            @RequestParam("descricao") String descricao,
            @RequestParam("foto") MultipartFile foto) throws IOException, RegraDeNegocioException {

        PetRequest pet = new PetRequest();
        pet.setNome(nome);
        pet.setTipo(tipo);
        pet.setTemperamento(temperamento);
        pet.setIdade(idade);
        pet.setDescricao(descricao);

        PetResponse petSalvo = petService.cadastrarPet(pet, foto);
        return new ResponseEntity<>(petSalvo, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PetResponse> atualizarPet(
            @PathVariable Integer id,
            @RequestParam("nome") String nome,
            @RequestParam("tipo") PetType tipo,
            @RequestParam("temperamento") String temperamento,
            @RequestParam("idade") int idade,
            @RequestParam("descricao") String descricao,
            @RequestParam("foto") MultipartFile foto) {

        PetRequest pet = new PetRequest();
        pet.setNome(nome);
        pet.setTipo(tipo);
        pet.setTemperamento(temperamento);
        pet.setIdade(idade);
        pet.setDescricao(descricao);

        try {
            PetResponse petAtualizado = petService.atualizarPet(id, pet, foto);
            return ResponseEntity.ok(petAtualizado);
        } catch (NotFoundException e) {
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
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}