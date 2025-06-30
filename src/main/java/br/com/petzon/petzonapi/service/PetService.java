package br.com.petzon.petzonapi.service;

import br.com.petzon.petzonapi.dto.CreatePetDto;
import br.com.petzon.petzonapi.exception.PetNaoEncontradoException;
import br.com.petzon.petzonapi.entity.Pet;
import br.com.petzon.petzonapi.entity.PetType;
import br.com.petzon.petzonapi.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final S3Service s3Service; // Injete o novo serviço de S3

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

    /**
     * Cadastra um novo pet, fazendo o upload da imagem para o S3 primeiro.
     * @param petDto DTO com os dados textuais do pet.
     * @param imagem O arquivo de imagem do pet.
     * @return O pet salvo com a URL da imagem do S3.
     * @throws IOException se ocorrer um erro no upload.
     */
    public Pet cadastrarPet(CreatePetDto petDto, MultipartFile imagem) throws IOException {
        // 1. Faz o upload do arquivo para o S3 e obtém a URL pública
        URL imageUrl = s3Service.uploadFile(imagem);

        // 2. Cria a entidade Pet com os dados do DTO e a URL do S3
        Pet novoPet = new Pet();
        novoPet.setTipo(petDto.getTipo());
        novoPet.setNome(petDto.getNome());
        novoPet.setTemperamento(petDto.getTemperamento());
        novoPet.setDescricao(petDto.getDescricao());
        novoPet.setIdade(petDto.getIdade());
        novoPet.setUrlFoto(imageUrl.toString()); // Salva a URL completa retornada pelo S3

        // 3. Salva o pet no banco de dados
        return petRepository.save(novoPet);
    }

    /**
     * Atualiza um pet existente, incluindo a substituição da imagem no S3.
     * @param id O ID do pet a ser atualizado.
     * @param petDto DTO com os novos dados textuais.
     * @param imagem O novo arquivo de imagem.
     * @return O pet atualizado.
     * @throws PetNaoEncontradoException se o pet não for encontrado.
     * @throws IOException se ocorrer um erro no upload.
     */
    public Pet atualizarPet(Integer id, CreatePetDto petDto, MultipartFile imagem) throws PetNaoEncontradoException, IOException {
        // Primeiro, busca o pet existente para garantir que ele existe
        Pet petExistente = buscarPorId(id);

        // Se uma nova imagem foi enviada, faz o upload
        if (imagem != null && !imagem.isEmpty()) {
            URL newImageUrl = s3Service.uploadFile(imagem);
            petExistente.setUrlFoto(newImageUrl.toString());
            // Nota: Uma melhoria futura seria deletar a imagem antiga do S3.
        }

        // Atualiza os outros campos
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
        // Nota: Uma melhoria futura seria deletar a imagem associada do S3.
    }
}