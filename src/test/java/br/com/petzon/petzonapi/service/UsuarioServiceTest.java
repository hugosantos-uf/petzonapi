package br.com.petzon.petzonapi.service;

import br.com.petzon.petzonapi.dto.UsuarioRequest;
import br.com.petzon.petzonapi.dto.UsuarioResponse;
import br.com.petzon.petzonapi.entity.Cargo;
import br.com.petzon.petzonapi.entity.Role;
import br.com.petzon.petzonapi.entity.Usuario;
import br.com.petzon.petzonapi.exception.NotFoundException;
import br.com.petzon.petzonapi.exception.RegraDeNegocioException;
import br.com.petzon.petzonapi.repository.CargoRepository;
import br.com.petzon.petzonapi.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CargoRepository cargoRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private UsuarioService usuarioService;

    private UsuarioRequest usuarioRequest;
    private Usuario usuario;
    private Cargo cargoUser;
    private Cargo cargoOng;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        usuarioRequest = new UsuarioRequest();
        usuarioRequest.setNome("Teste User");
        usuarioRequest.setEmail("teste@example.com");
        usuarioRequest.setSenha("password123");

        cargoUser = new Cargo();
        cargoUser.setIdCargo(1);
        cargoUser.setNome("ROLE_USER");

        cargoOng = new Cargo();
        cargoOng.setIdCargo(2);
        cargoOng.setNome("ROLE_ONG");

        usuario = new Usuario();
        usuario.setIdUsuario(1);
        usuario.setNome("Teste User");
        usuario.setEmail("teste@example.com");
        usuario.setAtivo(true);
        usuario.setCargos(new HashSet<>(Set.of(cargoUser)));
    }


    @Test
    void testCriarUsuario_Success() {
        Usuario novoUsuario = new Usuario();
        novoUsuario.setCargos(new HashSet<>());

        Usuario usuarioSalvo = new Usuario();
        usuarioSalvo.setIdUsuario(1);
        usuarioSalvo.setCargos(new HashSet<>(Set.of(cargoUser)));

        UsuarioResponse responseDto = new UsuarioResponse();

        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(objectMapper.convertValue(usuarioRequest, Usuario.class)).thenReturn(novoUsuario);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(cargoRepository.findByNome("ROLE_" + Role.USER.name())).thenReturn(Optional.of(cargoUser));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioSalvo);

        when(objectMapper.convertValue(usuarioSalvo, UsuarioResponse.class)).thenReturn(responseDto);

        UsuarioResponse result = usuarioService.criarUsuario(usuarioRequest);

        assertNotNull(result);
        assertEquals(responseDto, result);
        verify(usuarioRepository).save(novoUsuario);
        assertTrue(novoUsuario.getCargos().contains(cargoUser));
    }

    @Test
    void testCriarUsuario_EmailJaCadastrado() {
        when(usuarioRepository.findByEmail(usuarioRequest.getEmail())).thenReturn(Optional.of(new Usuario()));

        assertThrows(RegraDeNegocioException.class, () -> usuarioService.criarUsuario(usuarioRequest));
    }

    @Test
    void testGetLoggedUser_Success() {
        int userId = 1;
        UsuarioResponse responseDto = new UsuarioResponse();
        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));
        when(objectMapper.convertValue(usuario, UsuarioResponse.class)).thenReturn(responseDto);

        UsuarioResponse result = usuarioService.getLoggedUser(userId);

        assertNotNull(result);
        assertEquals(responseDto, result);
    }

    @Test
    void testGetLoggedUser_NotFound() {
        int userId = 99;
        when(usuarioRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> usuarioService.getLoggedUser(userId));
    }

    @Test
    void testPromoverParaOng_Success() {
        int userId = 1;
        UsuarioResponse responseDto = new UsuarioResponse();

        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));
        when(cargoRepository.findByNome("ROLE_ONG")).thenReturn(Optional.of(cargoOng));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(objectMapper.convertValue(usuario, UsuarioResponse.class)).thenReturn(responseDto);

        UsuarioResponse result = usuarioService.promoverParaOng(userId);

        assertNotNull(result);
        assertTrue(usuario.getCargos().contains(cargoOng));
    }

    @Test
    void testDesativarUsuario_Success() {
        int userId = 1;
        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));

        String result = usuarioService.desativarUsuario(userId);

        assertEquals("Usuário desativad", result);
        assertFalse(usuario.isAtivo());
        verify(usuarioRepository).save(usuario);
    }
}