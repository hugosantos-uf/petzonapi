package br.com.petzon.petzonapi.service;

import br.com.petzon.petzonapi.dto.CreatePetDto;
import br.com.petzon.petzonapi.entity.Pet;
import br.com.petzon.petzonapi.entity.PetType;
import br.com.petzon.petzonapi.exception.PetNaoEncontradoException;
import br.com.petzon.petzonapi.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;

    public Page<Pet> listarTodosOuPorTipo(String tipo, Pageable pageable) {
        if (tipo != null) {
            try {
                PetType petType = PetType.valueOf(tipo.toUpperCase());
                return petRepository.findByTipo(petType, pageable);
            } catch (IllegalArgumentException e) {
                return Page.empty(pageable);
            }
        }
        return petRepository.findAll(pageable);
    }

    public Pet buscarPorId(Integer id) {
        return petRepository.findById(id)
                .orElseThrow(() -> new PetNaoEncontradoException("Pet não encontrado com o ID: " + id));
    }

    public Pet cadastrarPet(CreatePetDto petDto) {
        Pet novoPet = new Pet();
        novoPet.setTipo(petDto.getTipo());
        novoPet.setNome(petDto.getNome());
        novoPet.setTemperamento(petDto.getTemperamento());
        novoPet.setDescricao(petDto.getDescricao());
        novoPet.setIdade(petDto.getIdade());
        novoPet.setUrlFoto(petDto.getUrlFoto());

        return petRepository.save(novoPet);
    }

    public Pet atualizarPet(Integer id, CreatePetDto petDto) throws PetNaoEncontradoException {
        Pet petExistente = buscarPorId(id);

        petExistente.setTipo(petDto.getTipo());
        petExistente.setNome(petDto.getNome());
        petExistente.setTemperamento(petDto.getTemperamento());
        petExistente.setDescricao(petDto.getDescricao());
        petExistente.setIdade(petDto.getIdade());
        petExistente.setUrlFoto(petDto.getUrlFoto());

        return petRepository.save(petExistente);
    }

    public void deletarPet(Integer id) throws PetNaoEncontradoException {
        Pet petParaDeletar = buscarPorId(id);
        petRepository.delete(petParaDeletar);
    }
}