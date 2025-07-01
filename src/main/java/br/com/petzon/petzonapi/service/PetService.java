package br.com.petzon.petzonapi.service;

import br.com.petzon.petzonapi.dto.PetRequest;
import br.com.petzon.petzonapi.dto.PetResponse;
import br.com.petzon.petzonapi.entity.Pet;
import br.com.petzon.petzonapi.entity.PetType;
import br.com.petzon.petzonapi.entity.Usuario;
import br.com.petzon.petzonapi.exception.PetNaoEncontradoException;
import br.com.petzon.petzonapi.exception.RegraDeNegocioException;
import br.com.petzon.petzonapi.repository.PetRepository;
import br.com.petzon.petzonapi.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final ObjectMapper objectMapper;
    private final PetRepository petRepository;
    private final S3Service s3Service;
    private final UsuarioRepository usuarioRepository;

    public Page<Pet> listarPorTipo(String tipo, Pageable pageable) {
            try {
                PetType petType = PetType.valueOf(tipo.toUpperCase());
                return petRepository.findByTipo(petType, pageable);
            } catch (IllegalArgumentException e) {
                return Page.empty(pageable);
            }
    }

    public Pet buscarPorId(int id) throws PetNaoEncontradoException {
        return petRepository.findById(id)
                .orElseThrow(() -> new PetNaoEncontradoException("Pet não encontrado com o ID: " + id));
    }

    public PetResponse cadastrarPet(PetRequest petRequest, MultipartFile imagem) throws IOException, RegraDeNegocioException {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        int idUsuarioLogado = Integer.parseInt((String) principal);

        Usuario responsavel = usuarioRepository.findById(idUsuarioLogado)
                .orElseThrow(() -> new RegraDeNegocioException("Usuário responsável não encontrado."));

        URL imageUrl = s3Service.uploadFile(imagem);

        Pet newPet = objectMapper.convertValue(petRequest, Pet.class);
        newPet.setUrlFoto(imageUrl.toString());
        newPet.setResponsavel(responsavel);

        Pet petCriado = petRepository.save(newPet);

        return objectMapper.convertValue(petCriado, PetResponse.class);
    }

    public PetResponse atualizarPet(int id, PetRequest petDto, MultipartFile imagem) throws PetNaoEncontradoException, IOException {
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

        Pet petAtualizado = petRepository.save(petExistente);

        return objectMapper.convertValue(petAtualizado, PetResponse.class);
    }

    public void deletarPet(Integer id) throws PetNaoEncontradoException {
        Pet petParaDeletar = buscarPorId(id);
        petRepository.delete(petParaDeletar);
    }
}