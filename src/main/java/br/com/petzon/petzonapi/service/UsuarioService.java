package br.com.petzon.petzonapi.service;

import br.com.petzon.petzonapi.dto.UsuarioDto;
import br.com.petzon.petzonapi.dto.UsuarioLogadoDto;
import br.com.petzon.petzonapi.exception.RegraDeNegocioException;
import br.com.petzon.petzonapi.entity.Cargo;
import br.com.petzon.petzonapi.entity.Role;
import br.com.petzon.petzonapi.entity.Usuario;
import br.com.petzon.petzonapi.repository.CargoRepository;
import br.com.petzon.petzonapi.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final CargoRepository cargoRepository;
    private final PasswordEncoder passwordEncoder;


    public Usuario criarUsuario(UsuarioDto usuarioDto) throws RegraDeNegocioException {
        if (usuarioRepository.findByEmail(usuarioDto.getEmail()).isPresent()) {
            throw new RegraDeNegocioException("Email já cadastrado!");
        }

        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(usuarioDto.getNome());
        novoUsuario.setEmail(usuarioDto.getEmail());
        novoUsuario.setSenha(passwordEncoder.encode(usuarioDto.getSenha()));
        novoUsuario.setAtivo(true);

        Cargo cargoUsuario = cargoRepository.findByNome("ROLE_" + Role.USER.name())
                .orElseThrow(() -> new RegraDeNegocioException("Cargo 'ROLE_USER' não encontrado no banco. Execute os scripts iniciais."));

        Set<Cargo> cargos = new HashSet<>();
        cargos.add(cargoUsuario);
        novoUsuario.setCargos(cargos);

        return usuarioRepository.save(novoUsuario);
    }

    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public Optional<Usuario> findById(Integer id) {
        return usuarioRepository.findById(id);
    }

    public UsuarioLogadoDto getLoggedUser(Integer idUsuario) throws RegraDeNegocioException {
        Usuario usuario = findById(idUsuario)
                .orElseThrow(() -> new RegraDeNegocioException("Usuário não encontrado"));

        UsuarioLogadoDto usuarioLogadoDto = new UsuarioLogadoDto();
        usuarioLogadoDto.setIdUsuario(usuario.getIdUsuario());
        usuarioLogadoDto.setNome(usuario.getNome());
        usuarioLogadoDto.setEmail(usuario.getEmail());
        usuarioLogadoDto.setCargos(usuario.getCargos().stream()
                .map(Cargo::getAuthority)
                .collect(Collectors.toSet()));

        return usuarioLogadoDto;
    }
}