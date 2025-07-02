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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final CargoRepository cargoRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    public UsuarioResponse criarUsuario(UsuarioRequest usuarioRequest) {
        if (usuarioRepository.findByEmail(usuarioRequest.getEmail()).isPresent()) {
            throw new RegraDeNegocioException("Email já cadastrado!");
        }

        Usuario novoUsuario = objectMapper.convertValue(usuarioRequest, Usuario.class);

        novoUsuario.setSenha(passwordEncoder.encode(usuarioRequest.getSenha()));
        novoUsuario.setAtivo(true);

        Cargo cargoUsuario = cargoRepository.findByNome("ROLE_" + Role.USER.name())
                .orElseThrow(() -> new NotFoundException("Cargo 'ROLE_USER' não encontrado."));

        Set<Cargo> cargos = new HashSet<>();
        cargos.add(cargoUsuario);
        novoUsuario.setCargos(cargos);

        Usuario usuarioSalvo = usuarioRepository.save(novoUsuario);

        return mapToDto(usuarioSalvo);
    }

    public UsuarioResponse getLoggedUser(int idUsuario){
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        return mapToDto(usuario);
    }

    public Page<UsuarioResponse> listarUsuarios(Pageable pageable) {
        Page<Usuario> usuariosPage = usuarioRepository.findAll(pageable);

        return usuariosPage.map(this::mapToDto);
    }

    public UsuarioResponse promoverParaOng(int idUsuario){
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado."));

        Cargo cargoOng = cargoRepository.findByNome("ROLE_ONG")
                .orElseThrow(() -> new NotFoundException("Cargo 'ROLE_ONG' não encontrado."));

        usuario.getCargos().add(cargoOng);
        usuarioRepository.save(usuario);
        return mapToDto(usuario);
    }

    public String desativarUsuario(int idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
        return "Usuário desativado";
    }

    private UsuarioResponse mapToDto(Usuario usuario) {
        UsuarioResponse dto = objectMapper.convertValue(usuario, UsuarioResponse.class);
        dto.setCargos(usuario.getCargos().stream()
                .map(Cargo::getAuthority)
                .collect(Collectors.toSet()));
        return dto;
    }
}