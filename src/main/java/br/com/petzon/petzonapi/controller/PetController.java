package br.com.petzon.petzonapi.controller;

import br.com.petzon.petzonapi.entity.Pet;
import br.com.petzon.petzonapi.service.PetService; // MUDANÇA AQUI
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/pets")
public class PetController {

    // Agora o Controller depende do Service, e não mais do Repository
    @Autowired
    private PetService petService;

    @GetMapping
    public List<Pet> listarPets(@RequestParam(required = false) String tipo) {

        return petService.listarTodosOuPorTipo(tipo);
    }
}