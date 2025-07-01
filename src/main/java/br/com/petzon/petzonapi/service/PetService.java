package br.com.petzon.petzonapi.service;

import br.com.petzon.petzonapi.dto.CreatePetDto;
import br.com.petzon.petzonapi.entity.Pet;
import br.com.petzon.petzonapi.entity.PetType;
import br.com.petzon.petzonapi.entity.Usuario;
import br.com.petzon.petzonapi.exception.PetNaoEncontradoException;
import br.com.petzon.petzonapi.exception.RegraDeNegocioException;
import br.com.petzon.petzonapi.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final S3Service s3Service;
    private final UsuarioService usuarioService;

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

    public Pet buscarPorId(int id) throws PetNaoEncontradoException {
        return petRepository.findById(id)
                .orElseThrow(() -> new PetNaoEncontradoException("Pet não encontrado com o ID: " + id));
    }

    public Pet cadastrarPet(CreatePetDto petDto, MultipartFile imagem) throws IOException, RegraDeNegocioException {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        int idUsuarioLogado = Integer.parseInt((String) principal);

        Usuario responsavel = usuarioService.findById(idUsuarioLogado)
                .orElseThrow(() -> new RegraDeNegocioException("Usuário responsável não encontrado."));

        URL imageUrl = s3Service.uploadFile(imagem);

        Pet novoPet = new Pet();
        novoPet.setTipo(petDto.getTipo());
        novoPet.setNome(petDto.getNome());
        novoPet.setTemperamento(petDto.getTemperamento());
        novoPet.setDescricao(petDto.getDescricao());
        novoPet.setIdade(petDto.getIdade());
        novoPet.setUrlFoto(imageUrl.toString());
        novoPet.setResponsavel(responsavel);

        return petRepository.save(novoPet);
    }

    public Pet atualizarPet(Integer id, CreatePetDto petDto, MultipartFile imagem) throws PetNaoEncontradoException, IOException {
        Pet petExistente = buscarPorId(id);

        if (imagem != null && !imagem.isEmpty()) {
            URL newImageUrl = s3Service.uploadFile(imagem);
            petExistente.setUrlFoto(newImageUrl.toString());
        }

        petExistente.setTipo(petDto.getTipo());
        petExistente.setNome(petDto.getNome());
        petExistente.setTemperamento(petDto.getTemperamento());
        petExistente.setDescricao(petDto.getDescricao());
        petExistente.setIdade(petDto.getIdade());

        return petRepository.save(petExistente);
    }

    public void deletarPet(Integer id) throws PetNaoEncontradoException {
        Pet petParaDeletar = buscarPorId(id);
        petRepository.delete(petParaDeletar);
    }
}