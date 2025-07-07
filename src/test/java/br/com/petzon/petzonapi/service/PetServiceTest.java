package br.com.petzon.petzonapi.service;

import br.com.petzon.petzonapi.dto.PetRequest;
import br.com.petzon.petzonapi.dto.PetResponse;
import br.com.petzon.petzonapi.entity.Pet;
import br.com.petzon.petzonapi.entity.PetType;
import br.com.petzon.petzonapi.entity.Usuario;
import br.com.petzon.petzonapi.exception.NotFoundException;
import br.com.petzon.petzonapi.exception.RegraDeNegocioException;
import br.com.petzon.petzonapi.repository.PetRepository;
import br.com.petzon.petzonapi.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PetServiceTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private S3Service s3Service;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PetService petService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testListarPorTipo_Success() {
        Pageable pageable = Pageable.unpaged();
        Pet pet = new Pet();
        Page<Pet> petPage = new PageImpl<>(Collections.singletonList(pet));
        when(petRepository.findByTipo(PetType.CACHORRO, pageable)).thenReturn(petPage);
        when(objectMapper.convertValue(any(Pet.class), eq(PetResponse.class))).thenReturn(new PetResponse());

        Page<PetResponse> result = petService.listarPorTipo("CACHORRO", pageable);

        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        verify(petRepository).findByTipo(PetType.CACHORRO, pageable);
    }

    @Test
    void testListarPorTipo_EmptyResultForInvalidType() {
        Pageable pageable = Pageable.unpaged();

        Page<PetResponse> result = petService.listarPorTipo("INVALIDO", pageable);

        assertTrue(result.isEmpty());
    }

    @Test
    void testListarTodos_Success() {
        Pageable pageable = Pageable.unpaged();
        Pet pet = new Pet();
        Page<Pet> petPage = new PageImpl<>(Collections.singletonList(pet));
        when(petRepository.findAll(pageable)).thenReturn(petPage);
        when(objectMapper.convertValue(any(Pet.class), eq(PetResponse.class))).thenReturn(new PetResponse());

        Page<PetResponse> result = petService.listarTodos(pageable);

        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        verify(petRepository).findAll(pageable);
    }

    @Test
    void testBuscarPorId_Success() {
        int petId = 1;
        Pet pet = new Pet();
        when(petRepository.findById(petId)).thenReturn(Optional.of(pet));
        when(objectMapper.convertValue(any(Pet.class), eq(PetResponse.class))).thenReturn(new PetResponse());

        PetResponse result = petService.buscarPorId(petId);

        assertNotNull(result);
        verify(petRepository).findById(petId);
    }

    @Test
    void testBuscarPorId_NotFound() {
        int petId = 1;
        when(petRepository.findById(petId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> petService.buscarPorId(petId));
    }

    @Test
    void testCadastrarPet_Success() throws IOException {
        PetRequest petRequest = new PetRequest();
        petRequest.setNome("Bolinha");
        petRequest.setTipo(PetType.CACHORRO);
        petRequest.setTemperamento("Brincalhão");
        petRequest.setDescricao("Um cãozinho muito amigável");
        petRequest.setIdade(2);

        MockMultipartFile image = new MockMultipartFile("foto", "test.jpg", "image/jpeg", "test data".getBytes());
        URL imageUrl = new URL("http://example.com/test.jpg");
        Usuario responsavel = new Usuario();
        responsavel.setIdUsuario(1);

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("1");
        SecurityContextHolder.setContext(securityContext);

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(responsavel));
        when(s3Service.uploadFile(image)).thenReturn(imageUrl);
        when(objectMapper.convertValue(any(PetRequest.class), eq(Pet.class))).thenReturn(new Pet());
        when(petRepository.save(any(Pet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(objectMapper.convertValue(any(Pet.class), eq(PetResponse.class))).thenReturn(new PetResponse());


        PetResponse result = petService.cadastrarPet(petRequest, image);

        assertNotNull(result);
        verify(petRepository).save(any(Pet.class));
    }

    @Test
    void testCadastrarPet_InvalidImage() {
        PetRequest petRequest = new PetRequest();
        petRequest.setNome("Bolinha");
        petRequest.setTipo(PetType.CACHORRO);
        petRequest.setTemperamento("Brincalhão");
        petRequest.setDescricao("Um cãozinho muito amigável");
        petRequest.setIdade(2);

        MockMultipartFile image = new MockMultipartFile("foto", "test.txt", "text/plain", "test data".getBytes());

        assertThrows(RegraDeNegocioException.class, () -> petService.cadastrarPet(petRequest, image));
    }


    @Test
    void testAtualizarPet_Success() throws IOException {
        int petId = 1;
        PetRequest petRequest = new PetRequest();
        petRequest.setNome("Bolinha Atualizado");
        petRequest.setTipo(PetType.GATO);
        petRequest.setTemperamento("Calmo");
        petRequest.setDescricao("Um gato muito tranquilo");
        petRequest.setIdade(3);

        MockMultipartFile image = new MockMultipartFile("foto", "update.jpg", "image/jpeg", "new test data".getBytes());
        Pet petExistente = new Pet();
        URL newImageUrl = new URL("http://example.com/update.jpg");

        when(petRepository.findById(petId)).thenReturn(Optional.of(petExistente));
        when(s3Service.uploadFile(image)).thenReturn(newImageUrl);
        when(petRepository.save(any(Pet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(objectMapper.convertValue(any(Pet.class), eq(PetResponse.class))).thenReturn(new PetResponse());

        PetResponse result = petService.atualizarPet(petId, petRequest, image);

        assertNotNull(result);
        verify(petRepository).save(petExistente);
    }

    @Test
    void testAtualizarPet_NotFound() {
        int petId = 1;
        PetRequest petRequest = new PetRequest();
        petRequest.setNome("Bolinha");
        petRequest.setTipo(PetType.CACHORRO);
        petRequest.setTemperamento("Brincalhão");
        petRequest.setDescricao("Um cãozinho muito amigável");
        petRequest.setIdade(2);

        MockMultipartFile image = new MockMultipartFile("foto", "update.jpg", "image/jpeg", "new test data".getBytes());

        when(petRepository.findById(petId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> petService.atualizarPet(petId, petRequest, image));
    }

    @Test
    void testDeletarPet_Success() {
        int petId = 1;
        Pet pet = new Pet();
        when(petRepository.findById(petId)).thenReturn(Optional.of(pet));

        petService.deletarPet(petId);

        verify(petRepository).delete(pet);
    }

    @Test
    void testDeletarPet_NotFound() {
        int petId = 1;
        when(petRepository.findById(petId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> petService.deletarPet(petId));
    }
}