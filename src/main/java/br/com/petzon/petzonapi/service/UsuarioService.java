package br.com.petzon.petzonapi.service;

import br.com.petzon.petzonapi.dto.UsuarioCreateDto;
import br.com.petzon.petzonapi.dto.UsuarioDto;
import br.com.petzon.petzonapi.entity.Cargo;
import br.com.petzon.petzonapi.entity.Role;
import br.com.petzon.petzonapi.entity.Usuario;
import br.com.petzon.petzonapi.exception.RegraDeNegocioException;
import br.com.petzon.petzonapi.repository.CargoRepository;
import br.com.petzon.petzonapi.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final CargoRepository cargoRepository;
    private final PasswordEncoder passwordEncoder;


    public UsuarioDto criarUsuario(UsuarioCreateDto usuarioCreateDto) throws RegraDeNegocioException {
        if (usuarioRepository.findByEmail(usuarioCreateDto.getEmail()).isPresent()) {
            throw new RegraDeNegocioException("Email já cadastrado!");
        }

        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(usuarioCreateDto.getNome());
        novoUsuario.setEmail(usuarioCreateDto.getEmail());
        novoUsuario.setSenha(passwordEncoder.encode(usuarioCreateDto.getSenha()));
        novoUsuario.setAtivo(true);

        Cargo cargoUsuario = cargoRepository.findByNome("ROLE_" + Role.USER.name())
                .orElseThrow(() -> new RegraDeNegocioException("Cargo 'ROLE_USER' não encontrado."));

        Set<Cargo> cargos = new HashSet<>();
        cargos.add(cargoUsuario);
        novoUsuario.setCargos(cargos);

        Usuario usuarioSalvo = usuarioRepository.save(novoUsuario);

        return mapToDto(usuarioSalvo);
    }

    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public Optional<Usuario> findById(Integer id) {
        return usuarioRepository.findById(id);
    }

    public UsuarioDto getLoggedUser(Integer idUsuario) throws RegraDeNegocioException {
        Usuario usuario = findById(idUsuario)
                .orElseThrow(() -> new RegraDeNegocioException("Usuário não encontrado"));

        UsuarioDto UsuarioDto = new UsuarioDto();
        UsuarioDto.setIdUsuario(usuario.getIdUsuario());
        UsuarioDto.setNome(usuario.getNome());
        UsuarioDto.setEmail(usuario.getEmail());
        UsuarioDto.setCargos(usuario.getCargos().stream()
                .map(Cargo::getAuthority)
                .collect(Collectors.toSet()));

        return UsuarioDto;
    }

    public List<UsuarioDto> listarUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public UsuarioDto promoverParaOng(Integer idUsuario) throws RegraDeNegocioException {
        Usuario usuario = findById(idUsuario)
                .orElseThrow(() -> new RegraDeNegocioException("Usuário não encontrado."));

        Cargo cargoOng = cargoRepository.findByNome("ROLE_ONG")
                .orElseThrow(() -> new RegraDeNegocioException("Cargo 'ROLE_ONG' não encontrado."));

        usuario.getCargos().add(cargoOng);
        usuarioRepository.save(usuario);
        return mapToDto(usuario);
    }

    private UsuarioDto mapToDto(Usuario usuario) {
        UsuarioDto dto = new UsuarioDto();
        dto.setIdUsuario(usuario.getIdUsuario());
        dto.setNome(usuario.getNome());
        dto.setEmail(usuario.getEmail());
        dto.setCargos(usuario.getCargos().stream()
                .map(Cargo::getAuthority)
                .collect(Collectors.toSet()));
        return dto;
    }
}
