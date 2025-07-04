package br.com.petzon.petzonapi.service;

import br.com.petzon.petzonapi.dto.PetRequest;
import br.com.petzon.petzonapi.dto.PetResponse;
import br.com.petzon.petzonapi.dto.ResponsavelDto;
import br.com.petzon.petzonapi.entity.Pet;
import br.com.petzon.petzonapi.entity.PetType;
import br.com.petzon.petzonapi.entity.Usuario;
import br.com.petzon.petzonapi.exception.NotFoundException;
import br.com.petzon.petzonapi.exception.RegraDeNegocioException;
import br.com.petzon.petzonapi.repository.PetRepository;
import br.com.petzon.petzonapi.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PetService {

    private final ObjectMapper objectMapper;
    private final PetRepository petRepository;
    private final S3Service s3Service;
    private final UsuarioRepository usuarioRepository;

    private static final List<String> ALLOWED_IMAGE_TYPES = List.of("image/jpeg", "image/png", "image/gif");

    @Cacheable("petsList")
    public Page<PetResponse> listarPorTipo(String tipo, Pageable pageable) {
        try {
            PetType petType = PetType.valueOf(tipo.toUpperCase());
            Page<Pet> petPage = petRepository.findByTipo(petType, pageable);

            return petPage.map(this::mapToPetResponse);

        } catch (IllegalArgumentException e) {
            return Page.empty(pageable);
        }
    }

    @Cacheable("petsList")
    public Page<PetResponse> listarTodos(Pageable pageable) {
        try {
            Page<Pet> petPage = petRepository.findAll(pageable);

            return petPage.map(this::mapToPetResponse);

        } catch (IllegalArgumentException e) {
            return Page.empty(pageable);
        }
    }

    @Cacheable(value = "petById", key = "#id")
    public PetResponse buscarPorId(int id) throws NotFoundException {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pet não encontrado com o ID: " + id));
        return mapToPetResponse(pet);
    }

    @CacheEvict(value = "petsList", allEntries = true)
    public PetResponse cadastrarPet(PetRequest petRequest, MultipartFile imagem) throws IOException, RegraDeNegocioException {
        validateImage(imagem);
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        int idUsuarioLogado = Integer.parseInt((String) principal);

        Usuario responsavel = usuarioRepository.findById(idUsuarioLogado)
                .orElseThrow(() -> new NotFoundException("Usuário responsável não encontrado."));

        URL imageUrl = s3Service.uploadFile(imagem);

        Pet newPet = objectMapper.convertValue(petRequest, Pet.class);
        newPet.setUrlFoto(imageUrl.toString());
        newPet.setResponsavel(responsavel);

        Pet petCriado = petRepository.save(newPet);

        return mapToPetResponse(petCriado);

    }

    @CacheEvict(value = "petsList", allEntries = true)
    public PetResponse atualizarPet(int id, PetRequest petDto, MultipartFile imagem) throws NotFoundException, IOException {
        Pet petExistente = petRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pet não encontrado com o ID: " + id));

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

        return mapToPetResponse(petAtualizado);
    }

    @CacheEvict(value = {"petById", "petsList"}, allEntries = true, key = "#id")
    public void deletarPet(int id) throws NotFoundException {
        Pet petParaDeletar = petRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pet não encontrado com o ID: " + id));
        petRepository.delete(petParaDeletar);
    }

    private PetResponse mapToPetResponse(Pet pet) {
        PetResponse petResponse = objectMapper.convertValue(pet, PetResponse.class);

        if (pet.getResponsavel() != null) {
            ResponsavelDto responsavelDto = new ResponsavelDto();
            responsavelDto.setIdUsuario(pet.getResponsavel().getIdUsuario());
            responsavelDto.setNome(pet.getResponsavel().getNome());
            petResponse.setResponsavel(responsavelDto);
        }
        return petResponse;
    }

    private void validateImage(MultipartFile image){
        if (image == null || image.isEmpty()) {
            throw new RegraDeNegocioException("A foto do pet é obrigatória.");
        }

        String contentType = image.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new RegraDeNegocioException("Tipo de arquivo inválido. Apenas imagens (JPEG, PNG, GIF) são permitidas. Tipo enviado: " + contentType);
        }
    }
}